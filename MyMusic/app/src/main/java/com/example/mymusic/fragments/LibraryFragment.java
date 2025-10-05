package com.example.mymusic.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.LibraryAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.LibraryItem;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.User;
import com.example.mymusic.repository.MusicRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class LibraryFragment extends Fragment {

    private RecyclerView rcvLibrary;
    private AppCompatButton btnAddPlaylist, icCancel;
    private TextView tvFilterLibraryDSP, tvFilterLibraryCB, tvFilterLibraryMayMusic, tvFilterLibraryNS, tvFilterLibraryTD,tvSortLibrary;

    private LibraryAdapter libraryAdapter;
    private ArrayList<Playlist> allPlaylists;
    private ArrayList<Artist> allArtists;
    private ArrayList<LibraryItem> displayedLibraryItems; // Combined list for display
    private Map<String, User> usersMap = new HashMap<>();
    private ArrayList<String> followingPlaylistIds = new ArrayList<>();
    private ArrayList<String> followingArtistIds = new ArrayList<>();

    private MusicRepository musicRepository;
    private static final String TAG = "LibraryFragment";
    private String currentActiveFilterType = null; // To help restore state, or "all_items_initial"

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allPlaylists = new ArrayList<>();
        allArtists = new ArrayList<>();
        displayedLibraryItems = new ArrayList<>();
        musicRepository = new MusicRepository();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi fragment quay trở lại
        // Điều này đảm bảo rằng các thay đổi (như xóa bài hát đã tải xuống)
        // được phản ánh, bao gồm cả số lượng bài hát.
        if (libraryAdapter != null) {
            loadInitialData();
        }
    }

    @Nonnull
    @Override
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // ... (find view by id for other views) ...
        btnAddPlaylist = view.findViewById(R.id.btn_addPlaylist_library);
        rcvLibrary = view.findViewById(R.id.rv_library);
        icCancel = view.findViewById(R.id.ic_cancel);
        tvFilterLibraryDSP = view.findViewById(R.id.tv_filterlibrary_dsp);
        tvFilterLibraryCB = view.findViewById(R.id.tv_filterlibrary_cb);
        tvFilterLibraryMayMusic = view.findViewById(R.id.tv_filterlibrary_maymusic);
        tvFilterLibraryNS = view.findViewById(R.id.tv_filterlibrary_ns);
        tvFilterLibraryTD = view.findViewById(R.id.tv_filterlibrary_td);
        tvSortLibrary= view.findViewById(R.id.tv_sortby);

        rcvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(displayedLibraryItems, usersMap);
        rcvLibrary.setAdapter(libraryAdapter);

        libraryAdapter.setOnPinStateChangeListener(() -> {
            // Sắp xếp pinned lên đầu
            Collections.sort(displayedLibraryItems, (a, b) -> {
                if (a.isPinned() && !b.isPinned()) return -1;
                if (!a.isPinned() && b.isPinned()) return 1;
                return 0;
            });

            libraryAdapter.notifyDataSetChanged();
        });

        btnAddPlaylist.setOnClickListener(v -> addPlaylist());
        tvFilterLibraryDSP.setOnClickListener(v -> onClickFilterDSP());
        tvFilterLibraryCB.setOnClickListener(v -> onClickFilterCB());
        tvFilterLibraryMayMusic.setOnClickListener(v -> onClickFilterMayMusic());
        tvFilterLibraryTD.setOnClickListener(v -> onClickFilterFollowTD());
        tvFilterLibraryNS.setOnClickListener(v -> onClickFilterNS());
        icCancel.setOnClickListener(v -> onClickCancelFilter());
        tvSortLibrary.setOnClickListener(v->{SortLibrary(v);});

        currentActiveFilterType = "all_items_initial"; // Set initial filter type
        loadInitialData();

        return view;
    }
    private void SortLibrary(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_sort_library, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_sort_default) {
                Toast.makeText(v.getContext(), "Mặc định (không sort)", Toast.LENGTH_SHORT).show();
                loadInitialData(); // reload lại dữ liệu ban đầu
                sortPinnedFirst();
                return true;
            } else if (id == R.id.action_sort_date) {
                Toast.makeText(v.getContext(), "Sắp xếp theo ngày tạo", Toast.LENGTH_SHORT).show();
                // sort Playlist theo createdAt (mới nhất lên đầu)
                Collections.sort(displayedLibraryItems, (a, b) -> {
                    if (a.isPlaylist() && b.isPlaylist()) {
                        Timestamp ta = a.getPlaylist().getCreatedAt();
                        Timestamp tb = b.getPlaylist().getCreatedAt();
                        if (ta == null || tb == null) return 0;
                        return tb.compareTo(ta); // mới -> cũ
                    }
                    return 0; // Artist giữ nguyên
                });
                sortPinnedFirst();
                libraryAdapter.notifyDataSetChanged();
                return true;
            } else if (id == R.id.action_sort_name_az) {
                Toast.makeText(v.getContext(), "Sắp xếp theo tên (A-Z)", Toast.LENGTH_SHORT).show();
                Collections.sort(displayedLibraryItems, (a, b) -> {
                    String nameA, nameB;
                    if (a.isPlaylist()) {
                        nameA = a.getPlaylist().getTitle();
                    } else {
                        nameA = a.getArtist().getName();
                    }
                    if (b.isPlaylist()) {
                        nameB = b.getPlaylist().getTitle();
                    } else {
                        nameB = b.getArtist().getName();
                    }
                    if (nameA == null) nameA = "";
                    if (nameB == null) nameB = "";
                    return nameA.compareToIgnoreCase(nameB);
                });sortPinnedFirst();

                libraryAdapter.notifyDataSetChanged();
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }




    private void onClickFilterDSP() {
        updateFilterBackgrounds(tvFilterLibraryDSP);
        tvFilterLibraryCB.setVisibility(View.VISIBLE);
        tvFilterLibraryMayMusic.setVisibility(View.VISIBLE);
        tvFilterLibraryTD.setVisibility(View.VISIBLE);
        tvFilterLibraryNS.setVisibility(View.GONE);
        icCancel.setVisibility(View.VISIBLE);
        currentActiveFilterType = null; // DSP default (playlists only)
        filterAndDisplayItems(currentActiveFilterType);
    }

    private void onClickFilterCB() {
        updateFilterBackgrounds(tvFilterLibraryCB);
        tvFilterLibraryMayMusic.setVisibility(View.GONE);
        tvFilterLibraryTD.setVisibility(View.GONE);
        tvFilterLibraryNS.setVisibility(View.GONE);
        icCancel.setVisibility(View.VISIBLE); // Keep cancel visible as it is a sub-filter of DSP
        currentActiveFilterType = "user";
        filterAndDisplayItems(currentActiveFilterType);
    }

    private void onClickFilterMayMusic() {
        updateFilterBackgrounds(tvFilterLibraryMayMusic);
        tvFilterLibraryCB.setVisibility(View.GONE);
        tvFilterLibraryTD.setVisibility(View.GONE);
        tvFilterLibraryNS.setVisibility(View.GONE);
        icCancel.setVisibility(View.VISIBLE); // Keep cancel visible
        currentActiveFilterType = "system";
        filterAndDisplayItems(currentActiveFilterType);
    }

    private void onClickFilterFollowTD() {
        updateFilterBackgrounds(tvFilterLibraryTD);
        tvFilterLibraryCB.setVisibility(View.GONE);
        tvFilterLibraryMayMusic.setVisibility(View.GONE);
        tvFilterLibraryNS.setVisibility(View.GONE);
        icCancel.setVisibility(View.VISIBLE); // Keep cancel visible
        currentActiveFilterType = "follow_playlist_only";
        filterAndDisplayItems(currentActiveFilterType);
    }

    private void onClickFilterNS() {
        updateFilterBackgrounds(tvFilterLibraryNS);
        tvFilterLibraryDSP.setVisibility(View.GONE);
        tvFilterLibraryCB.setVisibility(View.GONE);
        tvFilterLibraryMayMusic.setVisibility(View.GONE);
        tvFilterLibraryTD.setVisibility(View.GONE);
        icCancel.setVisibility(View.VISIBLE);
        currentActiveFilterType = "artist_only";
        filterAndDisplayItems(currentActiveFilterType);
    }

    private void onClickCancelFilter() {
        tvFilterLibraryDSP.setVisibility(View.VISIBLE);
        tvFilterLibraryNS.setVisibility(View.VISIBLE);
        tvFilterLibraryCB.setVisibility(View.GONE);
        tvFilterLibraryMayMusic.setVisibility(View.GONE);
        tvFilterLibraryTD.setVisibility(View.GONE);
        icCancel.setVisibility(View.GONE);

        // Khi cancel thì trở lại trạng thái initial (all playlists + artists user đang follow)
        currentActiveFilterType = "all_items_initial";
        updateFilterBackgrounds(null);
        filterAndDisplayItems("all_items_initial");
    }


    private void updateFilterBackgrounds(TextView selectedTextView) {
        TextView[] allFilters = {tvFilterLibraryDSP, tvFilterLibraryCB, tvFilterLibraryMayMusic, tvFilterLibraryNS, tvFilterLibraryTD};
        for (TextView filterTextView : allFilters) {
            if (getContext() == null) return;
            sortPinnedFirst();
            if (filterTextView == selectedTextView) {
                filterTextView.setBackgroundResource(R.drawable.bg_filterlibrary_select);
            } else {
                filterTextView.setBackgroundResource(R.drawable.bg_filterlibrary_unselect);
            }
        }
    }

    private void filterAndDisplayItems(String type) {
        displayedLibraryItems.clear();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            if (libraryAdapter != null) {

                libraryAdapter.notifyDataSetChanged();
            }
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<LibraryItem> tempItems = new ArrayList<>();

        // Add virtual "Favorites" playlist for user-centric views
        if ("all_items_initial".equals(type) || "user".equals(type) || type == null) {
            Playlist favoritesPlaylist = new Playlist();
            favoritesPlaylist.setPlaylistID("favorites_playlist"); // Special non-firestore ID
            favoritesPlaylist.setTitle("Bài hát yêu thích");
            favoritesPlaylist.setType("favorites"); // Distinguishing type
            favoritesPlaylist.setUserId(currentUserId);
            favoritesPlaylist.setPinned(true);
            displayedLibraryItems.add(new LibraryItem(favoritesPlaylist));

            Playlist downloadedSongs = new Playlist();
            downloadedSongs.setPlaylistID("downloaded_songs");
            downloadedSongs.setTitle("Bài hát đã tải");
            downloadedSongs.setType("downloads");
            downloadedSongs.setUserId(currentUserId);
            downloadedSongs.setPinned(true);
            displayedLibraryItems.add(new LibraryItem(downloadedSongs));
        }

        if ("all_items_initial".equals(type)) {
            // Add user's own playlists
            for (Playlist p : allPlaylists) {
                if (currentUserId.equals(p.getUserId()) && "user".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
            // Add system playlists
            for (Playlist p : allPlaylists) {
                if ("system".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
            // Add other followed playlists (non-user, non-system)
            for (Playlist p : allPlaylists) {
                if (followingPlaylistIds.contains(p.getPlaylistID()) &&
                        !currentUserId.equals(p.getUserId()) &&
                        !"system".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
            // Add followed artists
            for (Artist a : allArtists) {
                if (followingArtistIds.contains(a.getArtistID())) {
                    tempItems.add(new LibraryItem(a));
                }
            }
        } else if (type == null) { // Default for DSP: User, System, and Followed Playlists (non-user, non-system) - NO ARTISTS
            for (Playlist p : allPlaylists) {
                if (currentUserId.equals(p.getUserId()) && "user".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
            for (Playlist p : allPlaylists) {
                if ("system".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
            for (Playlist p : allPlaylists) {
                if (followingPlaylistIds.contains(p.getPlaylistID()) &&
                        !currentUserId.equals(p.getUserId()) &&
                        !"system".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
        } else if ("user".equals(type)) {
            for (Playlist p : allPlaylists) {
                if (currentUserId.equals(p.getUserId()) && "user".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
        } else if ("system".equals(type)) {
            for (Playlist p : allPlaylists) {
                if ("system".equals(p.getType())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
        } else if ("follow_playlist_only".equals(type)) { // For TD filter: Followed playlists (non-user, and system if followed)
            for (Playlist p : allPlaylists) {
                // Shows playlists the user is following, excluding their own.
                if (followingPlaylistIds.contains(p.getPlaylistID()) && !currentUserId.equals(p.getUserId())) {
                    tempItems.add(new LibraryItem(p));
                }
            }
        } else if ("artist_only".equals(type)) { // For NS filter: Followed artists only
            for (Artist a : allArtists) {
                if (followingArtistIds.contains(a.getArtistID())) {
                    tempItems.add(new LibraryItem(a));
                }
            }
        }
        displayedLibraryItems.addAll(tempItems);
        sortPinnedFirst();
        libraryAdapter.notifyDataSetChanged();
    }

    private void addPlaylist() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tạo playlist.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm playlist mới");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_playlist, null);
        builder.setView(dialogView);
        EditText edtTitle = dialogView.findViewById(R.id.edt_title_playlist);
        EditText edtDesc = dialogView.findViewById(R.id.edt_desc_playlist);

        builder.setPositiveButton("Thêm", (d, w) -> {
            String title = edtTitle.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Nhập tên playlist!", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Playlist newPl = new Playlist();
            newPl.setTitle(title);
            newPl.setDescription(desc);
            newPl.setType("user");
            newPl.setUserId(currentUserId);
            newPl.setIspublic(false);
            newPl.setCreatedAt(Timestamp.now());
            newPl.setSongs(new ArrayList<>());

            musicRepository.addPlaylist(newPl, isSuccess -> {
                if (isSuccess) {
                    FirebaseFirestore.getInstance().collection("Users").document(currentUserId)
                            .update("playlists", FieldValue.arrayUnion(newPl.getPlaylistID()))
                            .addOnSuccessListener(a -> Log.d(TAG, "Đã thêm playlist vào user"))
                            .addOnFailureListener(e -> Log.e(TAG, "Lỗi update playlists của user: ", e));
                    Toast.makeText(getContext(), "Thêm playlist thành công", Toast.LENGTH_SHORT).show();
                    loadInitialData(); // Reloads data and re-applies the current filter
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm playlist", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void applyCurrentFilterAfterLoad() {
        if ("all_items_initial".equals(currentActiveFilterType)) {
            filterAndDisplayItems("all_items_initial");
            updateFilterBackgrounds(null); // No filter visually selected
            tvFilterLibraryDSP.setVisibility(View.VISIBLE);
            tvFilterLibraryNS.setVisibility(View.VISIBLE);
            tvFilterLibraryCB.setVisibility(View.GONE);
            tvFilterLibraryMayMusic.setVisibility(View.GONE);
            tvFilterLibraryTD.setVisibility(View.GONE);
            icCancel.setVisibility(View.GONE);
        } else if ("artist_only".equals(currentActiveFilterType)) {
            onClickFilterNS();
        } else if ("user".equals(currentActiveFilterType)) {
            onClickFilterCB();
        } else if ("system".equals(currentActiveFilterType)) {
            onClickFilterMayMusic();
        } else if ("follow_playlist_only".equals(currentActiveFilterType)) {
            onClickFilterFollowTD();
        } else { // currentActiveFilterType is null (DSP default)
            onClickFilterDSP();
        }
        sortPinnedFirst();
    }

    private void loadInitialData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            displayedLibraryItems.clear();
            allPlaylists.clear();
            allArtists.clear();

            if (libraryAdapter != null) {
                libraryAdapter.notifyDataSetChanged();
            }
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        musicRepository.listenAllUsers(users -> {
            usersMap.clear();
            for (User u : users) {
                usersMap.put(u.getUserID(), u);
            }

            db.collection("Users").document(currentUserId).get()
                    .addOnSuccessListener(userDoc -> {
                        followingPlaylistIds.clear();
                        followingArtistIds.clear();
                        if (userDoc.exists()) {
                            if (userDoc.contains("followingPlaylists")) {
                                Object rawFollowingPlaylists = userDoc.get("followingPlaylists");
                                if (rawFollowingPlaylists instanceof List) {
                                    followingPlaylistIds.addAll((ArrayList<String>) rawFollowingPlaylists);
                                }
                            }
                            if (userDoc.contains("followingArtists")) {
                                Object rawFollowingArtists = userDoc.get("followingArtists");
                                if (rawFollowingArtists instanceof List) {
                                    followingArtistIds.addAll((ArrayList<String>) rawFollowingArtists);
                                }
                            }
                        }
                        musicRepository.listenAllArtists(artists -> {
                            allArtists.clear();
                            if (artists != null) {
                                allArtists.addAll(artists);
                            }
                            musicRepository.listenAllPlaylists(data -> {
                                allPlaylists.clear();
                                if (data != null) {
                                    allPlaylists.addAll(data);
                                }
                                applyCurrentFilterAfterLoad(); // This will handle the initial display or re-apply
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user follow data: ", e);
                        musicRepository.listenAllArtists(artists -> {
                            allArtists.clear();
                            if (artists != null) {
                                allArtists.addAll(artists);
                            }
                            musicRepository.listenAllPlaylists(data -> {
                                allPlaylists.clear();
                                if (data != null) {
                                    allPlaylists.addAll(data);
                                }
                                applyCurrentFilterAfterLoad();
                            });
                        });
                    });
        });
        sortPinnedFirst();
    }
    private void sortPinnedFirst() {
        Collections.sort(displayedLibraryItems, (a, b) -> {
            if (a.isPinned() && !b.isPinned()) return -1;
            if (!a.isPinned() && b.isPinned()) return 1;
            return 0;
        });
    }

}
