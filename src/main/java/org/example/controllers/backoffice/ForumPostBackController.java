package org.example.controllers.backoffice;

import org.example.entities.ForumPost;
import org.example.services.ForumPostService;

import java.util.List;
import java.util.Optional;

/**
 * Backoffice controller for ForumPost entity: admin operations (list, get, delete posts).
 */
public class ForumPostBackController {
    private final ForumPostService postService = new ForumPostService();

    public List<ForumPost> listAllPosts() {
        return postService.findAll();
    }

    public Optional<ForumPost> getPost(int postId) {
        return postService.findById(postId);
    }

    public boolean deletePost(int postId) {
        return postService.delete(postId);
    }
}
