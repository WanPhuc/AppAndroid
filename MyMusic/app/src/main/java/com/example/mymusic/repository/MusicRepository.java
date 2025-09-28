package com.example.mymusic.repository;

import android.util.Log;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MusicRepository {

    private final FirebaseFirestore db;
    private final CollectionReference songsRef, artistsRef, playlistsRef, usersRef;

    public MusicRepository() {
        db = FirebaseFirestore.getInstance();
        songsRef = db.collection("Songs");
        artistsRef = db.collection("Artists");
        playlistsRef = db.collection("Playlists");
        usersRef = db.collection("Users");
    }

    // ======== Generic Methods ========
    private <T> void getAll(CollectionReference ref, Class<T> clazz, OnDataLoadedListener<T> listener) {
        ref.get().addOnSuccessListener(query -> listener.onDataLoaded(parseDocuments(query.getDocuments(), clazz)));
    }

    private <T> void getAllRealtime(CollectionReference ref, Class<T> clazz, OnDataLoadedListener<T> listener) {
        ref.addSnapshotListener((query, e) -> {
            if (e != null) { Log.w("Firestore", "Listen failed.", e); return; }
            if (query != null) listener.onDataLoaded(parseDocuments(query.getDocuments(), clazz));
        });
    }

    private <T> ArrayList<T> parseDocuments(List<DocumentSnapshot> docs, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            T item = doc.toObject(clazz);
            if (item != null) {
                if (item instanceof Song) ((Song) item).setSongID(doc.getId());
                else if (item instanceof Artist) ((Artist) item).setArtistID(doc.getId());
                else if (item instanceof Playlist) ((Playlist) item).setPlaylistID(doc.getId());
                else if (item instanceof User) ((User) item).setUserID(doc.getId());
                list.add(item);
            }
        }
        return list;
    }

    private ArrayList<String> extractGenres(List<DocumentSnapshot> docs) {
        HashSet<String> set = new HashSet<>();
        for (DocumentSnapshot doc : docs) {
            String genre = doc.getString("genre");
            if (genre != null) set.add(genre.trim());
        }
        return new ArrayList<>(set);
    }

    // ======== Songs ========
    public void getAllSongs(OnDataLoadedListener<Song> listener) { getAll(songsRef, Song.class, listener); }
    public void getAllSongsRealtime(OnDataLoadedListener<Song> listener) { getAllRealtime(songsRef, Song.class, listener); }

    // ======== Genres ========
    public void getAllGenres(OnDataLoadedListener<String> listener) {
        songsRef.get().addOnSuccessListener(query -> listener.onDataLoaded(extractGenres(query.getDocuments())));
    }

    public void getAllGenresRealtime(OnDataLoadedListener<String> listener) {
        songsRef.addSnapshotListener((query, e) -> {
            if (e != null) { Log.w("Firestore", "Listen failed.", e); return; }
            if (query != null) listener.onDataLoaded(extractGenres(query.getDocuments()));
        });
    }

    // ======== Artists ========
    public void getAllArtists(OnDataLoadedListener<Artist> listener) { getAll(artistsRef, Artist.class, listener); }
    public void getAllArtistsRealtime(OnDataLoadedListener<Artist> listener) { getAllRealtime(artistsRef, Artist.class, listener); }

    // ======== Playlists ========
    public void getAllPlaylists(OnDataLoadedListener<Playlist> listener) { getAll(playlistsRef, Playlist.class, listener); }
    public void getAllPlaylistsRealtime(OnDataLoadedListener<Playlist> listener) { getAllRealtime(playlistsRef, Playlist.class, listener); }

    // Playlists theo type
    public void getAllPlaylistsByType(OnDataLoadedListener<Playlist> listener) {
        playlistsRef.get().addOnSuccessListener(query -> {
            ArrayList<Playlist> list = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                Playlist item = doc.toObject(Playlist.class);
                if (item != null) {
                    item.setPlaylistID(doc.getId());
                    String type = doc.getString("type");
                    if ("system".equals(type)) list.add(item);
                    else if ("user".equals(type) && Boolean.TRUE.equals(doc.getBoolean("isPublic"))) list.add(item);
                }
            }
            listener.onDataLoaded(list);
        });
    }

    // ======== Users ========
    public void getAllUsers(OnDataLoadedListener<User> listener) { getAll(usersRef, User.class, listener); }
    public void getAllUsersRealtime(OnDataLoadedListener<User> listener) { getAllRealtime(usersRef, User.class, listener); }

    // ======== Callback Interface ========
    public interface OnDataLoadedListener<T> {
        void onDataLoaded(ArrayList<T> data);
    }
}
