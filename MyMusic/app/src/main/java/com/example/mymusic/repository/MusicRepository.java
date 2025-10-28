package com.example.mymusic.repository;

import android.util.Log;

import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class MusicRepository {
    private final FirebaseFirestore db;
    private final CollectionReference songsRef;
    private final CollectionReference artistsRef;
    private final CollectionReference playlistsRef;
    private final CollectionReference usersRef;

    public MusicRepository() {
        db = FirebaseFirestore.getInstance();
        songsRef = db.collection("Songs");
        artistsRef = db.collection("Artists");
        playlistsRef = db.collection("Playlists");
        usersRef = db.collection("Users");
    }

    // -------- Songs --------
    public void listenAllSongs(OnDataLoadedListener<Song> listener) {
        songsRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            ArrayList<Song> songList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                Song item = doc.toObject(Song.class);
                if (item != null) item.setSongID(doc.getId());
                songList.add(item);
            }
            listener.onDataLoaded(songList);
        });
    }
    public void listenRecentlyPlayed(OnDataLoadedListener<Song> listener) {
        String userID = FirebaseAuth.getInstance().getUid();
        if (userID == null) return;

        usersRef.document(userID)
                .collection("RecentlyPlayed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((query, e) -> {
                    if (e != null || query == null) return;

                    ArrayList<String> songIDs = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String songID = doc.getString("songID");
                        if (songID != null) songIDs.add(songID);
                    }

                    // load Song objects từ songID
                    ArrayList<Song> result = new ArrayList<>();
                    for (String id : songIDs) {
                        songsRef.document(id).get().addOnSuccessListener(songDoc -> {
                            Song song = songDoc.toObject(Song.class);
                            if (song != null) {
                                song.setSongID(songDoc.getId());
                                result.add(song);
                                listener.onDataLoaded(result); // update UI dần
                            }
                        });
                    }
                });
    }
    public void getTop3RecentlyPlayed(OnDataLoadedListener<Song> listener) {
        String userID = FirebaseAuth.getInstance().getUid();
        if (userID == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(userID)
                .collection("RecentlyPlayed")
                .orderBy("timestamp", Query.Direction.DESCENDING) // bài nghe mới nhất trước
                .limit(3) // chỉ lấy 3 bài
                .get()
                .addOnSuccessListener(query -> {
                    ArrayList<Song> result = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String songID = doc.getString("songID");
                        if (songID == null) continue;

                        // Lấy song chi tiết từ Songs collection
                        db.collection("Songs")
                                .document(songID)
                                .get()
                                .addOnSuccessListener(songDoc -> {
                                    Song song = songDoc.toObject(Song.class);
                                    if (song != null) {
                                        song.setSongID(songDoc.getId());
                                        result.add(song);
                                        listener.onDataLoaded(result); // cập nhật UI dần
                                    }
                                });
                    }
                });
    }
    // -------- Genres (Songs) --------
    public void listenAllGenres(OnDataLoadedListener<String> listener) {
        songsRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            ArrayList<String> genres = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                String genre = doc.getString("genre");
                if (genre != null) {
                    String cleanGenre = genre.trim();
                    if (!genres.contains(cleanGenre)) {
                        genres.add(cleanGenre);
                    }
                }
            }
            listener.onDataLoaded(genres);
        });
    }

    // -------- Artists --------
    public void listenAllArtists(OnDataLoadedListener<Artist> listener) {
        artistsRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            ArrayList<Artist> artistList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                Artist item = doc.toObject(Artist.class);
                if (item != null) item.setArtistID(doc.getId());
                artistList.add(item);
            }
            listener.onDataLoaded(artistList);
        });
    }

    // -------- Playlists --------
    public void listenAllPlaylists(OnDataLoadedListener<Playlist> listener) {
        playlistsRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            ArrayList<Playlist> playlistList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                Playlist item = doc.toObject(Playlist.class);
                if (item != null) item.setPlaylistID(doc.getId());
                playlistList.add(item);
            }
            listener.onDataLoaded(playlistList);
        });
    }

    public void listenPlaylistsByType(OnDataLoadedListener<Playlist> listener) {
        playlistsRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            ArrayList<Playlist> playlistList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                String type = doc.getString("type");
                if (type != null) {
                    String cleanType = type.trim();
                    Playlist item = doc.toObject(Playlist.class);
                    if (item != null) {
                        item.setPlaylistID(doc.getId());

                        if (cleanType.equals("system")) {
                            playlistList.add(item);
                        } else if (cleanType.equals("user")) {
                            Boolean isPublic = doc.getBoolean("ispublic");
                            if (isPublic != null && isPublic) {
                                playlistList.add(item);
                            }
                        }
                    }
                }
            }
            listener.onDataLoaded(playlistList);
        });
    }

    public void addPlaylist(Playlist playlist, OnPlaylistAddedListener listener) {
        // Tạo document rỗng trước
        DocumentReference docRef = playlistsRef.document();

        // Gắn playlistID local từ docRef.getId()
        playlist.setPlaylistID(docRef.getId());

        // Đẩy data lên Firestore (sẽ KHÔNG lưu field playlistID vì đã @Exclude)
        docRef.set(playlist)
                .addOnSuccessListener(a -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("MusicRepository", "Lỗi khi thêm playlist", e);
                    if (listener != null) listener.onComplete(false);
                });
    }



    // -------- Users --------
    public void listenAllUsers(OnDataLoadedListener<User> listener) {
        usersRef.addSnapshotListener((query, e) -> {
            if (e != null || query == null) {
                Log.e("MusicRepository", "Error getting users", e);
                listener.onDataLoaded(null);
                return;
            }

            ArrayList<User> userList = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                User item = doc.toObject(User.class);
                if (item != null) {
                    item.setUserID(doc.getId());
                    userList.add(item);
                }
            }
            listener.onDataLoaded(userList);
        });
    }

    // -------- Interface callback chung --------
    public interface OnDataLoadedListener<T> {
        void onDataLoaded(ArrayList<T> data);
    }

    public interface OnPlaylistAddedListener {
        void onComplete(boolean isSuccess);
    }
}
