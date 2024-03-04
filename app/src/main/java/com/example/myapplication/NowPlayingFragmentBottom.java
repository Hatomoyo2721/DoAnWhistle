package com.example.myapplication;

import static com.example.myapplication.MainActivity.PATH_TO_FRAG;
import static com.example.myapplication.MainActivity.SHOW_MINI_PLAYER;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class NowPlayingFragmentBottom extends Fragment {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View v;

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        artist = v.findViewById(R.id.song_artist_miniPlayer);
        songName = v.findViewById(R.id.song_name_miniPlayer);
        albumArt = v.findViewById(R.id.bottom_album_art);
        nextBtn = v.findViewById(R.id.skip_next_bottom);
        playPauseBtn = v.findViewById(R.id.play_pause_miniPlayer);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (SHOW_MINI_PLAYER) {
                if (PATH_TO_FRAG != null) {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    Glide.with(getContext()).load(art).
                            into(albumArt);
                    songName.setText(PATH_TO_FRAG);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
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

        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}