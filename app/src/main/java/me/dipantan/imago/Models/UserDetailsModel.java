package me.dipantan.imago.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserDetailsModel {
    private String name;
    private String email;
    private String photoUrl;

    public UserDetailsModel() {
    }

    public UserDetailsModel(String name, String email,String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl= photoUrl;
    }
//    public UserDetailsModel(String name, String email) {
//        this.name = name;
//        this.email = email;
//    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
