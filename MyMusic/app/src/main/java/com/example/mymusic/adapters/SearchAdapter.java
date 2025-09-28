package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ARTIST_LIST = 1;
    private static final int VIEW_TYPE_CATEGORY_GENRES_LIST = 2;
    private static final int VIEW_TYPE_CATEGORY_PLAYLIST_LIST = 3;

    private final Context context;
    private final List<Object> items;

    public SearchAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);

        if (item instanceof String) {
            return VIEW_TYPE_TITLE;
        }

        if (item instanceof ArrayList && position > 0 && items.get(position - 1) instanceof String) {
            String title = (String) items.get(position - 1);

            switch (title) {
                case "Nghệ sĩ thịnh hành":
                    return VIEW_TYPE_ARTIST_LIST;
                case "Khám phá thể loại":
                    return VIEW_TYPE_CATEGORY_GENRES_LIST;
                case "Khám phá theo chủ đề":
                    return VIEW_TYPE_CATEGORY_PLAYLIST_LIST;
                default:
                    return VIEW_TYPE_TITLE;
            }
        }

        return VIEW_TYPE_TITLE;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_TITLE) {
            View view = inflater.inflate(R.layout.item_title_search, parent, false);
            return new TitleViewHolder(view);

        } else if (viewType == VIEW_TYPE_ARTIST_LIST) {
            View view = inflater.inflate(R.layout.item_artist_trending_list, parent, false);
            return new ArtistListViewHolder(view);

        } else if (viewType == VIEW_TYPE_CATEGORY_GENRES_LIST) {
            View view = inflater.inflate(R.layout.item_category_genres_list, parent, false);
            return new CategoryListViewHolder(view);

        } else if (viewType == VIEW_TYPE_CATEGORY_PLAYLIST_LIST) {
            View view = inflater.inflate(R.layout.item_category_playlist_list, parent, false);
            return new CategoryPlaylistListViewHolder(view);

        } else {
            View view = inflater.inflate(R.layout.item_title_search, parent, false);
            return new TitleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).bind((String) item);

        } else if (holder instanceof ArtistListViewHolder) {
            ((ArtistListViewHolder) holder).bind((ArrayList<Artist>) item);

        } else if (holder instanceof CategoryListViewHolder) {
            ((CategoryListViewHolder) holder).bind((ArrayList<String>) item);

        } else if (holder instanceof CategoryPlaylistListViewHolder) {
            ((CategoryPlaylistListViewHolder) holder).bind((ArrayList<Playlist>) item);
        }

    }

    public void updateData(List<Object> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ---------------- Title ----------------
    static class TitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        TitleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
        }

        void bind(String text) {
            title.setText(text);
        }
    }

    // ---------------- Artist List (ngang) ----------------
    static class ArtistListViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        ArtistListViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerTrending);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false));
        }

        void bind(ArrayList<Artist> artists) {
            ArtistAdapter adapter = new ArtistAdapter(artists);
            recyclerView.setAdapter(adapter);
        }
    }

    // ---------------- Category List (grid) ----------------
    static class CategoryListViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        CategoryListViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerCategory);
            recyclerView.setLayoutManager(
                    new GridLayoutManager(itemView.getContext(), 2, RecyclerView.VERTICAL, false)
            );
        }

        void bind(ArrayList<String> categories) {
            CategoryGenresAdapter adapter = new CategoryGenresAdapter(categories);
            recyclerView.setAdapter(adapter);
        }
    }

    // ---------------- Playlist List (grid) ----------------
    static class CategoryPlaylistListViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        CategoryPlaylistListViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerCategoryplaylist);
            recyclerView.setLayoutManager(
                    new GridLayoutManager(itemView.getContext(), 2, RecyclerView.VERTICAL, false)
            );
        }

        void bind(ArrayList<Playlist> playlists) {
            CategoryPlaylistAdapter adapter = new CategoryPlaylistAdapter(playlists);
            recyclerView.setAdapter(adapter);
        }
    }

}
