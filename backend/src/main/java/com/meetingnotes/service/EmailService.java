package com.meetingnotes.service;

import com.meetingnotes.entity.ActionItem;
import com.meetingnotes.entity.Meeting;
import com.meetingnotes.exception.AiProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends the meeting summary as an email via the Resend HTTP API
 * (https://resend.com). Uses plain HTTPS instead of SMTP, so it works on hosts
 * that block outbound SMTP ports (such as Render's free tier).
 */
@Service
public class EmailService {

    private final RestClient resend;
    private final String fromAddress;

    public EmailService(
            @Value("${RESEND_API_KEY:}") String apiKey,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.fromAddress = fromAddress;
        this.resend = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public void sendSummary(Meeting meeting, List<String> recipients) {
        Map<String, Object> payload = Map.of(
                "from", fromAddress,
                "to", recipients,
                "subject", "Meeting Summary: " + meeting.getTitle(),
                "html", buildHtml(meeting)
        );
        try {
            resend.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new AiProcessingException("Could not send the summary email: " + e.getMessage(), e);
        }
    }

    private String buildHtml(Meeting meeting) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Inter,Arial,sans-serif;max-width:640px;margin:0 auto;color:#1f2933;\">");
        sb.append("<h2 style=\"color:#2f5d8a;\">").append(escape(meeting.getTitle())).append("</h2>");

        if (meeting.getSummary() != null) {
            sb.append("<h3>Summary</h3>");
            sb.append("<p style=\"line-height:1.6;\">")
              .append(escape(meeting.getSummary().getOverview()))
              .append("</p>");

            var keyPoints = meeting.getSummary().getKeyPoints();
            if (keyPoints != null && !keyPoints.isEmpty()) {
                sb.append("<h3>Key discussion points</h3><ul>");
                keyPoints.forEach(p -> sb.append("<li>").append(escape(p)).append("</li>"));
                sb.append("</ul>");
            }
        }

        List<ActionItem> items = meeting.getActionItems();
        if (items != null && !items.isEmpty()) {
            sb.append("<h3>Action items</h3>");
            sb.append("<table style=\"border-collapse:collapse;width:100%;\">");
            sb.append("<tr style=\"background:#f0f4f8;text-align:left;\">")
              .append("<th style=\"padding:8px;border:1px solid #d9e2ec;\">Owner</th>")
              .append("<th style=\"padding:8px;border:1px solid #d9e2ec;\">Task</th>")
              .append("<th style=\"padding:8px;border:1px solid #d9e2ec;\">Deadline</th></tr>");
            for (ActionItem item : items) {
                sb.append("<tr>")
                  .append(cell(item.getOwner()))
                  .append(cell(item.getTask()))
                  .append(cell(item.getDeadline() == null ? "-" : item.getDeadline().toString()))
                  .append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("<p style=\"margin-top:24px;font-size:12px;color:#829ab1;\">")
          .append("Generated automatically from the meeting recording.</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private String cell(String value) {
        return "<td style=\"padding:8px;border:1px solid #d9e2ec;\">" + escape(value) + "</td>";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}