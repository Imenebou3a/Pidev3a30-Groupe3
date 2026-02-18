package org.example.services;

import org.example.entities.ForumComment;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for forum comment CRUD and queries.
 */
public class ForumCommentService {
    private static final String TABLE = "forum_comment";

    public Optional<ForumComment> findById(int id) {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<ForumComment> findByPostId(int postId) {
        List<ForumComment> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " WHERE post_id = ? ORDER BY created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ForumComment create(ForumComment comment) {
        String sql = "INSERT INTO " + TABLE + " (post_id, user_id, parent_comment_id, content, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comment.getPostId());
            ps.setObject(2, comment.getAuthorId());
            ps.setObject(3, comment.getParentCommentId());
            ps.setString(4, comment.getContent());
            ps.setObject(5, comment.getCreatedAt());
            ps.setObject(6, comment.getUpdatedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) comment.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comment;
    }

    public boolean update(ForumComment comment) {
        if (comment.getId() == null) return false;
        String sql = "UPDATE " + TABLE + " SET content = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comment.getContent());
            comment.setUpdatedAt(LocalDateTime.now());
            ps.setObject(2, comment.getUpdatedAt());
            ps.setInt(3, comment.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static ForumComment mapRow(ResultSet rs) throws SQLException {
        ForumComment c = new ForumComment();
        c.setId(rs.getInt("id"));
        c.setPostId(rs.getInt("post_id"));
        c.setAuthorId(rs.getObject("user_id", Integer.class));  // fixed: was "author_id"
        c.setParentCommentId(rs.getObject("parent_comment_id", Integer.class));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        c.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return c;
    }
}