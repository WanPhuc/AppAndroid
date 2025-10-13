package com.example.mymusic.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.SongDownloadManager;
import com.example.mymusic.fragments.CategoryPlaylistFragment;
import com.example.mymusic.models.LibraryItem;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {
    private ArrayList<LibraryItem> items;
    private Map<String, User> usersMap;
    private static final String TAG = "LibraryAdapter";
    private static final String PREFS_NAME = "MyMusicPrefs";
    private static final String FAVORITES_PINNED_KEY = "FavoritesPinned";

    public interface OnPinStateChangeListener {
        void onPinStateChanged();
    }

    private OnPinStateChangeListener pinStateChangeListener;

    public void setOnPinStateChangeListener(OnPinStateChangeListener listener) {
        this.pinStateChangeListener = listener;
    }


    public LibraryAdapter(ArrayList<LibraryItem> items, Map<String, User> usersMap) {
        this.items = items;
        this.usersMap = usersMap;
    }

    @Override
    public LibraryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder holder, int position) {
        LibraryItem item = items.get(position);
        holder.tvnameitem.setText(item.getTitle());

        holder.imgPin.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);

        if (item.isArtist()) {
            holder.imgitemartist.setVisibility(View.VISIBLE);
            holder.imgitemplaylist.setVisibility(View.GONE);
            holder.tvsecond.setText("Nghệ Sĩ");
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.artist)
                        .into(holder.imgitemartist);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.artist)
                        .circleCrop()
                        .into(holder.imgitemartist);
            }
        } else if (item.isPlaylist()) {
            holder.imgitemplaylist.setVisibility(View.VISIBLE);
            holder.imgitemartist.setVisibility(View.GONE);
            Playlist playlist = item.getPlaylist();
            if (playlist != null) {
                if ("system".equals(playlist.getType())) {
                    holder.tvsecond.setText("MayMusic");
                    Glide.with(holder.itemView.getContext()).load(R.drawable.ic_apple_music_icon).into(holder.imgitemplaylist);
                } else if ("favorites".equals(playlist.getType())) {
                    User user = usersMap.get(playlist.getUserId());
                    int count = 0;
                    if (user != null && user.getFavorites() != null) {
                        count = user.getFavorites().size();
                    }
                    holder.tvsecond.setText("Danh sách phát - " + count + " bài hát");
                    Glide.with(holder.itemView.getContext()).load(R.drawable.img_favorit).into(holder.imgitemplaylist);
                } else if ("downloads".equals(playlist.getType())) {
                    SongDownloadManager downloadManager = SongDownloadManager.getInstance(holder.itemView.getContext());
                    int count = downloadManager.getDownloadedSongIds().size();
                    holder.tvsecond.setText("Danh sách phát - " + count + " bài hát");
                    Glide.with(holder.itemView.getContext()).load(R.drawable.img_download).into(holder.imgitemplaylist);
                } else {
                    User playlistOwner = usersMap.get(playlist.getUserId());
                    if (playlistOwner != null && playlistOwner.getUsername() != null) {
                        holder.tvsecond.setText("Danh sách phát - " + playlistOwner.getUsername());
                    } else {
                        holder.tvsecond.setText("Danh sách phát");
                    }
                    Glide.with(holder.itemView.getContext()).load(R.drawable.img_default_playlist).into(holder.imgitemplaylist);
                }
            }
        } else {
            holder.imgitemplaylist.setVisibility(View.VISIBLE); // Default to playlist view if type is unknown
            holder.imgitemartist.setVisibility(View.GONE);
            holder.tvsecond.setText(item.getSubtitle());
            Glide.with(holder.itemView.getContext()).load(R.drawable.ic_apple_music_icon).into(holder.imgitemplaylist);
        }

        holder.btnmore.setOnClickListener(v -> btnMoreClick(v, item, position));

        holder.itemView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            ViewPager2 viewPager = activity.findViewById(R.id.vp_fragmain);
            CategoryPlaylistFragment fragment = new CategoryPlaylistFragment();
            FrameLayout container = activity.findViewById(R.id.container_main);
            if (viewPager != null && viewPager.getAdapter() instanceof MainAdapter) {
                Bundle args = new Bundle();
                Playlist playlist = item.getPlaylist();
                if (playlist != null && "favorites".equals(playlist.getType())) {
                    args.putString("type", "favorites");
                } else if (playlist != null && "downloads".equals(playlist.getType())) {
                    args.putString("type", "downloads");
                } else if (item.isPlaylist()) {
                    args.putString("type", "playlist");
                } else if (item.isArtist()) {
                    args.putString("type", "artist");
                }
                args.putString("value", item.getId());
                fragment.setArguments(args);

                activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container_main,fragment)
                                        .addToBackStack(null)
                                                .commit();
                viewPager.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "ViewPager or MainAdapter not found or incorrect type");
            }
        });
    }

    private void btnMoreClick(View v, LibraryItem item, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.menu_item_library);

        Playlist playlist = item.getPlaylist();

        MenuItem pinMenuItem = popup.getMenu().findItem(R.id.pin);

        if (playlist != null && "favorites".equals(playlist.getType())) {
            popup.getMenu().findItem(R.id.rename).setVisible(false);
            popup.getMenu().findItem(R.id.ispublic).setVisible(false);
            popup.getMenu().findItem(R.id.delete).setVisible(false);
            popup.getMenu().findItem(R.id.unfollow).setVisible(false);
            pinMenuItem.setVisible(true);
        } else if (playlist!=null&&"downloads".equals(playlist.getType())){
            popup.getMenu().findItem(R.id.rename).setVisible(false);
            popup.getMenu().findItem(R.id.ispublic).setVisible(false);
            popup.getMenu().findItem(R.id.delete).setVisible(false);
            popup.getMenu().findItem(R.id.unfollow).setVisible(false);
            pinMenuItem.setVisible(false);
        }else if (item.isArtist()) {
            popup.getMenu().findItem(R.id.rename).setVisible(false);
            popup.getMenu().findItem(R.id.ispublic).setVisible(false);
            popup.getMenu().findItem(R.id.delete).setVisible(false);
            popup.getMenu().findItem(R.id.unfollow).setVisible(true);
            pinMenuItem.setVisible(false);
        } else if (item.isPlaylist()) {
            if (playlist != null) {
                if ("system".equals(playlist.getType())) {
                    popup.getMenu().findItem(R.id.rename).setVisible(false);
                    popup.getMenu().findItem(R.id.ispublic).setVisible(false);
                    popup.getMenu().findItem(R.id.delete).setVisible(false);
                    popup.getMenu().findItem(R.id.unfollow).setVisible(true);
                    pinMenuItem.setVisible(false);
                }
                else if (!playlist.getUserId().equals(currentUser.getUid())) {
                    popup.getMenu().findItem(R.id.rename).setVisible(false);
                    popup.getMenu().findItem(R.id.ispublic).setVisible(false);
                    popup.getMenu().findItem(R.id.delete).setVisible(false);
                    popup.getMenu().findItem(R.id.unfollow).setVisible(true);
                    pinMenuItem.setVisible(false);
                }
                else {
                    popup.getMenu().findItem(R.id.unfollow).setVisible(false);
                    popup.getMenu().findItem(R.id.rename).setVisible(true);
                    popup.getMenu().findItem(R.id.ispublic).setVisible(true);
                    popup.getMenu().findItem(R.id.delete).setVisible(true);
                    pinMenuItem.setVisible(true);
                    if (item.isPinned()) {
                        pinMenuItem.setTitle("Bỏ ghim");
                    } else {
                        pinMenuItem.setTitle("Ghim danh sách");
                    }
                    if (playlist.getIspublic()) {
                        popup.getMenu().findItem(R.id.ispublic).setTitle("Bỏ công khai");
                    } else {
                        popup.getMenu().findItem(R.id.ispublic).setTitle("Công khai");
                    }
                }
            }
        }

        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.pin) {
                if (item.isPlaylist()) togglePin(v.getContext(), item, menuItem);
                return true;
            } else if (id == R.id.rename) {
                if (item.isPlaylist()) showDialogRename(v.getContext(), item, position);
                return true;
            } else if (id == R.id.ispublic) {
                if (item.isPlaylist()) togglePublic(v.getContext(), item, position, menuItem);
                return true;
            } else if (id == R.id.delete) {
                if (item.isPlaylist()) showDialogDelete(v.getContext(), item, position);
                return true;
            } else if (id == R.id.unfollow) {
                if (item.isPlaylist()) {
                    unFollowPlaylist(v.getContext(), item, position);
                } else if (item.isArtist()) {
                    unFollowArtist(v.getContext(), item, position);
                }
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void togglePin(Context context, LibraryItem item, MenuItem menuItem) {
        Playlist playlist = item.getPlaylist();
        if (playlist == null) return;

        boolean newState = !item.isPinned();
        if ("favorites".equals(playlist.getType())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(FAVORITES_PINNED_KEY, newState).apply();
            item.setPinned(newState);
            playlist.setPinned(newState);
            if (newState) {
                Toast.makeText(context, "Đã ghim danh sách", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Đã bỏ ghim danh sách", Toast.LENGTH_SHORT).show();
            }
            if (pinStateChangeListener != null) {
                pinStateChangeListener.onPinStateChanged();
            }
        } else {
            FirebaseFirestore.getInstance()
                    .collection("Playlists")
                    .document(item.getId())
                    .update("pinned", newState)
                    .addOnSuccessListener(a -> {
                        item.setPinned(newState);
                        playlist.setPinned(newState);

                        if (newState) {
                            Toast.makeText(context, "Đã ghim danh sách", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Đã bỏ ghim danh sách", Toast.LENGTH_SHORT).show();
                        }

                        if (pinStateChangeListener != null) {
                            pinStateChangeListener.onPinStateChanged();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
    private void reorderItems() {
        // Tách danh sách ghim và chưa ghim
        ArrayList<LibraryItem> pinned = new ArrayList<>();
        ArrayList<LibraryItem> others = new ArrayList<>();
        for (LibraryItem item : items) {
            if (item.isPinned()) {
                pinned.add(item);
            } else {
                others.add(item);
            }
        }
        // Ghép lại: pinned ở đầu, rồi mới đến others
        items.clear();
        items.addAll(pinned);
        items.addAll(others);

        notifyDataSetChanged();
    }

    private void showDialogRename(Context context, LibraryItem item, int position) {
        EditText input = new EditText(context);
        input.setHint("Nhập tên mới");
        new AlertDialog.Builder(context)
                .setTitle("Đổi tên danh sách phát")
                .setView(input)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        FirebaseFirestore.getInstance()
                                .collection("Playlists")
                                .document(item.getId())
                                .update("title", newName)
                                .addOnSuccessListener(a -> {
                                    item.setTitle(newName);
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Đổi tên thành công", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void togglePublic(Context context, LibraryItem item, int position, MenuItem menuItem) {
        Playlist playlist = item.getPlaylist();
        if (playlist == null) return;

        boolean newState = !playlist.getIspublic();
        FirebaseFirestore.getInstance()
                .collection("Playlists")
                .document(item.getId())
                .update("ispublic", newState)
                .addOnSuccessListener(a -> {
                    playlist.setIspublic(newState);
                    item.setIspublic(newState);
                    notifyItemChanged(position);

                    if (newState) {
                        menuItem.setTitle("Bỏ công khai");
                        Toast.makeText(context, "Playlist đã công khai", Toast.LENGTH_SHORT).show();
                    } else {
                        menuItem.setTitle("Công khai");
                        Toast.makeText(context, "Playlist đã bỏ công khai", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showDialogDelete(Context context, LibraryItem item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa danh sách phát")
                .setMessage("Bạn có chắc muốn xóa Playlist?")
                .setPositiveButton("Có", (d, w) -> {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) return;
                    FirebaseFirestore.getInstance()
                            .collection("Playlists")
                            .document(item.getId())
                            .delete()
                            .addOnSuccessListener(a -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(currentUser.getUid())
                                        .update("playlists", FieldValue.arrayRemove(item.getId()))
                                        .addOnSuccessListener(b -> {
                                            items.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, items.size());
                                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Không", (d, w) -> d.dismiss())
                .show();
    }

    private void unFollowPlaylist(Context context, LibraryItem item, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .update("followingPlaylists", FieldValue.arrayRemove(item.getId()))
                .addOnSuccessListener(b -> {
                    items.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, items.size());
                    Toast.makeText(context, "Bỏ theo dõi playlist thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi update playlists của user: ", e));
    }

    private void unFollowArtist(Context context, LibraryItem item, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .update("followingArtists", FieldValue.arrayRemove(item.getId()))
                .addOnSuccessListener(b -> {
                    items.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, items.size());
                    Toast.makeText(context, "Bỏ theo dõi nghệ sĩ thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi update artists của user: ", e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder {
        TextView tvnameitem, tvsecond;
        ImageView imgitemplaylist,imgPin;
        ShapeableImageView imgitemartist;
        AppCompatButton btnmore;

        public LibraryViewHolder(View itemView) {
            super(itemView);
            tvnameitem = itemView.findViewById(R.id.tv_nameitem_library);
            tvsecond = itemView.findViewById(R.id.tv_second_library);
            imgitemplaylist = itemView.findViewById(R.id.img_itemplaylist_library);
            imgitemartist = itemView.findViewById(R.id.img_itemartist_library);
            btnmore = itemView.findViewById(R.id.btn_more_library);
            imgPin = itemView.findViewById(R.id.ic_pin_library);
        }
    }
}
