package org.example.test;

import org.example.controllers.backoffice.ForumPostBackController;
import org.example.controllers.frontoffice.ForumPostFrontController;
import org.example.controllers.frontoffice.ForumCommentFrontController;
import org.example.controllers.frontoffice.ForumReactionFrontController;
import org.example.entities.ForumPost;
import org.example.entities.ForumComment;
import org.example.entities.ForumReaction;
import org.example.utils.DatabaseConnection;

import java.util.List;
import java.util.Scanner;

/**
 * Simple CLI to test forum operations (database name: pidev).
 * Run: mvn compile exec:java -Dexec.mainClass="org.example.test.ForumTestCLI"
 * Or set mainClass in pom.xml and run the JAR.
 */
public class ForumTestCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ForumPostFrontController postFrontController = new ForumPostFrontController();
    private static final ForumCommentFrontController commentFrontController = new ForumCommentFrontController();
    private static final ForumReactionFrontController reactionFrontController = new ForumReactionFrontController();
    private static final ForumPostBackController postBackController = new ForumPostBackController();

    public static void main(String[] args) {
        System.out.println("=== Forum Test CLI (database: pidev) ===");
        try {
            DatabaseConnection.getConnection();
            System.out.println("Connected to database 'pidev'.");
        } catch (Exception e) {
            System.err.println("Cannot connect to database: " + e.getMessage());
            System.err.println("Ensure MySQL is running and database 'pidev' exists with tables: forum_post, forum_comment, forum_reaction");
            return;
        }

        loop:
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listPosts();
                case "2" -> createPost();
                case "3" -> viewPostAndComments();
                case "4" -> addComment();
                case "5" -> addReaction();
                case "6" -> listPostsBackoffice();
                case "0" -> {
                    break loop;
                }
                default -> System.out.println("Unknown option.");
            }
        }

        DatabaseConnection.closeConnection();
        System.out.println("Bye.");
    }

    private static void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. List posts (front)");
        System.out.println("2. Create post");
        System.out.println("3. View post & comments");
        System.out.println("4. Add comment");
        System.out.println("5. Add reaction on post");
        System.out.println("6. List posts (backoffice)");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private static void listPosts() {
        List<ForumPost> posts = postFrontController.listPosts();
        if (posts.isEmpty()) {
            System.out.println("No posts.");
            return;
        }
        for (ForumPost p : posts) {
            System.out.println(p.getId() + " | " + p.getTitle() + " | by " + p.getUserId());
        }
    }

    private static void createPost() {
        System.out.print("User ID: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());
        System.out.println("Type (1=AVIS, 2=RECLAMATION, 3=RECOMMANDATION, 4=DISCUSSION): ");
        int typeChoice;
        try {
            typeChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            typeChoice = 4;
        }
        String type;
        switch (typeChoice) {
            case 1: type = "AVIS"; break;
            case 2: type = "RECLAMATION"; break;
            case 3: type = "RECOMMANDATION"; break;
            case 4: type = "DISCUSSION"; break;
            default: type = "DISCUSSION";
        }
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Content: ");
        String content = scanner.nextLine().trim();
        System.out.print("Rating (1-5, or 0 to skip): ");
        Integer rating;
        try {
            int r = Integer.parseInt(scanner.nextLine().trim());
            rating = (r >= 1 && r <= 5) ? r : null;
        } catch (NumberFormatException e) {
            rating = null;
        }
        ForumPost created = postFrontController.createPost(title, content, userId, type, rating);
        System.out.println("Created: " + created);
    }

    private static void viewPostAndComments() {
        System.out.print("Post ID: ");
        int postId = Integer.parseInt(scanner.nextLine().trim());
        postFrontController.getPost(postId).ifPresentOrElse(
                post -> {
                    System.out.println("Post: " + post.getTitle());
                    System.out.println(post.getContent());
                    List<ForumComment> comments = commentFrontController.getCommentsForPost(postId);
                    System.out.println("Comments: " + comments.size());
                    for (ForumComment c : comments) {
                        System.out.println("  " + c.getId() + " | " + c.getContent() + " (by " + c.getAuthorId() + ")");
                    }
                },
                () -> System.out.println("Post not found.")
        );
    }

    private static void addComment() {
        System.out.print("Post ID: ");
        int postId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Author ID: ");
        int authorId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Content: ");
        String content = scanner.nextLine().trim();
        ForumComment c = commentFrontController.addComment(postId, authorId, content);
        System.out.println("Comment created: " + c.getId());
    }

    private static void addReaction() {
        System.out.print("User ID: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Post ID: ");
        int postId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Type (LIKE, LOVE, HELPFUL, SAD, ANGRY): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        try {
            ForumReaction.ReactionType type = ForumReaction.ReactionType.valueOf(typeStr);
            ForumReaction r = reactionFrontController.addReactionOnPost(userId, postId, type);
            System.out.println("Reaction created: " + r);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid reaction type.");
        }
    }

    private static void listPostsBackoffice() {
        List<ForumPost> posts = postBackController.listAllPosts();
        if (posts.isEmpty()) {
            System.out.println("No posts.");
            return;
        }
        for (ForumPost p : posts) {
            System.out.println(p.getId() + " | " + p.getTitle() + " | status=" + p.getStatus());
        }
    }
}
