package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SongsFragment extends Fragment {
    public static MusicAdapter musicAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        GetSongsFromFirestore();
    }

    private void GetSongsFromFirestore() {
        db.collection("music").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("Firestore", "Error listening to collection changes", e);
                    return;
                }
                
                if (querySnapshot != null) {
                    ArrayList<MusicFiles> songs = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        MusicFiles song = document.toObject(MusicFiles.class);
                        songs.add(song);
                    }
                    updateSongList(songs);
                }
            }
        });
    }

    private void updateSongList(ArrayList<MusicFiles> songs) {
        musicAdapter = new MusicAdapter(getContext(), songs);
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    }
}