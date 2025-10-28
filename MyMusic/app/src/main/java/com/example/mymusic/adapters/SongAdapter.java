package com.example.mymusic.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mymusic.R;
import com.example.mymusic.SongDownloadManager;
import com.example.mymusic.activities.MainActivity;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.example.mymusic.services.MusicPlayerService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private final ArrayList<Song> songs;
    private final ArrayList<Playlist> playlists;
    private final HashMap<String, String> artistMap;
    private Context context;
    private OnSongClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // bài hát đang chọn
    private SongDownloadManager downloadManager;
    private boolean isDownloadedPlaylist = false;
    private boolean isFavoritesPlaylist = false; // Flag for favorites playlist
    private String currentPlaylistId; // To know which playlist is being displayed

    private static final int VIEW_TYPE_COMPACT = 0; // Dạng nhỏ (trang chủ, ngang)
    private static final int VIEW_TYPE_DETAIL = 1;  // Dạng đầy đủ (playlist, danh sách dọc)
    private boolean isCompactLayout = false; // flag chọn layout
    public MusicPlayerService musicPlayerService;
    private  boolean isServiceBound = false;
    public void setMusicPlayerService(MusicPlayerService service) {
        this.musicPlayerService = service;
    }
    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setDownloadedPlaylist(boolean downloadedPlaylist) {
        isDownloadedPlaylist = downloadedPlaylist;
    }

    public void setFavoritesPlaylist(boolean favoritesPlaylist) {
        isFavoritesPlaylist = favoritesPlaylist;
    }

    public void setCurrentPlaylistId(String playlistId) {
        this.currentPlaylistId = playlistId;
    }
    //hàm chuyển layout
    public void setCompactLayout(boolean compactLayout) {
        this.isCompactLayout = compactLayout;
        notifyDataSetChanged();
    }
    public SongAdapter(Context context, ArrayList<Song> songs, ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
        this.context = context;
        this.songs = songs;
        this.playlists = playlists;
        this.downloadManager = SongDownloadManager.getInstance(context);
        artistMap = new HashMap<>();
        for (Artist a : artists) artistMap.put(a.getArtistID(), a.getName());
    }

    @Nonnull
    @Override
    public SongViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_COMPACT) {
            View view = inflater.inflate(R.layout.item_song_grid, parent, false);
            return new CompactViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_song, parent, false);
            return new DetailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@Nonnull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        if (holder instanceof CompactViewHolder) {
            ((CompactViewHolder) holder).bind(song, artistMap, position);
        } else if (holder instanceof DetailViewHolder) {
            ((DetailViewHolder) holder).bind(song, artistMap, position);
        }
    }

    // ====== ViewType chọn XML theo chế độ true - dọc/ false - ngang ======
    @Override
    public int getItemViewType(int position) {
        return isCompactLayout ? VIEW_TYPE_COMPACT : VIEW_TYPE_DETAIL;
    }
    // ====== layout song doc ======
    class CompactViewHolder extends SongViewHolder {
        ImageView imgSong;
        TextView nameSong, nameArtist;

        CompactViewHolder(@Nonnull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.img_song);
            nameSong = itemView.findViewById(R.id.tv_nameSong);
            nameArtist = itemView.findViewById(R.id.tv_nameArtist);
        }

        void bind(Song song, HashMap<String, String> artistMap,int position ) {
            nameSong.setText(song.getTitle());
            nameArtist.setText(artistMap.getOrDefault(song.getArtistID(), "Unknown"));
            Glide.with(context)
                    .load(song.getCoverUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_default_song)
                    .into(imgSong);

            itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Bấm vào " + song.getTitle() , Toast.LENGTH_SHORT).show();
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                int prev = selectedPosition;
                selectedPosition = pos;

                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);

                if (listener != null) listener.onSongClick(song, pos);
                addToRecentlyPlayed(song);
            });
        }
    }

    // ====== layout song ngang ======
    class DetailViewHolder extends SongViewHolder {
        ImageView imgSong;
        TextView nameSong, nameArtist;
        ImageButton btnMore;

        DetailViewHolder(@Nonnull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.img_song);
            nameSong = itemView.findViewById(R.id.tv_nameSong);
            nameArtist = itemView.findViewById(R.id.tv_nameArtist);
            btnMore = itemView.findViewById(R.id.btn_more);
        }

        void bind(Song song, HashMap<String, String> artistMap, int position) {
            nameSong.setText(song.getTitle());
            nameArtist.setText(artistMap.getOrDefault(song.getArtistID(), "Unknown"));

            Glide.with(context)
                    .load(song.getCoverUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_default_song)
                    .into(imgSong);

            if (position == selectedPosition) {
                nameSong.setTextColor(context.getResources().getColor(R.color.select_song_play));
            } else {
                nameSong.setTextColor(context.getResources().getColor(android.R.color.white));
            }

            itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Bấm vào " + song.getTitle() , Toast.LENGTH_SHORT).show();
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                int prev = selectedPosition;
                selectedPosition = pos;

                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);

                if (listener != null) listener.onSongClick(song, pos);
                addToRecentlyPlayed(song);
            });

            btnMore.setOnClickListener(v -> showPopupMenu(v, song));
        }
    }

    private void showPopupMenu(View anchorView, Song song) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.menu_item_song, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView addToFavorites = sheetView.findViewById(R.id.btn_add_to_favorites);
        TextView addToPlaylist = sheetView.findViewById(R.id.btn_add_playlist);
        TextView shareSong = sheetView.findViewById(R.id.btn_share);
        TextView closeMenu = sheetView.findViewById(R.id.btn_close_menu);
        TextView downloadSong = sheetView.findViewById(R.id.btn_download_song);
        TextView removePlaylist = sheetView.findViewById(R.id.btn_remove_playlist);

        // Show/hide remove from playlist option
        if (currentPlaylistId != null && !isFavoritesPlaylist && !isDownloadedPlaylist) {
            removePlaylist.setVisibility(View.VISIBLE);
        } else {
            removePlaylist.setVisibility(View.GONE);
        }

        if (downloadManager.isSongDownloaded(song.getSongID())) {
            downloadSong.setText("🗑️ Xóa khỏi tải xuống");
        } else {
            downloadSong.setText("⬇️ Tải bài hát");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(currentUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getFavorites() != null && user.getFavorites().contains(song.getSongID())) {
                        addToFavorites.setText("💔 Gỡ khỏi bài hát yêu thích");
                    } else {
                        addToFavorites.setText("❤️ Thêm vào bài hát yêu thích");
                    }
                } else {
                    addToFavorites.setText("❤️ Thêm vào bài hát yêu thích");
                }
            }).addOnFailureListener(e -> {
                addToFavorites.setText("❤️ Thêm vào bài hát yêu thích");
            });
        } else {
            addToFavorites.setText("❤️ Thêm vào bài hát yêu thích");
        }

        addToFavorites.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            toggleFavoriteSong(song);
        });

        addToPlaylist.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showPlaylistDialog(song);
        });

        removePlaylist.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            removeSongFromCurrentPlaylist(song);
        });

        shareSong.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Toast.makeText(context, "Share " + song.getTitle(), Toast.LENGTH_SHORT).show();
            // ở đây ông có thể implement share intent
        });

        downloadSong.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (downloadManager.isSongDownloaded(song.getSongID())) {
                // Remove song from downloads
                downloadManager.removeDownloadedSongId(song.getSongID());

                // If we are in the downloaded playlist, remove it from the view
                if (isDownloadedPlaylist) {
                    int currentPosition = songs.indexOf(song);
                    if (currentPosition != -1) {
                        songs.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, songs.size());
                    }
                } else {
                    // Otherwise, just update the item to reflect the change (e.g., the icon in menu)
                    notifyDataSetChanged();
                }
                Toast.makeText(context, "Đã xóa bài hát khỏi danh sách tải xuống", Toast.LENGTH_SHORT).show();

            } else {
                // Download the song
                downloadManager.downloadSong(song);
                notifyDataSetChanged();
            }
        });

        closeMenu.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();

    }

    private void removeSongFromCurrentPlaylist(Song song) {
        if (currentPlaylistId == null || song == null || song.getSongID() == null) {
            return;
        }

        FirebaseFirestore.getInstance().collection("Playlists").document(currentPlaylistId)
                .update("songs", FieldValue.arrayRemove(song.getSongID()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã xóa bài hát khỏi playlist", Toast.LENGTH_SHORT).show();
                    int currentPosition = songs.indexOf(song);
                    if (currentPosition != -1) {
                        songs.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, songs.size());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void toggleFavoriteSong(Song song) {
        if (song == null || song.getSongID() == null) {
            Toast.makeText(context, "Lỗi: Không có thông tin bài hát.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String songId = song.getSongID();

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getFavorites() != null && user.getFavorites().contains(songId)) {
                    // Đã thích -> Bỏ thích
                    userRef.update("favorites", FieldValue.arrayRemove(songId))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã xóa khỏi Bài hát yêu thích", Toast.LENGTH_SHORT).show();
                                if (isFavoritesPlaylist) {
                                    int currentPosition = songs.indexOf(song);
                                    if (currentPosition != -1) {
                                        songs.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        notifyItemRangeChanged(currentPosition, songs.size());
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    // Chưa thích -> Thêm vào yêu thích
                    userRef.update("favorites", FieldValue.arrayUnion(songId))
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã thêm vào Bài hát yêu thích", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(context, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi kiểm tra danh sách yêu thích.", Toast.LENGTH_SHORT).show());
    }

    private void showPlaylistDialog(Song song) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Bạn cần đăng nhập để xem playlist", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();
        FirebaseFirestore.getInstance().collection("Playlists")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", "user")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Playlist> userPlaylists = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Playlist playlist = document.toObject(Playlist.class);
                        playlist.setPlaylistID(document.getId());
                        userPlaylists.add(playlist);
                    }

                    CharSequence[] playlistNames = new CharSequence[userPlaylists.size()];
                    boolean[] checkedItems = new boolean[userPlaylists.size()];

                    for (int i = 0; i < userPlaylists.size(); i++) {
                        Playlist p = userPlaylists.get(i);
                        playlistNames[i] = p.getTitle();
                        if (p.getSongs() != null) {
                            checkedItems[i] = p.getSongs().contains(song.getSongID());
                        } else {
                            checkedItems[i] = false;
                        }
                    }

                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Thêm vào playlist")
                            .setMultiChoiceItems(playlistNames, checkedItems, (dialog, which, isChecked) -> {
                                Playlist selected = userPlaylists.get(which);
                                if (isChecked) {
                                    addSongToSpecificPlaylist(song, selected);
                                } else {
                                    removeSongFromSpecificPlaylist(song, selected);
                                }
                            })
                            .setPositiveButton("Xong", (dialog, which) -> dialog.dismiss())
                            .setNeutralButton("➕ Tạo playlist mới", (dialog, which) -> {
                                showCreatePlaylistDialog(song);
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi tải danh sách playlist.", Toast.LENGTH_SHORT).show();
                });
    }


    private void addSongToSpecificPlaylist(Song song, Playlist playlist) {
        FirebaseFirestore.getInstance().collection("Playlists").document(playlist.getPlaylistID())
                .update("songs", FieldValue.arrayUnion(song.getSongID()))
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã thêm vào '" + playlist.getTitle() + "'", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeSongFromSpecificPlaylist(Song song, Playlist playlist) {
        FirebaseFirestore.getInstance().collection("Playlists").document(playlist.getPlaylistID())
                .update("songs", FieldValue.arrayRemove(song.getSongID()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã xóa khỏi '" + playlist.getTitle() + "'", Toast.LENGTH_SHORT).show();

                    // Nếu playlist vừa được cập nhật là playlist đang hiển thị,
                    // hãy xóa bài hát khỏi danh sách và cập nhật RecyclerView.
                    if (playlist.getPlaylistID().equals(currentPlaylistId)) {
                        int currentPosition = songs.indexOf(song);
                        if (currentPosition != -1) {
                            songs.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            notifyItemRangeChanged(currentPosition, songs.size());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void addToRecentlyPlayed(Song song) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getUid();
        if (userID == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("songID", song.getSongID());
        data.put("timestamp", FieldValue.serverTimestamp()); // thời gian nghe

        db.collection("Users")
                .document(userID)
                .collection("RecentlyPlayed")
                .document(song.getSongID())  // dùng songID làm key → tránh trùng
                .set(data, SetOptions.merge()); // chỉ update timestamp nếu đã tồn tại
    }
    private void showCreatePlaylistDialog(Song song) {
        EditText input = new EditText(context);
        input.setHint("Nhập tên playlist");

        new android.app.AlertDialog.Builder(context)
                .setTitle("Tạo playlist mới")
                .setView(input)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(context, "Tên playlist không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (song == null || song.getSongID() == null) {
                        Toast.makeText(context, "Lỗi: Không có thông tin bài hát để thêm.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = currentUser.getUid();

                    String playlistId = FirebaseFirestore.getInstance().collection("Playlists").document().getId();
                    HashMap<String, Object> newPlaylist = new HashMap<>();
                    newPlaylist.put("title", name);
                    newPlaylist.put("description", "");
                    newPlaylist.put("ispublic", false);
                    newPlaylist.put("songs", Arrays.asList(song.getSongID()));
                    newPlaylist.put("createdAt", FieldValue.serverTimestamp());
                    newPlaylist.put("type", "user");
                    newPlaylist.put("userId", uid);

                    FirebaseFirestore.getInstance().collection("Playlists")
                            .document(playlistId)
                            .set(newPlaylist)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(context, "Đã tạo và thêm bài hát vào playlist '" + name + "'", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Tạo thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView nameSong, nameArtist;
        ImageButton btnMore;

        public SongViewHolder(@Nonnull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.img_song);
            nameSong = itemView.findViewById(R.id.tv_nameSong);
            nameArtist = itemView.findViewById(R.id.tv_nameArtist);
            btnMore = itemView.findViewById(R.id.btn_more);
        }

    }

    public void updateArtists(ArrayList<Artist> artists) {
        artistMap.clear();
        for (Artist a : artists) {
            artistMap.put(a.getArtistID(), a.getName());
        }
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int prev = selectedPosition;
        selectedPosition = position;

        if (prev != RecyclerView.NO_POSITION) {
            notifyItemChanged(prev);
        }
        notifyItemChanged(selectedPosition);
    }
    public void bindSongView(SongViewHolder holder, Song song, int position, MusicPlayerService musicPlayerService) {
        Song currentSong = song;
        // Set tên bài hát
        holder.nameSong.setText(song.getTitle());

        // Lấy tên ca sĩ từ artistMap dựa theo artistID
        String artistName = artistMap.get(song.getArtistID());
        holder.nameArtist.setText(artistName != null ? artistName : "Unknown");

        // Load ảnh bài hát
        Glide.with(context)
                .load(song.getCoverUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_default_song)
                .into(holder.imgSong);

        // Đổi màu bài hát đang phát
        if (position == selectedPosition) {
            holder.nameSong.setTextColor(context.getResources().getColor(R.color.select_song_play));
        } else {
            holder.nameSong.setTextColor(context.getResources().getColor(android.R.color.white));
        }

        // Xử lý click bài hát
        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            int prev = selectedPosition;
            selectedPosition = pos;

            if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSongClick(song, pos);
            Toast.makeText(context, "bài " + song.getTitle(), Toast.LENGTH_SHORT).show();
            if (musicPlayerService == null) {
                Toast.makeText(context, "khong co musicPlayerService: ", Toast.LENGTH_SHORT).show();
                return;
            }
            musicPlayerService.stop();
            musicPlayerService.playSong(song);
            if (context instanceof MainActivity) {
                ((MainActivity) context).showMiniPlayer(song);
            }

        });
    }
}
