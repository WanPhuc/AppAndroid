package com.example.mymusic.models;

public class Song {
    private String songID;
    private String title;
    private String artistID;
    private String genre;
    private String coverUrl;
    private String fileUrl;
    private String year;
    private String duration;
    private String lyrics;

    public Song(){}
    public Song(String songID, String title, String artistID, String genre, String coverUrl, String fileUrl, String year, String duration, String lyrics){
        this.songID = songID;
        this.title = title;
        this.artistID = artistID;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.fileUrl = fileUrl;
        this.year = year;
        this.duration = duration;
        this.lyrics = lyrics;
    }
    public String getSongID() {return songID;}
    public void setSongID(String songID) {this.songID = songID;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getArtistID() {return artistID;}
    public void setArtistID(String artistID) {this.artistID = artistID;}
    public String getGenre() {return genre;}
    public void setGenre(String genre) {this.genre = genre;}
    public String getCoverUrl() {return coverUrl;}
    public void setCoverUrl(String coverUrl) {this.coverUrl = coverUrl;}
    public String getFileUrl() {return fileUrl;}
    public void setFileUrl(String fileUrl) {this.fileUrl = fileUrl;}
    public String getYear() {return year;}
    public void setYear(String year) {this.year = year;}
    public String getDuration() {return duration;}
    public void setDuration(String duration) {this.duration = duration;}
    public String getLyrics() {return lyrics;}
    public void setLyrics(String lyrics) {this.lyrics = lyrics;}
}
