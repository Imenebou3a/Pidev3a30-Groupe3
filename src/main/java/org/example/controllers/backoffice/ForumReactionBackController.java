package org.example.controllers.backoffice;

import org.example.entities.ForumReaction;
import org.example.services.ForumReactionService;

import java.util.List;

/**
 * Backoffice controller for ForumReaction entity: admin operations (list, delete reactions).
 */
public class ForumReactionBackController {
    private final ForumReactionService reactionService = new ForumReactionService();

    public List<ForumReaction> getReactionsForPost(int postId) {
        return reactionService.findByPostId(postId);
    }

    public List<ForumReaction> getReactionsForComment(int commentId) {
        return reactionService.findByCommentId(commentId);
    }

    public boolean deleteReaction(int reactionId) {
        return reactionService.delete(reactionId);
    }
}
