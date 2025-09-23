package com.example.mymusic.models;

import java.util.List;

public class Playlist {
    private String playlistID;
    private String title;
    private String description;
    private String cover;
    private String type;        // user / system
    private String userId;
    private boolean isPublic;
    private String category;
    private String createdAt;
    private List<String> songs;

    public Playlist(){}
    public Playlist(String playlistID, String title, String description, String cover, String type, String userId, boolean isPublic, String category, String createdAt, List<String> songs){
        this.playlistID = playlistID;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.type = type;
        this.userId = userId;
        this.isPublic = isPublic;
        this.category = category;
        this.createdAt = createdAt;
        this.songs = songs;
    }
    public String getPlaylistID() {return playlistID;}
    public void setPlaylistID(String playlistID) {this.playlistID = playlistID;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getCover() {return cover;}
    public void setCover(String cover) {this.cover = cover;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    public boolean isPublic() {return isPublic;}
    public void setPublic(boolean isPublic) {this.isPublic = isPublic;}
    public void setCategory(String category) {this.category = category;}
    public String getCategory() {return category;}
    public String getCreatedAt() {return createdAt;}
    public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}
    public List<String> getSongs() {return songs;}
    public void setSongs(List<String> songs) {this.songs = songs;}

}
