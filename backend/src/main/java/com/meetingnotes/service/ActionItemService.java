package com.meetingnotes.service;

import com.meetingnotes.dto.meeting.ActionItemResponse;
import com.meetingnotes.entity.ActionItem;
import com.meetingnotes.entity.ActionItemStatus;
import com.meetingnotes.exception.ResourceNotFoundException;
import com.meetingnotes.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;

    @Transactional
    public ActionItemResponse updateStatus(Long itemId, Long userId, ActionItemStatus status) {
        ActionItem item = actionItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Action item not found"));

        // Ensure the item belongs to a meeting owned by the requesting user.
        if (!item.getMeeting().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Not your action item");
        }

        item.setStatus(status);
        return ActionItemResponse.from(item);
    }
}
