package com.example.mymusic.models;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {


    private String songID;
    private String title;
    private String artistID;
    private String genre;
    private String coverUrl;
    private String fileUrl;
    private String year;
    private int duration;
    private String lyrics;
    private Artist artist;
    public Song(){}

    public Song(String songID, Artist artist, String title, String artistID, String genre, String coverUrl, String fileUrl, String year, int duration, String lyrics){
        this.songID = songID;
        this.title = title;
        this.artistID = artistID;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.fileUrl = fileUrl;
        this.year = year;
        this.duration = duration;
        this.lyrics = lyrics;
        this.artist = artist;
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
    public int getDuration() {return duration;}
    public void setDuration(int duration) {this.duration = duration;}
    public String getLyrics() {return lyrics;}
    public void setLyrics(String lyrics) {this.lyrics = lyrics;}

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songID, song.songID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songID);
    }

}
