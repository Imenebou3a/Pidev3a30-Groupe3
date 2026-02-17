package org.example.controllers.frontoffice;

import org.example.entities.ForumReaction;
import org.example.services.ForumReactionService;

import java.util.List;

/**
 * Frontoffice controller for ForumReaction entity: user-facing operations (create, list reactions).
 */
public class ForumReactionFrontController {
    private final ForumReactionService reactionService = new ForumReactionService();

    public ForumReaction addReactionOnPost(int userId, int postId, ForumReaction.ReactionType type) {
        ForumReaction r = new ForumReaction(userId, postId, null, type);
        return reactionService.create(r);
    }

    public ForumReaction addReactionOnComment(int userId, int commentId, ForumReaction.ReactionType type) {
        ForumReaction r = new ForumReaction(userId, null, commentId, type);
        return reactionService.create(r);
    }

    public List<ForumReaction> getReactionsForPost(int postId) {
        return reactionService.findByPostId(postId);
    }

    public List<ForumReaction> getReactionsForComment(int commentId) {
        return reactionService.findByCommentId(commentId);
    }
}
