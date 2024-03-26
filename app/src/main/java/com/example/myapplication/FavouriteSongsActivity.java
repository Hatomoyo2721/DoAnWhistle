package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavouriteSongsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavouriteSongsAdapter favouriteSongsAdapter;
    private List<String> favouriteSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_songs);

        recyclerView = findViewById(R.id.recyclerView_favourite);
        favouriteSongsAdapter = new FavouriteSongsAdapter(this, favouriteSongs);
        recyclerView.setAdapter(favouriteSongsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favouriteSongsAdapter.notifyDataSetChanged();
    }
}