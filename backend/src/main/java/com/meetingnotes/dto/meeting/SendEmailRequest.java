package com.meetingnotes.dto.meeting;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SendEmailRequest(
        @NotEmpty(message = "At least one recipient is required")
        List<@Email(message = "Each recipient must be a valid email") String> recipients
) {}
