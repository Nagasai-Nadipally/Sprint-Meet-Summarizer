package com.meetingnotes.controller;

import com.meetingnotes.dto.meeting.MeetingDetailResponse;
import com.meetingnotes.dto.meeting.MeetingSummaryResponse;
import com.meetingnotes.dto.meeting.SendEmailRequest;
import com.meetingnotes.entity.User;
import com.meetingnotes.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /** POST /api/meetings/upload — multipart audio/video + optional title. */
    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<MeetingDetailResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @AuthenticationPrincipal User user
    ) {
        MeetingDetailResponse response = meetingService.upload(file, title, user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /** GET /api/meetings — dashboard list for the current user. */
    @GetMapping
    public List<MeetingSummaryResponse> list(@AuthenticationPrincipal User user) {
        return meetingService.listForUser(user.getId());
    }

    /** GET /api/meetings/search?keyword=release */
    @GetMapping("/search")
    public List<MeetingSummaryResponse> search(
            @RequestParam String keyword,
            @AuthenticationPrincipal User user
    ) {
        return meetingService.search(user.getId(), keyword);
    }

    /** GET /api/meetings/{id} — full detail (transcript, summary, action items). */
    @GetMapping("/{id}")
    public MeetingDetailResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return meetingService.getForUser(id, user.getId());
    }

    /** POST /api/meetings/{id}/send-email */
    @PostMapping("/{id}/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(
            @PathVariable Long id,
            @Valid @RequestBody SendEmailRequest request,
            @AuthenticationPrincipal User user
    ) {
        meetingService.emailSummary(id, user.getId(), request.recipients());
        return ResponseEntity.ok(Map.of("message", "Summary sent to " + request.recipients().size() + " recipient(s)"));
    }
}
