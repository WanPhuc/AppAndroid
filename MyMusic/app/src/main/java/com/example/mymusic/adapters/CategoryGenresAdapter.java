package com.example.mymusic.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusic.R;

import java.util.ArrayList;
import java.util.Random;

public class CategoryGenresAdapter extends RecyclerView.Adapter<CategoryGenresAdapter.CategoryViewHolder> {
    private final ArrayList<String> categories;
    private final Random random = new Random();

    public CategoryGenresAdapter(ArrayList<String> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_genres_search, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategory.setText(category);

        holder.itemView.setOnClickListener(v->{
            AppCompatActivity activity=(AppCompatActivity) v.getContext();
            ViewPager2 viewPager=activity.findViewById(R.id.vp_fragmain);
            MainAdapter adapter=(MainAdapter) viewPager.getAdapter();
            Bundle args=new Bundle();
            args.putString("type","genre");
            args.putString("value",category);
            adapter.setCategoryArgs(args);
            viewPager.setCurrentItem(3,true);
        });

        int backgroundColor;
        int textColor = holder.tvCategory.getCurrentTextColor();

        do {
            backgroundColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        } while (ColorUtils.calculateContrast(textColor, backgroundColor) < 1.5); // Ensure minimum contrast ratio

        Drawable background = holder.itemView.getBackground();
        if (background instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) background;
            Drawable currentDrawable = stateListDrawable.getCurrent();
            if (currentDrawable instanceof GradientDrawable) {
                // Mutate the drawable to avoid affecting other items if they share the same drawable instance
                GradientDrawable gradientDrawable = (GradientDrawable) currentDrawable.mutate();
                gradientDrawable.setColor(backgroundColor);
            } else {

                 holder.itemView.setBackgroundColor(backgroundColor); // Or some other fallback
            }
        } else if (background instanceof GradientDrawable) {
            // If the background was already a GradientDrawable (e.g., if not using a selector)
            GradientDrawable gradientDrawable = (GradientDrawable) background.mutate();
            gradientDrawable.setColor(backgroundColor);
        } else {
            // Fallback for other drawable types or if no background is set
            holder.itemView.setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
