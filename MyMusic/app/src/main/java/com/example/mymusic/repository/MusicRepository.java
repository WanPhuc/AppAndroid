package com.example.mymusic.repository;

import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MusicRepository {
    private final FirebaseFirestore db;
    private final CollectionReference songsRef;
    private final CollectionReference artistsRef;
    private final CollectionReference playlistsRef;
    private final CollectionReference usersRef;

    public MusicRepository(){
        db = FirebaseFirestore.getInstance();
        songsRef = db.collection("Songs");
        artistsRef = db.collection("Artists");
        playlistsRef = db.collection("Playlists");
        usersRef = db.collection("Users");
    }
    // -------- Songs --------
    public void getAllSongs(OnDataLoadedListener<Song> listener){
        songsRef.get().addOnSuccessListener(query ->{
            ArrayList<Song> songList = new ArrayList<>();
            for (DocumentSnapshot doc:query.getDocuments()){
                Song item=doc.toObject(Song.class);
                if (item!=null)item.setSongID(doc.getId());
                songList.add(item);
            }
            listener.onDataLoaded(songList);
        });
    }
    // -------- Genres (Songs) --------
    public void getAllGenres(OnDataLoadedListener<String> listener) {
        songsRef.get().addOnSuccessListener(query -> {
            ArrayList<String> genres = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                String genre = doc.getString("genre");
                if (genre != null) {
                    String cleanGenre = genre.trim(); // bỏ khoảng trắng
                    if (!genres.contains(cleanGenre)) {
                        genres.add(cleanGenre);
                    }
                }
            }
            listener.onDataLoaded(genres);
        });
    }

    // -------- Artists --------
    public void getAllArtists(OnDataLoadedListener<Artist> listener){
        artistsRef.get().addOnSuccessListener(query ->{
            ArrayList<Artist> artistList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()){
                Artist item = doc.toObject(Artist.class);
                if (item!= null)item.setArtistID(doc.getId());
                artistList.add(item);
            }
            listener.onDataLoaded(artistList);
        });
    }
    // -------- Playlists --------
    public void getAllPlaylists(OnDataLoadedListener<Playlist> listener){
        playlistsRef.get().addOnSuccessListener(query ->{
            ArrayList<Playlist> playlistList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()){
                Playlist item = doc.toObject(Playlist.class);
                if (item!= null)item.setPlaylistID(doc.getId());
                playlistList.add(item);
            }
            listener.onDataLoaded(playlistList);
        });
    }
    // -------- Users --------
    public void getAllUsers(OnDataLoadedListener<User> listener){
        usersRef.get().addOnSuccessListener(query ->{
            ArrayList<User> userList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                User item = doc.toObject(User.class);
                if (item != null) item.setUserID(doc.getId());
                userList.add(item);
            }
            listener.onDataLoaded(userList);
        });
    }
    // -------- Interface callback chung --------
    public interface OnDataLoadedListener<T> {
        void onDataLoaded(ArrayList<T> data);
    }


}
