package com.example.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.SearchResultAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private RecyclerView rvSearchResult;
    private TextView tvEmptyResult;
    private SearchResultAdapter adapter;

    private ArrayList<Object> searchResults = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        rvSearchResult = view.findViewById(R.id.rv_search_result);
        tvEmptyResult = view.findViewById(R.id.tvEmptyResult);

        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchResultAdapter(getContext(), searchResults);
        rvSearchResult.setAdapter(adapter);

        // Nhận dữ liệu truyền từ SearchFragment (nếu có)
        if (getArguments() != null) {
            ArrayList<Object> results =
                    (ArrayList<Object>) getArguments().getSerializable("search_results");
            if (results != null) {
                updateResults(results);
            }
        }

        return view;
    }

    /**
     * Cập nhật danh sách kết quả tìm kiếm
     */
    public void updateResults(List<Object> results) {
        searchResults.clear();
        searchResults.addAll(results);
        adapter.notifyDataSetChanged();

        // Hiển thị/hide "không tìm thấy"
        if (results.isEmpty()) {
            tvEmptyResult.setVisibility(View.VISIBLE);
            rvSearchResult.setVisibility(View.GONE);
        } else {
            tvEmptyResult.setVisibility(View.GONE);
            rvSearchResult.setVisibility(View.VISIBLE);
        }
    }
}
