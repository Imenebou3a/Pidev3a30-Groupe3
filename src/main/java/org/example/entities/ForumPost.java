package org.example.entities;

import java.time.LocalDateTime;

/**
 * Entity representing a forum post.
 * Schema: id, user_id, type, related_entity_type, related_entity_id, title, content,
 * rating, status, is_moderated, created_at, updated_at, view_count, is_archived.
 */
public class ForumPost {
    private Integer id;
    private Integer userId;
    private String type;
    private String relatedEntityType;
    private Integer relatedEntityId;
    private String title;
    private String content;
    private Integer rating;
    private String status;
    private boolean isModerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private boolean isArchived;

    public ForumPost() {
    }

    public ForumPost(String title, String content, Integer userId) {
        this.userId = userId;
        this.type = "DISCUSSION";
        this.relatedEntityType = null;
        this.relatedEntityId = null;
        this.title = title;
        this.content = content;
        this.rating = 0;
        this.status = "EN_ATTENTE";
        this.isModerated = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.isArchived = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isModerated() {
        return isModerated;
    }

    public void setModerated(boolean moderated) {
        isModerated = moderated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    @Override
    public String toString() {
        return "ForumPost{id=" + id + ", title='" + title + "', userId=" + userId + ", createdAt=" + createdAt + "}";
    }
}
