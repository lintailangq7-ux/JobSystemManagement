package saru.model;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int post_id;
    private int user_id;
    private String body;
    private LocalDateTime created_at;

    public Comment() {}

    public Comment(int id, int post_id, int user_id, String body, LocalDateTime created_at) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.body = body;
        this.created_at = created_at;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPost_id() { return post_id; }
    public void setPost_id(int post_id) { this.post_id = post_id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
}