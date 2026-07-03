package com.meetingnotes.dto.meeting;

import com.meetingnotes.entity.ActionItem;
import com.meetingnotes.entity.ActionItemStatus;

import java.time.LocalDate;

public record ActionItemResponse(
        Long id,
        String owner,
        String task,
        LocalDate deadline,
        ActionItemStatus status
) {
    public static ActionItemResponse from(ActionItem item) {
        return new ActionItemResponse(
                item.getId(),
                item.getOwner(),
                item.getTask(),
                item.getDeadline(),
                item.getStatus()
        );
    }
}
