package com.example.mymusic.models;

import java.util.List;

public class User {
    private String UserID;
    private String name;
    private String email;
    private List<String> favorites;
    private List<String> playlists;
    private List<String> followingPlaylists;
    public User(){}
    public User(String UserID, String name, String email, List<String> favorites, List<String> playlists, List<String> followingPlaylists) {
        this.UserID = UserID;
        this.name = name;
        this.email = email;
        this.favorites = favorites;
        this.playlists = playlists;
        this.followingPlaylists = followingPlaylists;
    }
    public String getUserID() {return UserID;}
    public void setUserID(String UserID) {this.UserID = UserID;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public List<String> getFavorites() {return favorites;}
    public void setFavorites(List<String> favorites) {this.favorites = favorites;}
    public List<String> getPlaylists() {return playlists;}
    public void setPlaylists(List<String> playlists) {this.playlists = playlists;}
    public List<String> getFollowingPlaylists() {return followingPlaylists;}
    public void setFollowingPlaylists(List<String> followingPlaylists) {this.followingPlaylists = followingPlaylists;}
}
