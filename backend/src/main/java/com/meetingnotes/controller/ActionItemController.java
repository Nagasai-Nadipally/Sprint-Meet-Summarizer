package com.meetingnotes.controller;

import com.meetingnotes.dto.meeting.ActionItemResponse;
import com.meetingnotes.dto.meeting.UpdateStatusRequest;
import com.meetingnotes.entity.User;
import com.meetingnotes.service.ActionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/action-items")
@RequiredArgsConstructor
public class ActionItemController {

    private final ActionItemService actionItemService;

    /** PUT /api/action-items/{id}/status — toggle Pending / In Progress / Completed. */
    @PutMapping("/{id}/status")
    public ActionItemResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal User user
    ) {
        return actionItemService.updateStatus(id, user.getId(), request.status());
    }
}
