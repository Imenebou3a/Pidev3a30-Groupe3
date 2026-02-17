package org.example.entities;

import java.time.LocalDateTime;

/**
 * Entity representing a reaction (like, etc.) on a post or comment.
 */
public class ForumReaction {
    public enum ReactionType {
        LIKE, LOVE, HELPFUL, SAD, ANGRY
    }

    private Integer id;
    private Integer userId;
    private Integer postId;      // nullable if reaction is on comment
    private Integer commentId;   // nullable if reaction is on post
    private ReactionType type;
    private LocalDateTime createdAt;

    public ForumReaction() {
    }

    public ForumReaction(Integer userId, Integer postId, Integer commentId, ReactionType type) {
        this.userId = userId;
        this.postId = postId;
        this.commentId = commentId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
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

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ForumReaction{id=" + id + ", userId=" + userId + ", type=" + type + ", postId=" + postId + ", commentId=" + commentId + "}";
    }
}
