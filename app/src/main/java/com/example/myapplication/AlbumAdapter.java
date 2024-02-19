package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {
    private Context music_Context;
    private ArrayList<MusicFiles> album_Files;
    View v;

    public AlbumAdapter(Context music_Context, ArrayList<MusicFiles> album_Files) {
        this.music_Context = music_Context;
        this.album_Files = album_Files;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(music_Context).inflate(R.layout.album_item, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(album_Files.get(position).getAlbum());
        try {
            byte[] image = getAlbumArt(album_Files.get(position).getPath());
            if (image != null) {
                Glide.with(music_Context).asBitmap()
                        .load(image)
                        .into(holder.album_image);
            } else {
                Glide.with(music_Context)
                        .load(R.drawable.question_mark)
                        .into(holder.album_image);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, e.g., load a default image or log the error
            Glide.with(music_Context)
                    .load(R.drawable.question_mark)
                    .into(holder.album_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(music_Context, AlbumDetails.class);
                i.putExtra("albumName", album_Files.get(position).getAlbum());
                music_Context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return album_Files.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }

    private byte[] getAlbumArt(String s) throws IOException {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(s);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();

            if (art != null) {
                return art;
            }
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
