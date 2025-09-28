package com.example.mymusic.models;

public class SearchItem {
    private String id;        // SongID hoặc ArtistID
    private String mainText;  // Tên bài hát hoặc tên nghệ sĩ
    private String subText;   // "Nghệ sĩ" hoặc "Bài hát - ..."
    private String imageUrl;  // coverUrl hoặc avatar
    private boolean isArtist; // true = artist, false = song

    public SearchItem(String id, String mainText, String subText, String imageUrl, boolean isArtist) {
        this.id = id;
        this.mainText = mainText;
        this.subText = subText;
        this.imageUrl = imageUrl;
        this.isArtist = isArtist;
    }

    public String getId() {
        return id;
    }

    public String getMainText() {
        return mainText;
    }

    public String getSubText() {
        return subText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isArtist() {
        return isArtist;
    }
}

