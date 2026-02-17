package org.example.controllers.backoffice;

import org.example.entities.ForumComment;
import org.example.services.ForumCommentService;

import java.util.List;

/**
 * Backoffice controller for ForumComment entity: admin operations (list, delete comments).
 */
public class ForumCommentBackController {
    private final ForumCommentService commentService = new ForumCommentService();

    public List<ForumComment> getCommentsForPost(int postId) {
        return commentService.findByPostId(postId);
    }

    public boolean deleteComment(int commentId) {
        return commentService.delete(commentId);
    }
}
