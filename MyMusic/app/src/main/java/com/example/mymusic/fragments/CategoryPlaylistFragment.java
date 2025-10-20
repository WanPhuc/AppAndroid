package com.example.mymusic.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.SongDownloadManager;
import com.example.mymusic.activities.MainActivity;
import com.example.mymusic.adapters.MainAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.example.mymusic.repository.MusicRepository;
import com.example.mymusic.services.MusicPlayerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CategoryPlaylistFragment extends Fragment implements MusicPlayerService.PlayerListener {
    private TextView categoryPlaylistTittle, categoryPlaylistDescription, tvNameArtist;
    private ImageButton btnBack, btnAddLibrary, btnShuffle, btnPlay;
    private ImageView imgSong, imgHeaderBg;
    private RecyclerView rcvCategoryPlaylist;

    private SongAdapter songAdapter;
    private ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<Playlist> playList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MusicRepository musicRepository = new MusicRepository();
    private boolean isFollowing = false; // trạng thái follow
    private boolean isDownloadedPlaylist = false;
    private boolean isFavoritesPlaylist = false; // Flag for favorites playlist

    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.addPlayerListener(CategoryPlaylistFragment.this);

            if (!artistList.isEmpty()) {
                musicPlayerService.setArtists(artistList);
            }
            isServiceBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };


    public static CategoryPlaylistFragment newInstance(String type, String value) {
        CategoryPlaylistFragment fragment = new CategoryPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("type", type);   // "artist", "genre", "playlist", "favorites", "downloads"
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
        categoryPlaylistDescription = view.findViewById(R.id.tv_textsmallgenre);
        btnBack = view.findViewById(R.id.btn_back);
        btnAddLibrary = view.findViewById(R.id.btn_addlibrary);
        btnShuffle = view.findViewById(R.id.btn_shuffle);
        btnPlay = view.findViewById(R.id.btn_play);
        imgSong = view.findViewById(R.id.img_song);
        imgHeaderBg = view.findViewById(R.id.img_header_bg);
        tvNameArtist = view.findViewById(R.id.tv_nameArtist);

        rcvCategoryPlaylist = view.findViewById(R.id.rcv_category_playlist);
        rcvCategoryPlaylist.setLayoutManager(new LinearLayoutManager(getContext()));

        songAdapter = new SongAdapter(getContext(), songsList, playList, artistList);
        rcvCategoryPlaylist.setAdapter(songAdapter);

        musicRepository.listenAllArtists(artists -> {
            artistList.clear();
            artistList.addAll(artists);
            songAdapter.updateArtists(artistList);
            if (isServiceBound) {
                musicPlayerService.setArtists(artistList);
            }

            if (getArguments() != null) {
                String type = getArguments().getString("type");
                String value = getArguments().getString("value");

                isDownloadedPlaylist = "downloads".equals(type);
                isFavoritesPlaylist = "favorites".equals(type);
                songAdapter.setDownloadedPlaylist(isDownloadedPlaylist);
                songAdapter.setFavoritesPlaylist(isFavoritesPlaylist);
                if ("playlist".equals(type)) {
                    songAdapter.setCurrentPlaylistId(value);
                }

                if ("artist".equals(type)) {
                    loadSongsByArtist(value);
                } else if ("genre".equals(type)) {
                    loadSongsByGenre(value);
                } else if ("playlist".equals(type)) {
                    loadSongsByPlaylist(value);
                } else if ("favorites".equals(type)) {
                    loadFavoriteSongs();
                } else if ("downloads".equals(type)) {
                    loadDownloadedSongs();
                }
            }
        });

        songAdapter.setOnSongClickListener((song, position) -> {
            if (!isServiceBound) return;

            List<Song> currentServicePlaylist = musicPlayerService.getOriginalSongs();
            boolean isDifferentPlaylist = currentServicePlaylist == null
                    || currentServicePlaylist.isEmpty()
                    || currentServicePlaylist.size() != songsList.size()
                    || !currentServicePlaylist.get(0).getSongID().equals(songsList.get(0).getSongID());

            if (isDifferentPlaylist) {
                // 🛑 Stop playlist cũ
                musicPlayerService.stop();
                // 🔁 Set playlist mới
                musicPlayerService.setSongs(new ArrayList<>(songsList));
            }

            // ▶️ Phát bài được click
            musicPlayerService.playSong(song);
            songAdapter.setSelectedPosition(position);

            //hiện mini playẻr
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMiniPlayer(song);
            }

            updateUI();
            // Preload the next song's image
            if (position + 1 < songsList.size()) {
                preloadImage(songsList.get(position + 1).getCoverUrl());
            }
        }
    );



        btnPlay.setOnClickListener(v -> {
            if (isServiceBound && !songsList.isEmpty()) {
                List<Song> currentServicePlaylist = musicPlayerService.getOriginalSongs();
                boolean isDifferentPlaylist = currentServicePlaylist == null
                        || currentServicePlaylist.isEmpty()
                        || currentServicePlaylist.size() != songsList.size()
                        || !currentServicePlaylist.get(0).getSongID().equals(songsList.get(0).getSongID());

                if (isDifferentPlaylist) {
                    // 🛑 Stop playlist cũ
                    musicPlayerService.stop();
                    // 🔄 Set playlist mới
                    musicPlayerService.setSongs(new ArrayList<>(songsList));
                    // ▶️ Phát bài đầu tiên trong playlist mới
                    musicPlayerService.playSong(songsList.get(0));
                    songAdapter.setSelectedPosition(0);
                } else {
                    // 🔁 Nếu đang ở playlist cũ thì chỉ toggle play/pause
                    musicPlayerService.playPause();
                }

                updateUI();
            }
        });


        btnShuffle.setOnClickListener(v -> {
            if (isServiceBound && !songsList.isEmpty()) {
                if (musicPlayerService.getCurrentSong() == null) {
                    musicPlayerService.setSongs(songsList);
                }
                musicPlayerService.toggleShuffle();
                btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
            }
        });

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnAddLibrary.setOnClickListener(v -> {
            if (getArguments() == null) return;
            String type = getArguments().getString("type");
            String value = getArguments().getString("value");

            if ("playlist".equals(type)) {
                toggleFollow(value); // follow playlist
            } else if ("artist".equals(type)) {
                toggleFollowArtist(value); // follow artist
            }
        });


        return view;
    }

    private void preloadImage(String url) {
        if (getContext() != null) {
            Glide.with(getContext())
                .load(url)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .preload();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isServiceBound) {
        }
    }

    private void loadFavoriteSongs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem mục yêu thích", Toast.LENGTH_SHORT).show();
            songsList.clear();
            songAdapter.notifyDataSetChanged();
            return;
        }
        String currentUserId = currentUser.getUid();
        categoryPlaylistTittle.setVisibility(View.VISIBLE);
        categoryPlaylistTittle.setText("Bài hát yêu thích");
        categoryPlaylistDescription.setText("Danh sách các bài hát bạn đã thích");
        btnAddLibrary.setVisibility(View.GONE);
        tvNameArtist.setVisibility(View.GONE);
        Glide.with(requireContext()).load(R.drawable.img_favorit).into(imgHeaderBg);

        db.collection("Users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getFavorites() != null && !user.getFavorites().isEmpty()) {
                    fetchSongsFromIds(user.getFavorites());
                } else {
                    songsList.clear();
                    songAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi khi tải bài hát yêu thích", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDownloadedSongs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem mục đã tải", Toast.LENGTH_SHORT).show();
            songsList.clear();
            songAdapter.notifyDataSetChanged();
            return;
        }

        categoryPlaylistTittle.setVisibility(View.VISIBLE);
        categoryPlaylistTittle.setText("Bài hát đã tải");
        categoryPlaylistDescription.setText("Danh sách các bài hát bạn đã tải về");
        btnAddLibrary.setVisibility(View.GONE);
        tvNameArtist.setVisibility(View.GONE);
        Glide.with(requireContext()).load(R.drawable.img_download).into(imgHeaderBg);

        SongDownloadManager downloadManager = SongDownloadManager.getInstance(getContext());
        List<String> downloadedSongIds = downloadManager.getDownloadedSongIds();

        if (downloadedSongIds.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có bài hát nào được tải", Toast.LENGTH_SHORT).show();
            songsList.clear();
            songAdapter.notifyDataSetChanged();
            return;
        }

        fetchSongsFromIds(downloadedSongIds);
    }

    // --------------------- FOLLOW / UNFOLLOW -----------------------
    private void toggleFollow(String playlistId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để theo dõi playlist", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();

        if (isFollowing) {
            // Đang follow -> bỏ follow
            db.collection("Users").document(currentUserId)
                    .update("followingPlaylists", FieldValue.arrayRemove(playlistId))
                    .addOnSuccessListener(a -> {
                        isFollowing = false;
                        btnAddLibrary.setImageResource(R.drawable.ic_add_library);
                        Toast.makeText(getContext(), "Đã bỏ theo dõi playlist", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi khi bỏ theo dõi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Chưa follow -> thêm follow
            db.collection("Users").document(currentUserId)
                    .update("followingPlaylists", FieldValue.arrayUnion(playlistId))
                    .addOnSuccessListener(a -> {
                        isFollowing = true;
                        btnAddLibrary.setImageResource(R.drawable.ic_check);
                        Toast.makeText(getContext(), "Đã theo dõi playlist", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi khi thêm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    // --------------------- FOLLOW / UNFOLLOW ARTIST -----------------------
    private void toggleFollowArtist(String artistId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để theo dõi nghệ sĩ", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();

        if (isFollowing) {
            // đang follow -> bỏ follow
            db.collection("Users").document(currentUserId)
                    .update("followingArtists", FieldValue.arrayRemove(artistId))
                    .addOnSuccessListener(a -> {
                        isFollowing = false;
                        btnAddLibrary.setImageResource(R.drawable.ic_add_library);
                        Toast.makeText(getContext(), "Đã bỏ theo dõi nghệ sĩ", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi khi bỏ theo dõi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // chưa follow -> thêm follow
            db.collection("Users").document(currentUserId)
                    .update("followingArtists", FieldValue.arrayUnion(artistId))
                    .addOnSuccessListener(a -> {
                        isFollowing = true;
                        btnAddLibrary.setImageResource(R.drawable.ic_check);
                        Toast.makeText(getContext(), "Đã theo dõi nghệ sĩ", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi khi thêm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }


    // --------------------- LOAD DATA -----------------------
    private void loadSongsByArtist(String artistId) {
        tvNameArtist.setVisibility(View.VISIBLE);
        categoryPlaylistTittle.setVisibility(View.GONE);
        categoryPlaylistDescription.setText("Nghệ sĩ");
        String artistName = "Nghệ sĩ không xác định";
        for (Artist artist : artistList) {
            if (artist.getArtistID().equals(artistId)) {
                artistName = artist.getName();
                if (artist.getAvatar() != null && getContext() != null) {
                    Glide.with(requireContext())
                            .load(artist.getAvatar())
                            .placeholder(R.drawable.img_default_song) // ảnh mặc định nếu lỗi
                            .into(imgHeaderBg);
                }
                break;
            }
        }

        tvNameArtist.setText(artistName);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db.collection("Users").document(currentUserId).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            ArrayList<String> following = (ArrayList<String>) userDoc.get("followingArtists");
                            if (following != null && following.contains(artistId)) {
                                isFollowing = true;
                                btnAddLibrary.setVisibility(View.VISIBLE);
                                btnAddLibrary.setImageResource(R.drawable.ic_check);
                            } else {
                                isFollowing = false;
                                btnAddLibrary.setVisibility(View.VISIBLE);
                                btnAddLibrary.setImageResource(R.drawable.ic_add_library);
                            }
                        }
                    });
        } else {
            btnAddLibrary.setVisibility(View.GONE);
        }
        // load songs of artist
        db.collection("Songs")
                .whereEqualTo("artistID", artistId)
                .get()
                .addOnSuccessListener(query -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setSongID(doc.getId());
                            songsList.add(song);
                        }
                    }
                    if (isServiceBound && musicPlayerService != null) {
                        musicPlayerService.setSongs(songsList);
                    }
                    songAdapter.notifyDataSetChanged();
                });
        db.collection("songs")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Log.d("Firestore", doc.getId() + " => " + doc.getData());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error: " + e.getMessage());
                });
    }


    private void loadSongsByGenre(String genre) {
        tvNameArtist.setVisibility(View.GONE);
        categoryPlaylistTittle.setVisibility(View.VISIBLE);
        btnAddLibrary.setVisibility(View.GONE);
        categoryPlaylistTittle.setText(genre);
        categoryPlaylistDescription.setText("Danh sách các bài hát thuộc thể loại " + genre);
        imgHeaderBg.setVisibility(View.GONE);

        db.collection("Songs")
                .whereEqualTo("genre", genre)
                .get()
                .addOnSuccessListener(query -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setSongID(doc.getId());
                            songsList.add(song);
                        }
                    }
                    if (isServiceBound && musicPlayerService != null) {
                        musicPlayerService.setSongs(songsList);
                    }
                    songAdapter.notifyDataSetChanged();
                });
    }

    private void loadSongsByPlaylist(String playlistId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : null;

        db.collection("Playlists").document(playlistId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Playlist playlist = doc.toObject(Playlist.class);
                        if (playlist != null) {
                            categoryPlaylistTittle.setVisibility(View.VISIBLE);
                            categoryPlaylistTittle.setText(playlist.getTitle());
                            categoryPlaylistDescription.setText(playlist.getDescription());
                            tvNameArtist.setVisibility(View.GONE);
                            Glide.with(requireContext()).load(R.drawable.img_default_playlist).into(imgHeaderBg);

                            if (currentUserId != null && currentUserId.equals(playlist.getUserId())) {
                                btnAddLibrary.setVisibility(View.GONE); // playlist của mình
                            } else {
                                btnAddLibrary.setVisibility(View.VISIBLE);
                                if (currentUserId != null) {
                                    db.collection("Users").document(currentUserId).get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (userDoc.exists()) {
                                                    ArrayList<String> following = (ArrayList<String>) userDoc.get("followingPlaylists");

                                                    if (following != null && following.contains(playlistId)) {
                                                        isFollowing = true;
                                                        btnAddLibrary.setImageResource(R.drawable.ic_check);
                                                    } else {
                                                        isFollowing = false;
                                                        btnAddLibrary.setImageResource(R.drawable.ic_add_library);
                                                    }
                                                }
                                            });
                                } else {
                                    btnAddLibrary.setImageResource(R.drawable.ic_add_library);
                                    isFollowing = false;
                                }
                            }

                            if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
                                fetchSongsFromIds(playlist.getSongs());
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

    private void fetchSongsFromIds(List<String> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            songsList.clear();
            songAdapter.notifyDataSetChanged();

            // 🧹 Clear playlist trong service nếu rỗng
            if (isServiceBound && musicPlayerService != null) {
                musicPlayerService.setSongs(new ArrayList<>());
            }
            return;
        }

        db.collection("Songs")
                .whereIn(FieldPath.documentId(), songIds)
                .get()
                .addOnSuccessListener(query -> {
                    songsList.clear();
                    for (DocumentSnapshot doc : query) {
                        Song song = doc.toObject(Song.class);
                        if (song != null) {
                            song.setSongID(doc.getId());
                            songsList.add(song);
                        }
                    }
                    songAdapter.notifyDataSetChanged();
                    // 🔥 Chỉ set playlist 1 lần duy nhất khi load thành công
                    if (isServiceBound && musicPlayerService != null) {
                        // Kiểm tra nếu playlist mới khác playlist cũ thì mới set lại
                        if (musicPlayerService.getCurrentPlaylistSize() != songsList.size()) {
                            musicPlayerService.setSongs(new ArrayList<>(songsList));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchSongsFromIds", "Lỗi load bài hát từ playlist: ", e);
                });
    }


    @Override
    public void onSongChanged(Song song) {
        updateUI();
    }

    @Override
    public void onPlayerStateChanged(boolean isPlaying) {
        updateUI();
    }

    @Override
    public void onPlaylistChanged(List<Song> newPlaylist) {
        if (musicPlayerService != null) {
            List<Song> activeList = musicPlayerService.getTransientSongs() != null
                    ? musicPlayerService.getTransientSongs()
                    : newPlaylist; // ưu tiên transient nếu có
            songsList.clear();
            songsList.addAll(activeList);
            songAdapter.notifyDataSetChanged();
            updateUI();
        }
    }

    private void updateUI() {
        if (!isServiceBound || musicPlayerService == null) return;

        Song currentSong = musicPlayerService.getCurrentSong();
        if (currentSong != null) {
            int position = songsList.indexOf(currentSong);
            songAdapter.setSelectedPosition(position);

            if (getContext() != null && imgSong != null && currentSong.getCoverUrl() != null) {
                Glide.with(requireContext())
                        .load(currentSong.getCoverUrl())
                        .placeholder(R.drawable.img_default_song)
                        .into(imgSong);
            }
        } else {
            songAdapter.setSelectedPosition(-1);
            if (getContext() != null && imgSong != null) {
                Glide.with(requireContext())
                        .load(R.drawable.img_default_song)
                        .into(imgSong);
            }
        }

        btnPlay.setImageResource(musicPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
    }
}
