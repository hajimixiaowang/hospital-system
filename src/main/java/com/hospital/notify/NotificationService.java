package com.hospital.notify;

import org.springframework.stereotype.Service;

import java.util.List;

/** 通知查询/已读维护（通知中心页面用） */
@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void insert(Long userId, String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setStatus(Notification.STATUS_UNREAD);
        n.setCreateTime(java.time.LocalDateTime.now());
        notificationMapper.insert(n);
    }

    public List<Notification> listByUser(Long userId) {
        return notificationMapper.listByUser(userId);
    }

    public Long unreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    public void markRead(Long id, Long userId) {
        notificationMapper.markRead(id, userId);
    }
}
