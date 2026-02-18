package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.example.controllers.frontoffice.ForumPostFrontController;
import org.example.controllers.frontoffice.ForumCommentFrontController;
import org.example.controllers.frontoffice.ForumReactionFrontController;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane mainContainer;
    @FXML private Button btnPosts;
    @FXML private Button btnCreatePost;
    @FXML private Button btnBackoffice;
    @FXML private Button btnProfileMenu;

    private ContextMenu profileMenu;

    private ForumPostFrontController postController = new ForumPostFrontController();
    private ForumCommentFrontController commentController = new ForumCommentFrontController();
    private ForumReactionFrontController reactionController = new ForumReactionFrontController();

    @FXML
    public void initialize() {
        profileMenu = new ContextMenu();

        MenuItem itemPosts = new MenuItem("🗺 Explore Posts");
        itemPosts.setOnAction(e -> loadPostList());

        MenuItem itemCreate = new MenuItem("✍ Share Experience");
        itemCreate.setOnAction(e -> loadCreatePost());

        MenuItem itemAdmin = new MenuItem("⚙ Admin Panel");
        itemAdmin.setOnAction(e -> loadBackoffice());

        profileMenu.getItems().addAll(itemPosts, itemCreate, itemAdmin);

        btnProfileMenu.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                profileMenu.show(btnProfileMenu, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });

        loadPostList();
    }

    @FXML
    public void loadPostList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PostListView.fxml"));
            Parent postList = loader.load();
            PostListController controller = loader.getController();
            controller.setMainController(this);
            mainContainer.setCenter(postList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadCreatePost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePostView.fxml"));
            Parent createPost = loader.load();
            CreatePostController controller = loader.getController();
            controller.setMainController(this);
            mainContainer.setCenter(createPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadBackoffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BackofficeView.fxml"));
            Parent backoffice = loader.load();
            BackofficeController controller = loader.getController();
            controller.setMainController(this);
            mainContainer.setCenter(backoffice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPostDetail(int postId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PostDetailView.fxml"));
            Parent postDetail = loader.load();
            PostDetailController controller = loader.getController();
            controller.setPostId(postId);
            controller.setMainController(this);
            mainContainer.setCenter(postDetail);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BorderPane getMainContainer() {
        return mainContainer;
    }
}