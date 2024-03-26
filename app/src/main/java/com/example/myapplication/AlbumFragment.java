package com.example.myapplication;

import static com.example.myapplication.MainActivity.albums;
import static com.example.myapplication.MainActivity.musicFiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        ArrayList<MusicFiles> albumsToShow = new ArrayList<>();
        for (MusicFiles album : albums) {
            if (getNumberOfSongsInAlbum(album.getAlbum()) >= 3) {
                albumsToShow.add(album);
            }
        }

        if (!albumsToShow.isEmpty()) {
            albumAdapter = new AlbumAdapter(getContext(), albumsToShow);
            recyclerView.setAdapter(albumAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }

        return v;
    }

    private int getNumberOfSongsInAlbum(String albumName) {
        int count = 0;
        for (MusicFiles musicFile : musicFiles) {
            if (albumName.equals(musicFile.getAlbum())) {
                count++;
            }
        }
        return count;
    }

    private boolean hasEnoughSongsInAlbums(ArrayList<MusicFiles> albums, int minimumSongs) {
        for (MusicFiles album : albums) {
            if (getNumberOfSongsInAlbum(album.getAlbum()) >= minimumSongs) {
                return true;
            }
        }
        return false;
    }
}