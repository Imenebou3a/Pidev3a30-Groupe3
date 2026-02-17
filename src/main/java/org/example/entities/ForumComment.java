package org.example.entities;

import java.time.LocalDateTime;

/**
 * Entity representing a comment on a forum post.
 */
public class ForumComment {
    private Integer id;
    private Integer postId;
    private Integer authorId;
    private Integer parentCommentId; // for nested replies
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ForumComment() {
    }

    public ForumComment(Integer postId, Integer authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Integer parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    @Override
    public String toString() {
        return "ForumComment{id=" + id + ", postId=" + postId + ", authorId=" + authorId + ", createdAt=" + createdAt + "}";
    }
}
