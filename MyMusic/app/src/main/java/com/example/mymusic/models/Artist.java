package com.example.mymusic.models;

public class Artist {
    private String artistID;
    private String name;
    private String avatar;

    public Artist(){}
    public Artist(String artistID, String name, String avatar){
        this.artistID = artistID;
        this.name = name;
        this.avatar = avatar;
    }
    public String getArtistID() {return artistID;}
    public void setArtistID(String artistID) {this.artistID = artistID;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getAvatar() {return avatar;}
    public void setAvatar(String avatar) {this.avatar = avatar;}
}
