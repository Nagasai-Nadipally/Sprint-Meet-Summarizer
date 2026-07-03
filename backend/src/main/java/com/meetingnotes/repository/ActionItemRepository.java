package com.meetingnotes.repository;

import com.meetingnotes.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
    List<ActionItem> findByMeetingIdOrderByIdAsc(Long meetingId);
}
