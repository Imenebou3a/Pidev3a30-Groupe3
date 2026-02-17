package org.example.controllers.frontoffice;

import org.example.entities.ForumComment;
import org.example.services.ForumCommentService;

import java.util.List;

/**
 * Frontoffice controller for ForumComment entity: user-facing operations (list, create comments).
 */
public class ForumCommentFrontController {
    private final ForumCommentService commentService = new ForumCommentService();

    public List<ForumComment> getCommentsForPost(int postId) {
        return commentService.findByPostId(postId);
    }

    public ForumComment addComment(int postId, int authorId, String content) {
        return addComment(postId, authorId, null, content);
    }

    public ForumComment addComment(int postId, int authorId, Integer parentCommentId, String content) {
        ForumComment comment = new ForumComment(postId, authorId, content);
        comment.setParentCommentId(parentCommentId);
        return commentService.create(comment);
    }
}
