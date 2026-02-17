package org.example.controllers.frontoffice;

import org.example.entities.ForumPost;
import org.example.services.ForumPostService;

import java.util.List;
import java.util.Optional;

/**
 * Frontoffice controller for ForumPost entity: user-facing operations (list, view, create, update posts).
 */
public class ForumPostFrontController {
    private final ForumPostService postService = new ForumPostService();

    public List<ForumPost> listPosts() {
        return postService.findAll();
    }

    public Optional<ForumPost> getPost(int postId) {
        return postService.findById(postId);
    }

    public ForumPost createPost(String title, String content, int userId) {
        ForumPost post = new ForumPost(title, content, userId);
        return postService.create(post);
    }

    /** Create post with required type (AVIS, RECLAMATION, RECOMMANDATION, DISCUSSION) and optional rating. */
    public ForumPost createPost(String title, String content, int userId, String type, Integer rating) {
        ForumPost post = new ForumPost(title, content, userId);
        post.setType(type != null && !type.isBlank() ? type : "DISCUSSION");
        if (rating != null) post.setRating(rating);
        return postService.create(post);
    }

    public boolean updatePost(ForumPost post) {
        return postService.update(post);
    }
}
