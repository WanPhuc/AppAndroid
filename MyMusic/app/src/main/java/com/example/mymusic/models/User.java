package com.example.mymusic.models;

import java.util.List;

public class User {
    private String UserID;
    private String username;
    private String email;
    private List<String> favorites;
    private List<String> playlists;
    private List<String> followingPlaylists;
    public User(){}
    public User(String UserID, String username, String email, List<String> favorites, List<String> playlists, List<String> followingPlaylists) {
        this.UserID = UserID;
        this.username = username;
        this.email = email;
        this.favorites = favorites;
        this.playlists = playlists;
        this.followingPlaylists = followingPlaylists;
    }
    public String getUserID() {return UserID;}
    public void setUserID(String UserID) {this.UserID = UserID;}
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public List<String> getFavorites() {return favorites;}
    public void setFavorites(List<String> favorites) {this.favorites = favorites;}
    public List<String> getPlaylists() {return playlists;}
    public void setPlaylists(List<String> playlists) {this.playlists = playlists;}
    public List<String> getFollowingPlaylists() {return followingPlaylists;}
    public void setFollowingPlaylists(List<String> followingPlaylists) {this.followingPlaylists = followingPlaylists;}
}
