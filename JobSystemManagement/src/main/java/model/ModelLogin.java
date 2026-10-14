package model;

//1. Login.java
public class ModelLogin {
 private String userId;      // ユーザーID
 private String password;    // パスワード

 // デフォルトコンストラクタ
 public ModelLogin() {}

 // 全引数コンストラクタ
 public ModelLogin(String userId, String password) {
     this.userId = userId;
     this.password = password;
 }

 // Getter & Setter
 public String getUserId() { return userId; }
 public void setUserId(String userId) { this.userId = userId; }

 public String getPassword() { return password; }
 public void setPassword(String password) { this.password = password; }
}