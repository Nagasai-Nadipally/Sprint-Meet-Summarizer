package com.meetingnotes.service;

import com.meetingnotes.entity.ActionItem;
import com.meetingnotes.entity.Meeting;
import com.meetingnotes.exception.AiProcessingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendSummary(Meeting meeting, List<String> recipients) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Meeting Summary: " + meeting.getTitle());
            helper.setText(buildHtml(meeting), true);
            mailSender.send(message);
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
                  .append(cell(item.getDeadline() == null ? "—" : item.getDeadline().toString()))
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
