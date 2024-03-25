package com.example.myapplication;

import static com.example.myapplication.AlbumDetailsAdapter.album_Files;
import static com.example.myapplication.MainActivity.repeatBoolean;
import static com.example.myapplication.MainActivity.shuffleBoolean;
import static com.example.myapplication.MusicAdapter.music_Files;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

    private TextView song_name, artist_name, duration_played, duration_finished;
    private ImageView back_btn, next_btn, prev_btn, shuffle_btn, repeat_btn, cover_art;
    private FloatingActionButton play_pause_btn;
    private SeekBar seekBar;
    private int position = -1;
    public static Uri uri;
    //public static MediaPlayer mediaPlayer; //Để phát nhạc và sử dụng các thao tác liên quan tới phát nhạc
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    private final Handler handler = new Handler(); //Xử lý cập nhật giao diện người dùng
    private Thread playThread, prevThread, nextThread; //Xử lý sự kiện: Play/Pause, Previous, Next
    MusicService musicService;
    ServiceConnection serviceConnection;

    //Thiết lập giao diện người dùng và gọi các phương thức khởi tạo khác
    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duration_play);
        duration_finished = findViewById(R.id.duration_finish);
        cover_art = findViewById(R.id.cover_art);
        next_btn = findViewById(R.id.next);
        prev_btn = findViewById(R.id.previous);
        back_btn = findViewById(R.id.back_btn);
        shuffle_btn = findViewById(R.id.shuffle);
        repeat_btn = findViewById(R.id.repeat);
        play_pause_btn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initViews(); //Ánh xạ
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override //Cho người dùng thay đổi thời gian của nhạc đang phát
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() { // 'runOnUiThread' để cập nhật giao diện người dùng liên tục
            @Override
            public void run() { //Hiển thị thời gian đã phát và cập nhật thanh seekbar mỗi giây
                if (musicService != null) {
                    int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPostion);
                    duration_played.setText(formattedTime(mCurrentPostion));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffle_btn.setImageResource(R.drawable.baseline_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    shuffle_btn.setImageResource(R.drawable.baseline_shuffle_on);
                }
            }
        });

        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeat_btn.setImageResource(R.drawable.baseline_repeat_off);
                } else {
                    repeatBoolean = true;
                    repeat_btn.setImageResource(R.drawable.baseline_repeat_on);
                }
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override //Xử lý các nút Play/Pause, Next, Previous
    protected void onResume() {
        Intent i = new Intent(this, MusicService.class);
        bindService(i, this, BIND_AUTO_CREATE);

        playThreadbtn();
        nextThreadbtn();
        prevThreadbtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadbtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prev_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        try {
            if (musicService.isPlaying() && !listSongs.isEmpty()) {
                musicService.stop();
                musicService.release();

                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(listSongs.size() - 1);
                } else {
                    position = position > 0 ? position - 1 : listSongs.size() - 1;
                }

                uri = Uri.parse(listSongs.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri);

                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());
                cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));

                musicService.start();

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });

                musicService.OnCompleted();
                musicService.showNotification(R.drawable.baseline_pause);
                play_pause_btn.setBackgroundResource(R.drawable.baseline_pause);

                musicService.start();

            } else {
                musicService.stop();
                musicService.release();

                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(listSongs.size() - 1);
                } else if (!shuffleBoolean && !repeatBoolean) {
                    position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
                }

                uri = Uri.parse(listSongs.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri);

                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());
                cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));

                musicService.start();

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });

                musicService.OnCompleted();
                musicService.showNotification(R.drawable.baseline_play_arrow);
                play_pause_btn.setBackgroundResource(R.drawable.baseline_play_arrow);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextThreadbtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        try {
            if (musicService.isPlaying() && !listSongs.isEmpty()) {
                musicService.stop();
                musicService.release();

                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(listSongs.size() - 1);
                } else if (!shuffleBoolean && !repeatBoolean) {
                    position = (position + 1) % listSongs.size();
                } else if (next_btn.isClickable()) {
                    position = getRandom(listSongs.size() - 1);
                }

                uri = Uri.parse(listSongs.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri);

                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());
                cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.OnCompleted();

                musicService.showNotification(R.drawable.baseline_pause);
                play_pause_btn.setBackgroundResource(R.drawable.baseline_pause);

                musicService.start();

            } else {
                musicService.stop();
                musicService.release();

                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(listSongs.size() - 1);
                } else if (!shuffleBoolean && !repeatBoolean) {
                    position = ((position + 1) % listSongs.size());
                }

                uri = Uri.parse(listSongs.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri);

                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());
                cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.OnCompleted();
                musicService.showNotification(R.drawable.baseline_play_arrow);
                play_pause_btn.setBackgroundResource(R.drawable.baseline_play_arrow);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playThreadbtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                play_pause_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        try {
            if (musicService.isPlaying()) {
                musicService.pause();

                play_pause_btn.setImageResource(R.drawable.baseline_play_arrow);
                musicService.showNotification(R.drawable.baseline_play_arrow);

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
            } else {
                play_pause_btn.setImageResource(R.drawable.baseline_pause);
                musicService.showNotification(R.drawable.baseline_pause);
                musicService.start();

                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPostion = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPostion);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private String formattedTime(int mCurrentPosition) {
        String minutes = String.valueOf(mCurrentPosition / 60);
        String seconds = String.valueOf(mCurrentPosition % 60);

        // Ensure minutes and seconds have leading zeros if necessary
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }
        return minutes + ":" + seconds;
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetail")) {
            listSongs = album_Files;
        } else {
            listSongs = music_Files; //Change a bit here musicFiles (MainActivity) -> music_Files (MusicAdapter)
        }

        if (listSongs != null) {
            play_pause_btn.setImageResource(R.drawable.baseline_pause);
            uri = Uri.parse(listSongs.get(position).getPath());

            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));

            seekBar.setMax((int) (listSongs.get(position).getDuration() / 1000));


            getSongDetailsFromFirestore(position);
        }
        Intent i = new Intent(this, MusicService.class);
        i.putExtra("servicePosition", position);
        startService(i);
    }

    private void getSongDetailsFromFirestore(int position) {
        String path = listSongs.get(position).getPath().replace("//", "/");
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("music")
                .document(path);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String title = document.getString("title");
                        String artist = document.getString("artist");
                        long duration = document.getLong("duration"); // Thời lượng bài hát
                        String image = document.getString("image");

                        // Cập nhật thông tin bài hát
                        song_name.setText(title);
                        artist_name.setText(artist);
                        cover_art.setImageURI(Uri.parse(image));


                        // Cập nhật thời lượng bài hát cho SeekBar
                        seekBar.setMax((int) (duration / 1000));

                        // Cập nhật các thông tin khác cho MusicService (nếu cần)
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.w("Firestore", "Error getting documents.", task.getException());
                }
            }
        });
    }

    private void metaData(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        int durationInMilis = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        int durationInSeconds = durationInMilis / 1000;
        String durationFormatted = formattedTime(durationInSeconds);
        duration_finished.setText(durationFormatted);

        Glide.with(this).load(music_Files.get(position).getImage()).into(cover_art);
    }

    //Hàm thông báo khi kết nối service thành công / ngắt kết nối
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
            musicService = myBinder.getService();
            musicService.setCallBack(this);
            seekBar.setMax(musicService.getDuration() / 1000);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            cover_art.setImageURI(Uri.parse(listSongs.get(position).getImage()));
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.baseline_pause);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    @Override
    public void onSongChanged() {
        if (musicService != null) {
            int newPosition = musicService.getCurrentPosition();
            song_name.setText(listSongs.get(newPosition).getTitle());
            artist_name.setText(listSongs.get(newPosition).getArtist());
            cover_art.setImageURI(Uri.parse(listSongs.get(newPosition).getImage()));
            seekBar.setProgress(newPosition / 1000);
        }
    }
}