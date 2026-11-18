package saru.model;

import java.time.LocalDateTime;

public class CommentLike {
    private int comment_id;
    private int user_id;
    private LocalDateTime created_at;

    public CommentLike() {}

    public CommentLike(int comment_id, int user_id, LocalDateTime created_at) {
        this.comment_id = comment_id;
        this.user_id = user_id;
        this.created_at = created_at;
    }

    public int getComment_id() { return comment_id; }
    public void setComment_id(int comment_id) { this.comment_id = comment_id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
}