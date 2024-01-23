package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.viewHolder> {

    private Context music_Context;
    private ArrayList<MusicFiles> music_Files;

    MusicAdapter(Context music_Context, ArrayList<MusicFiles> music_Files) {
        this.music_Files = music_Files;
        this.music_Context = music_Context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(music_Context).inflate(R.layout.music_items, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.file_Name.setText(music_Files.get(position).getTitle());
        try {
            byte[] image = getAlbumArt(music_Files.get(position).getPath());
            if (image != null) {
                Glide.with(music_Context).asBitmap()
                        .load(image)
                        .into(holder.album_Art);
            } else {
                Glide.with(music_Context)
                        .load(R.drawable.question_mark)
                        .into(holder.album_Art);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(music_Context, PlayerActivity.class);
                    music_Context.startActivity(i);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, e.g., load a default image or log the error
            Glide.with(music_Context)
                    .load(R.drawable.question_mark)
                    .into(holder.album_Art);
        }
    }

    @Override
    public int getItemCount() {
        return music_Files.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView file_Name;
        ImageView album_Art;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            file_Name = itemView.findViewById(R.id.music_file_name);
            album_Art = itemView.findViewById(R.id.music_img);
        }
    }

    private byte[] getAlbumArt(String s) throws IOException { //public static ArrayList<MusicFiles> getSongs - explains meaning of Uri there!!
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(s);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
