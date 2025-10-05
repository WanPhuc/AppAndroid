package com.example.mymusic.models;

import com.google.firebase.Timestamp;

public class LibraryItem {
    private String  id;
    private String title;
    private String  subtitle;
    private String imageUrl;
    private String type;
    private String userId;
    private boolean ispublic;
    private boolean pinned;
    private boolean isArtist;
    private boolean isPlaylist;
    private boolean isPlaylistFollowed;
    private Playlist playlist;
    private Artist artist; // Added for artist type
    private Timestamp createdAt;
    

    public LibraryItem(Playlist playlist) {
        this.id = playlist.getPlaylistID();
        this.title = playlist.getTitle();
        this.subtitle = "Playlist";
        this.imageUrl = "";
        this.type = playlist.getType();
        this.userId = playlist.getUserId();
        this.ispublic = playlist.getIspublic();
        this.pinned = playlist.isPinned();
        this.isPlaylist = true;
        this.isArtist = false;
        this.playlist = playlist;
        this.createdAt = playlist.getCreatedAt();
    }

    public LibraryItem(Artist artist) {
        this.id = artist.getArtistID();
        this.title = artist.getName();
        this.subtitle = "Nghệ sĩ";
        this.imageUrl = artist.getAvatar();
        this.isArtist = true;
        this.isPlaylist = false;
        this.artist = artist;
        this.type = "artist";
        this.createdAt = null; // Artists might not have a creation date in the same way
    }

    public LibraryItem(String id, String title, String subtitle, String imageUrl,String type,String userId,boolean ispublic, boolean pinned, boolean isArtist, boolean isPlaylist, boolean isPlaylistFollowed) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.type = type;
        this.userId = userId;
        this.ispublic = ispublic;
        this.pinned = pinned;
        this.isArtist = isArtist;
        this.isPlaylist = isPlaylist;
        this.isPlaylistFollowed = isPlaylistFollowed;
    }
    public String getId() {return id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getSubtitle() {return subtitle;}
    public String getImageUrl() {return imageUrl;}
    public String getType() {return type;}
    public boolean isArtist() {return isArtist;}
    public String getUserId() {return userId;}
    public boolean getIspublic() {return ispublic;}
    public void setIspublic(boolean ispub) {this.ispublic = ispub;}
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public boolean isPlaylist() {return isPlaylist;}
    public Playlist getPlaylist() {
        return playlist;
    }
    public Artist getArtist() { 
        return artist;
    }
    public boolean isPlaylistFollowed() {return isPlaylistFollowed;}
    public Timestamp getCreatedAt() { return createdAt; }
}
