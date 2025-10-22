package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GREETING = 0;
    private static final int VIEW_TYPE_SECTION = 1;


    private final Context context;
    private final List<Object> items; // Có thể chứa String (title) hoặc List<Song>

    public HomeAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return VIEW_TYPE_SECTION;
        if (item instanceof GreetingSection) return VIEW_TYPE_GREETING;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_GREETING) {
            View view = inflater.inflate(R.layout.item_home_greeting, parent, false);
            return new GreetingViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_home_section, parent, false);
            return new SectionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof GreetingViewHolder && item instanceof GreetingSection) {
            ((GreetingViewHolder) holder).bind((GreetingSection) item);
        } else if (holder instanceof SectionViewHolder && item instanceof SectionData) {
            ((SectionViewHolder) holder).bind((SectionData) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ====== Greeting section ======
    static class GreetingViewHolder extends RecyclerView.ViewHolder {
        TextView greetingText;
        RecyclerView recyclerView;

        GreetingViewHolder(View itemView) {
            super(itemView);
            greetingText = itemView.findViewById(R.id.greeting_text);
            recyclerView = itemView.findViewById(R.id.recycler_recent);
        }

        void bind(GreetingSection section) {
            greetingText.setText(section.getGreeting());
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
            );


        }
    }

    // ====== Section (e.g. “Dành cho bạn”) ======
    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        RecyclerView recyclerView;

        SectionViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
            recyclerView = itemView.findViewById(R.id.recycler_items);
        }

        void bind(SectionData section) {
            sectionTitle.setText(section.getTitle());
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
            );

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
}
