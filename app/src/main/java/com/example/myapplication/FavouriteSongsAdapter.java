package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavouriteSongsAdapter extends RecyclerView.Adapter<FavouriteSongsAdapter.ViewHolder> {
    private List<String> favouriteSongs;
    private Context context;

    public FavouriteSongsAdapter(Context context, List<String> favoriteSongs) {
        this.context = context;
        this.favouriteSongs = favoriteSongs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favourite_song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String songTitle = favouriteSongs.get(position);
        holder.songTitleTextView.setText(songTitle);
    }

    @Override
    public int getItemCount() {
        return favouriteSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitleTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitleTextView = itemView.findViewById(R.id.song_title);
        }
    }
}
