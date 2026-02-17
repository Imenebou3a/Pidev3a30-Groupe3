package org.example.services;

import org.example.entities.ForumReaction;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for forum reactions (on posts or comments).
 */
public class ForumReactionService {
    private static final String TABLE = "forum_reaction";

    public Optional<ForumReaction> findById(int id) {
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

    public List<ForumReaction> findByPostId(int postId) {
        List<ForumReaction> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " WHERE post_id = ?";
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

    public List<ForumReaction> findByCommentId(int commentId) {
        List<ForumReaction> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " WHERE comment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ForumReaction create(ForumReaction reaction) {
        String sql = "INSERT INTO " + TABLE + " (user_id, post_id, comment_id, type, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reaction.getUserId());
            ps.setObject(2, reaction.getPostId());
            ps.setObject(3, reaction.getCommentId());
            ps.setString(4, reaction.getType().name());
            ps.setObject(5, reaction.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) reaction.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reaction;
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

    private static ForumReaction mapRow(ResultSet rs) throws SQLException {
        ForumReaction r = new ForumReaction();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setPostId(rs.getObject("post_id", Integer.class));
        r.setCommentId(rs.getObject("comment_id", Integer.class));
        r.setType(ForumReaction.ReactionType.valueOf(rs.getString("type")));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return r;
    }
}
