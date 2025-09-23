package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ARTIST_LIST = 1;
    private static final int VIEW_TYPE_CATEGORY_LIST = 2;

    // 👇 Thêm 2 loại view mới cho search result
    private static final int VIEW_TYPE_SONG = 3;       // 👈 sửa
    private static final int VIEW_TYPE_ARTIST = 4;     // 👈 sửa

    private final Context context;
    private final List<Object> items;

    public SearchAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return VIEW_TYPE_TITLE;
        if (item instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) item;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Artist) return VIEW_TYPE_ARTIST_LIST;
                if (first instanceof String) return VIEW_TYPE_CATEGORY_LIST;
            }
        }
        // 👇 check thêm Song / Artist riêng lẻ
        if (item instanceof Song) return VIEW_TYPE_SONG;     // 👈 sửa
        if (item instanceof Artist) return VIEW_TYPE_ARTIST; // 👈 sửa

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
        } else if (viewType == VIEW_TYPE_CATEGORY_LIST) {
            View view = inflater.inflate(R.layout.item_category_list, parent, false);
            return new CategoryListViewHolder(view);

        } else {
            // 👇 fallback an toàn, tránh lỗi Missing return
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
            CategoryAdapter adapter = new CategoryAdapter(categories);
            recyclerView.setAdapter(adapter);
        }
    }

    // ---------------- Search Result (Song / Artist) ----------------


}
