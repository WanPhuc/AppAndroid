package com.example.mymusic.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Playlist {
    private String playlistID;
    private String title;
    private String description;

    private String type;        // user / system
    private String userId;
    //@PropertyName("ispublic")
    private boolean ispublic;
    private boolean pinned;

    private Timestamp createdAt;
    private List<String> songs;

    public Playlist(){}
    public Playlist(String playlistID, String title, String description, String type, String userId, boolean ispublic, boolean pinned, Timestamp createdAt, List<String> songs){
        this.playlistID = playlistID;
        this.title = title;
        this.description = description;
        this.type = type;
        this.userId = userId;
        this.ispublic = ispublic;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.songs = songs;
    }
    @Exclude
    public String getPlaylistID() {return playlistID;}
    @Exclude
    public void setPlaylistID(String playlistID) {this.playlistID = playlistID;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    //@PropertyName("ispublic")
    public boolean getIspublic() {return ispublic;}
    //@PropertyName("ispublic")
    public void setIspublic(boolean ispub) {this.ispublic = ispub;}
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public Timestamp getCreatedAt() {return createdAt;}
    public void setCreatedAt(Timestamp createdAt) {this.createdAt = createdAt;}
    public List<String> getSongs() {return songs;}
    public void setSongs(List<String> songs) {this.songs = songs;}

}
