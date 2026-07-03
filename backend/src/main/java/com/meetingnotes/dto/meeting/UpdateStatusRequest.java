package com.meetingnotes.dto.meeting;

import com.meetingnotes.entity.ActionItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "status is required")
        ActionItemStatus status
) {}
