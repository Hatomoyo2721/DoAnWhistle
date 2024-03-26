package com.example.myapplication;

import static com.example.myapplication.MainActivity.musicFiles;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    Button btn_back;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        btn_back = findViewById(R.id.album_btn_back);
        albumName = getIntent().getStringExtra("albumName");

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int j = 0;
        for (int i = 0; i < musicFiles.size(); i++) {
            if (albumName.equals(musicFiles.get(i).getAlbum())) {
                albumSongs.add(j, musicFiles.get(i));
                j ++;

            }
        }
        try {
            byte[] image = getAlbumArt(albumSongs.get(0).getPath());
            if (image != null) {
                Glide.with(this)
                        .load(image)
                        .into(albumPhoto);
            }
            else {
                Glide.with(this)
                        .load(R.drawable.question_mark)
                        .into(albumPhoto);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Glide.with(this)
                    .load(R.drawable.question_mark)
                    .into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<MusicFiles> tempAlbumSongs = new ArrayList<>();

        for (MusicFiles musicFile : musicFiles) {
            if (albumName.equals(musicFile.getAlbum())) {
                tempAlbumSongs.add(musicFile);
            }
        }

        if (tempAlbumSongs.size() > 1) {
            albumSongs.clear();
            albumSongs.addAll(tempAlbumSongs);
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
            recyclerView.setVisibility(View.VISIBLE);
            albumPhoto.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            albumPhoto.setVisibility(View.GONE);
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