package org.example.services;

import org.example.entities.ForumPost;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for forum post CRUD and queries.
 * Matches forum_post schema: id, user_id, type, related_entity_type, related_entity_id,
 * title, content, rating, status, is_moderated, created_at, updated_at, view_count, is_archived.
 */
public class ForumPostService {
    private static final String TABLE = "forum_post";

    public Optional<ForumPost> findById(int id) {
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

    public List<ForumPost> findAll() {
        List<ForumPost> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ForumPost create(ForumPost post) {
        if (post.getType() == null || post.getType().isBlank()) {
            post.setType("DISCUSSION");
        }
        String sql = "INSERT INTO " + TABLE + " (user_id, type, related_entity_type, related_entity_id, title, content, rating, status, is_moderated, created_at, updated_at, view_count, is_archived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, post.getUserId());
            ps.setString(2, post.getType());
            ps.setString(3, post.getRelatedEntityType());
            ps.setObject(4, post.getRelatedEntityId());
            ps.setString(5, post.getTitle());
            ps.setString(6, post.getContent());
            ps.setObject(7, post.getRating());
            ps.setString(8, post.getStatus());
            ps.setBoolean(9, post.isModerated());
            ps.setObject(10, post.getCreatedAt());
            ps.setObject(11, post.getUpdatedAt());
            ps.setObject(12, post.getViewCount());
            ps.setBoolean(13, post.isArchived());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) post.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    public boolean update(ForumPost post) {
        if (post.getId() == null) return false;
        String sql = "UPDATE " + TABLE + " SET user_id = ?, type = ?, related_entity_type = ?, related_entity_id = ?, title = ?, content = ?, rating = ?, status = ?, is_moderated = ?, updated_at = ?, view_count = ?, is_archived = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            post.setUpdatedAt(LocalDateTime.now());
            ps.setObject(1, post.getUserId());
            ps.setString(2, post.getType());
            ps.setString(3, post.getRelatedEntityType());
            ps.setObject(4, post.getRelatedEntityId());
            ps.setString(5, post.getTitle());
            ps.setString(6, post.getContent());
            ps.setObject(7, post.getRating());
            ps.setString(8, post.getStatus());
            ps.setBoolean(9, post.isModerated());
            ps.setObject(10, post.getUpdatedAt());
            ps.setObject(11, post.getViewCount());
            ps.setBoolean(12, post.isArchived());
            ps.setInt(13, post.getId());
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

    private static ForumPost mapRow(ResultSet rs) throws SQLException {
        ForumPost p = new ForumPost();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getObject("user_id", Integer.class));
        p.setType(rs.getString("type"));
        p.setRelatedEntityType(rs.getString("related_entity_type"));
        p.setRelatedEntityId(rs.getObject("related_entity_id", Integer.class));
        p.setTitle(rs.getString("title"));
        p.setContent(rs.getString("content"));
        p.setRating(rs.getObject("rating", Integer.class));
        p.setStatus(rs.getString("status"));
        p.setModerated(rs.getBoolean("is_moderated"));
        p.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        p.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        p.setViewCount(rs.getObject("view_count", Integer.class));
        p.setArchived(rs.getBoolean("is_archived"));
        return p;
    }
}
