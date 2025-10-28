package com.example.mymusic.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusic.R;
import com.example.mymusic.activities.MainActivity;
import com.example.mymusic.fragments.CategoryPlaylistFragment;
import com.example.mymusic.fragments.PlayedSongDetailFragment;
import com.example.mymusic.fragments.SearchFragment;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.MusicPlayerService;


import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GREETING = 0;
    private static final int VIEW_TYPE_SECTION = 1;
    private static final int VIEW_TYPE_CATETORY = 2;
    private static final int VIEW_TYPE_ARTIST = 3;
    private static final int VIEW_TYPE_PLAYED = 4;
    private final Context context;
    private  List<Object> items; // chứa GreetingSection hoặc SectionData
    private  ArrayList<Playlist> playlists;
    private  ArrayList<Artist> artists;

    public  MusicPlayerService musicPlayerService;
    private  boolean isServiceBound = false;
    public void setMusicPlayerService(MusicPlayerService service) {
        this.musicPlayerService = service;
    }
    public HomeAdapter(Context context, List<Object> items, ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
        this.context = context;
        this.items = items;
        this.playlists = playlists;
        this.artists = artists != null ? artists : new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof GreetingSection) return VIEW_TYPE_GREETING;
        if (item instanceof SectionData) return VIEW_TYPE_SECTION;
        if (item instanceof CategoryData) return VIEW_TYPE_CATETORY;
        if (item instanceof ArtistData) return VIEW_TYPE_ARTIST;
        if (item instanceof PlayedData) return VIEW_TYPE_PLAYED;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_GREETING) {
            View view = inflater.inflate(R.layout.item_home_greeting, parent, false);
            return new GreetingViewHolder(view);
        }
        if(viewType == VIEW_TYPE_SECTION)  {
            View view = inflater.inflate(R.layout.item_home_section, parent, false);
            return new SectionViewHolder(view);
        }
        if(viewType == VIEW_TYPE_CATETORY){
            View view = inflater.inflate(R.layout.item_home_catetory, parent, false);
            return new CategoryViewHolder(view);
        }
        if(viewType == VIEW_TYPE_ARTIST){
            View view = inflater.inflate(R.layout.item_artist_trending_list, parent, false);
            return new ArtistViewHolder(view);
        }
        if(viewType == VIEW_TYPE_PLAYED){
            View view = inflater.inflate(R.layout.item_home_section, parent, false);
            return new PlayedViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof GreetingViewHolder && item instanceof GreetingSection) {
            ((GreetingViewHolder) holder).bind((GreetingSection) item, context, playlists, artists);
        } else if (holder instanceof SectionViewHolder && item instanceof SectionData) {
            ((SectionViewHolder) holder).bind((SectionData) item, context, playlists, artists);
        }else if (holder instanceof CategoryViewHolder && item instanceof CategoryData) {
            ((CategoryViewHolder) holder).bind((CategoryData) item, context, playlists, artists);
        } else if (holder instanceof ArtistViewHolder && item instanceof ArtistData) {
            ((ArtistViewHolder) holder).bind((ArtistData) item, context, playlists, artists);
        } else if (holder instanceof PlayedViewHolder && item instanceof PlayedData) {
            ((PlayedViewHolder) holder).bind((PlayedData) item, context, playlists, artists);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public void updateData(List<Object> newItems, ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ====== Greeting section ======
     class GreetingViewHolder extends RecyclerView.ViewHolder {
        TextView greetingText;
        RecyclerView recyclerView;

        GreetingViewHolder(View itemView) {
            super(itemView);
            greetingText = itemView.findViewById(R.id.greeting_text);
            recyclerView = itemView.findViewById(R.id.recycler_recent);
        }

        void bind(GreetingSection section, Context context ,ArrayList<Playlist> playlists, ArrayList<Artist> artists) {

            greetingText.setText(section.getGreeting());
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            );
            recyclerView.setNestedScrollingEnabled(false);
            //  Gắn adapter vào RecyclerView
            SongAdapter adapter = new SongAdapter(context,
                    new ArrayList<>(section.getSongs()),
                    playlists,
                    artists);
            recyclerView.setAdapter(adapter);
            if (musicPlayerService == null) {
                Toast.makeText(context, "khong co musicPlayerService: ", Toast.LENGTH_SHORT).show();
            }
            musicPlayerService.setSongs(new ArrayList<>(section.getSongs()));
            ArrayList<Song> songsList = new ArrayList<>(section.getSongs());
            //sự kiện click
            adapter.setOnSongClickListener((song, pos) -> {
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
                //hiện mini playẻr
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showMiniPlayer(song);
                }
            });
            //set layout
            adapter.setCompactLayout(true);
        }
    }

    // ====== Section (e.g. “Dành cho bạn”) ======
    class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;

        SectionViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
            recyclerView = itemView.findViewById(R.id.recycler_items);

        }

        void bind(SectionData section, Context context, ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
            sectionTitle.setText(section.getTitle());
            //cuộn dọc
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            );
            recyclerView.setNestedScrollingEnabled(false);

            // SongAdapter của bạn
            SongAdapter adapter = new SongAdapter(context,
                    new ArrayList<>(section.getSongs()),
                    playlists,
                    artists);
            recyclerView.setAdapter(adapter);
            adapter.updateArtists(artists);
            //sự kiện click
            if (musicPlayerService == null) {
                Toast.makeText(context, "khong co musicPlayerService: ", Toast.LENGTH_SHORT).show();
            }
            musicPlayerService.setSongs(new ArrayList<>(section.getSongs()));
            ArrayList<Song> songsList = new ArrayList<>(section.getSongs());
            //sự kiện click
            adapter.setOnSongClickListener((song, pos) -> {
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
                //hiện mini playẻr
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showMiniPlayer(song);
                }
            });
            //set layout
            adapter.setCompactLayout(false);

        }
    }
    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;
        public CategoryViewHolder(View itemView ) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.catetory_title);
            recyclerView = itemView.findViewById(R.id.recycler_items_catetory);

        }
        void bind(CategoryData section, Context context ,ArrayList<Playlist> playlists, ArrayList<Artist> artists) {

            sectionTitle.setText(section.getTitle());
            recyclerView.setLayoutManager(
                    new GridLayoutManager(itemView.getContext(), 2, RecyclerView.VERTICAL, false)
            );
            recyclerView.setNestedScrollingEnabled(false);
            CategoryPlaylistAdapter adapter = new CategoryPlaylistAdapter(playlists);
            recyclerView.setAdapter(adapter);
        }
    }
    private class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;
        public ArtistViewHolder(View itemView ) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.trending_title);
            recyclerView = itemView.findViewById(R.id.recyclerTrending);
        }
        void bind(ArtistData section, Context context , ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
            sectionTitle.setText(section.getTitle());
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false));
            recyclerView.setNestedScrollingEnabled(false);
            ArtistAdapter adapter = new ArtistAdapter(artists);
            recyclerView.setAdapter(adapter);
        }
    }

    private class PlayedViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;
        ImageButton button;
        public PlayedViewHolder(View itemView ) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
            recyclerView = itemView.findViewById(R.id.recycler_items);
            button = itemView.findViewById(R.id.btn_detail);
        }
        void bind(PlayedData section, Context context , ArrayList<Playlist> playlists, ArrayList<Artist> artists) {
            sectionTitle.setText(section.getTitle());
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.VERTICAL, false));
            recyclerView.setNestedScrollingEnabled(false);
            SongAdapter adapter = new SongAdapter(context,
                    new ArrayList<>(section.getSongs()),
                    playlists,
                    artists);
            recyclerView.setAdapter(adapter);
            if (musicPlayerService != null) {
                adapter.setMusicPlayerService(musicPlayerService);
            } else {
                Toast.makeText(context, "khong co musicPlayerService: ", Toast.LENGTH_SHORT).show();
            }
            adapter.setMusicPlayerService(musicPlayerService);
            button.setOnClickListener(v -> {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                // Chuyển Fragment
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_up,  // enter
                                R.anim.fade_out,     // exit
                                R.anim.fade_in,      // popEnter
                                R.anim.slide_out_down // popExit
                        )
                        .replace(R.id.container_main, new PlayedSongDetailFragment())
                        .addToBackStack(null)  // cho phép bấm Back để quay lại
                        .commit();
                ViewPager2 viewPager = activity.findViewById(R.id.vp_fragmain);
                viewPager.setVisibility(View.GONE);

                FrameLayout container = activity.findViewById(R.id.container_main);
                container.setVisibility(View.VISIBLE);

            });
            adapter.setCompactLayout(false);

        }
    }

    // ====== Data models ======
    public static class GreetingSection {
        private final String greeting;
        private final List<Song> songs;

        public GreetingSection(String greeting, List<Song> songs) {
            this.greeting = greeting;
            this.songs = songs;
        }

        public String getGreeting() { return greeting; }
        public List<Song> getSongs() { return songs; }
    }

    public static class SectionData {
        private final String title;
        private final List<Song> songs;

        public SectionData(String title, List<Song> songs) {
            this.title = title;
            this.songs = songs;
        }

        public String getTitle() { return title; }
        public List<Song> getSongs() { return songs; }
    }
    public static class PlayedData {
        private final String title;
        private final List<Song> songs;

        public PlayedData(String title, List<Song> songs) {
            this.title = title;
            this.songs = songs;
        }

        public String getTitle() { return title; }
        public List<Song> getSongs() { return songs; }
    }

    public static class CategoryData {
        private final String title;
        private final List<Playlist> songs;

        public CategoryData(String title, List<Playlist> songs) {
            this.title = title;
            this.songs = songs;
        }

        public String getTitle() { return title; }
        public List<Playlist> getSongs() { return songs; }
    }
    public static class ArtistData {
        private final String title;
        private final List<Artist> artists;

        public ArtistData(String greeting, List<Artist> artists) {
            this.title = greeting;
            this.artists = artists;
        }

        public String getTitle() { return title; }
        public List<Artist> getSongs() { return artists; }
    }

}
