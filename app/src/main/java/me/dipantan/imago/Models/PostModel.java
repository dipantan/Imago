package me.dipantan.imago.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PostModel {
    private String postText;
    private String author;
    private String date;
    private String authorIconUrl;
    private String postKey;
    private String postImage;

    public PostModel(String postText, String author, String date, String authorIconUrl, String postKey, String postImage) {
        this.postText = postText;
        this.author = author;
        this.date = date;
        this.authorIconUrl = authorIconUrl;
        this.postKey = postKey;
        this.postImage = postImage;
    }

    public PostModel() {
    }


    public String getPostText() {
        return postText;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    public String getPostKey() {
        return postKey;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setAuthorIconUrl(String authorIconUrl) {
        this.authorIconUrl = authorIconUrl;
    }
}
