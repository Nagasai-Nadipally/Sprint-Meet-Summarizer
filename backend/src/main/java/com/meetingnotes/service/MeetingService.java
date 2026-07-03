package com.meetingnotes.service;

import com.meetingnotes.dto.meeting.MeetingDetailResponse;
import com.meetingnotes.dto.meeting.MeetingSummaryResponse;
import com.meetingnotes.entity.Meeting;
import com.meetingnotes.entity.MeetingStatus;
import com.meetingnotes.entity.User;
import com.meetingnotes.exception.BadRequestException;
import com.meetingnotes.exception.ResourceNotFoundException;
import com.meetingnotes.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final StorageService storageService;
    private final MeetingProcessingService processingService;
    private final EmailService emailService;

    /**
     * Stores the upload, persists a Meeting in UPLOADED state, then fires the
     * async pipeline. Returns immediately so the client can poll for progress.
     */
    public MeetingDetailResponse upload(MultipartFile file, String title, User owner) {
        // store() validates type/size and throws BadRequestException on failure
        String storageKey = storageService.store(file);

        String resolvedTitle = StringUtils.hasText(title)
                ? title.trim()
                : deriveTitle(file.getOriginalFilename());

        Meeting meeting = Meeting.builder()
                .title(resolvedTitle)
                .originalFilename(file.getOriginalFilename())
                .storageKey(storageKey)
                .fileSizeBytes(file.getSize())
                .status(MeetingStatus.UPLOADED)
                .owner(owner)
                .build();

        // repository.save commits in its own transaction before we kick off async work
        Meeting saved = meetingRepository.save(meeting);

        processingService.process(saved.getId());

        return MeetingDetailResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> listForUser(Long userId) {
        return meetingRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(MeetingSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getForUser(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findByIdAndOwnerId(meetingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        return MeetingDetailResponse.from(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> search(Long userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("Search keyword cannot be empty");
        }
        return meetingRepository.searchByKeyword(userId, keyword.trim()).stream()
                .map(MeetingSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public void emailSummary(Long meetingId, Long userId, List<String> recipients) {
        Meeting meeting = meetingRepository.findByIdAndOwnerId(meetingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        if (meeting.getStatus() != MeetingStatus.COMPLETED) {
            throw new BadRequestException("This meeting hasn't finished processing yet");
        }
        emailService.sendSummary(meeting, recipients);
    }

    private String deriveTitle(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "Untitled meeting";
        }
        String base = StringUtils.stripFilenameExtension(filename);
        return base.replace('_', ' ').replace('-', ' ').trim();
    }
}
