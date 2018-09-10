package com.example.alirz.blogapp;

import java.util.Date;

public class Comments {

    String comment, user_id;
    Date timestamp;

    public Comments(){
    }

    public Comments(String comment, String user_id, Date timestamp) {
        this.comment = comment;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
