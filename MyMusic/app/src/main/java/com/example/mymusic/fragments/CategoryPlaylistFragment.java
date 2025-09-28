package com.example.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CategoryPlaylistFragment extends Fragment {
    private TextView categoryPlaylistTittle,categoryPlaylistDescription;
    private ImageButton btnBack,btnAddLibrary,btnSuffle,btnPlay;
    private ImageView imgSong;
    private RecyclerView rcvCategoryPlaylist;
    private SongAdapter songAdapter;
    private MusicRepository musicRepository;
    private ArrayList<Song> songsList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static CategoryPlaylistFragment newInstance(String type, String value) {
        CategoryPlaylistFragment fragment = new CategoryPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("type", type);   // "artist" hoặc "genre"
        args.putString("value", value); // id hoặc tên
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_playlist, container, false);

        categoryPlaylistTittle = view.findViewById(R.id.tv_textbiggenre);
        categoryPlaylistDescription= view.findViewById(R.id.tv_textsmallgenre);
        btnBack = view.findViewById(R.id.btn_back);
        btnAddLibrary = view.findViewById(R.id.btn_addlibrary);
        btnSuffle = view.findViewById(R.id.btn_shuffle);
        btnPlay = view.findViewById(R.id.btn_play);
        imgSong = view.findViewById(R.id.img_song);
        rcvCategoryPlaylist = view.findViewById(R.id.rcv_category_playlist);
        rcvCategoryPlaylist.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(songsList);
        rcvCategoryPlaylist.setAdapter(songAdapter);

        if (getArguments() != null) {
            String type = getArguments().getString("type");
            String value = getArguments().getString("value");

            if ("artist".equals(type)) {
                loadSongsByArtist(value);
            } else if ("genre".equals(type)) {
                loadSongsByGenre(value);
            } else if ("playlist".equals(type)) {
                loadSongsByPlaylist(value);
            }
        }


        return view;
    }

    private void loadSongsByArtist(String artistId) {
        db.collection("Songs")
                .whereEqualTo("artistID", artistId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Song song = doc.toObject(Song.class);
                        songsList.add(song);
                    }
                    songAdapter.notifyDataSetChanged();
                });
    }
    private void loadSongsByGenre(String genre) {
        db.collection("Songs")
                .whereEqualTo("genre", genre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Song song = doc.toObject(Song.class);
                        songsList.add(song);
                    }
                    songAdapter.notifyDataSetChanged();
                });
    }
    private void loadSongsByPlaylist(String playlistId) {
        db.collection("Playlists") // chữ P hoa
                .document(playlistId)    // vd: "playlistTop100"
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Playlist playlist = doc.toObject(Playlist.class);

                        if (playlist != null) {
                            categoryPlaylistTittle.setText(playlist.getTitle());
                            categoryPlaylistDescription.setText(playlist.getDescription());

                            if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
                                fetchSongsFromIds(new ArrayList<>(playlist.getSongs()));
                            } else {
                                songsList.clear();
                                songAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        categoryPlaylistTittle.setText("Không tìm thấy playlist: " + playlistId);
                    }
                })
                .addOnFailureListener(e -> {
                    categoryPlaylistTittle.setText("Lỗi load playlist");
                    categoryPlaylistDescription.setText(e.getMessage());
                });
    }


    private void fetchSongsFromIds(ArrayList<String> songIds) {
        if (songIds == null || songIds.isEmpty()) return;

        db.collection("Songs")
                .whereIn(FieldPath.documentId(), songIds) // query theo list ID
                .get()
                .addOnSuccessListener(query -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setSongID(doc.getId());
                            songsList.add(song);
                        }
                    }
                    songAdapter.notifyDataSetChanged();
                });
    }

}
