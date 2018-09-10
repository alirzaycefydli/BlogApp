package com.example.alirz.blogapp;

import java.util.Date;

public class Post extends PostId{
     String user_id,image, description,date;


     public Post(){}

    public Post(String user_id, String image, String description, String date) {
        this.user_id = user_id;
        this.image = image;
        this.description = description;
        this.date = date;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
