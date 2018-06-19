package com.example.lenovo.ronixtask;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lenovo on 6/9/2018.
 */

public class User {
    @SerializedName("SSID")
    private String userName;

    public User(String userName) {
        this.userName = userName;

    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
