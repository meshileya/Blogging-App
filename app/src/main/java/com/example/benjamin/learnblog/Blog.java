package com.example.benjamin.learnblog;

import android.net.Uri;

/**
 * Created by Benjamin on 12/13/2017.
 */

public class Blog {
    public String image, title, content, username, ownerDp;
    public Blog() {
    }

    public Blog(String image, String title, String content, String username, String ownerDp) {
        this.image = image;
        this.title = title;
        this.content = content;
        this.username = username;
        this.ownerDp = ownerDp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOwnerDp() {
        return ownerDp;
    }

    public void setOwnerDp(String ownerDp) {
        this.ownerDp = ownerDp;
    }
}
