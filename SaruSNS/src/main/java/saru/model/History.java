package saru.model;

import java.time.LocalDateTime;

public class History {
    private int user_id;
    private int post_id;
    private LocalDateTime viewed_at;
    private int postTyp;

    public History() {}
    
    public History(int user_id,int post_id,LocalDateTime viewed_at,int postTyp) {
    		this.user_id = user_id;
    		this.post_id = post_id;
    		this.viewed_at = viewed_at;
    		this.postTyp = postTyp;
    }
    
    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }
    
    public int getPost_id() { return post_id ; }
    public void setPost_id (int post_id  ) { this.post_id  = post_id ; }
    
    public LocalDateTime getViewed_at() { return viewed_at; }
    public void setViewed_at(LocalDateTime viewed_at) { this.viewed_at = viewed_at; }
    
    public int getPostTyp () { return postTyp ; }
    public void set(int postTyp ) { this.postTyp  =postTyp  ; }

}
