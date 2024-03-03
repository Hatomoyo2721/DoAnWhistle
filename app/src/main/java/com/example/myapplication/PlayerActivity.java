package com.example.myapplication;

import static com.example.myapplication.AlbumDetailsAdapter.album_Files;
import static com.example.myapplication.ApplicationClass.ACTION_NEXT;
import static com.example.myapplication.ApplicationClass.ACTION_PLAY;
import static com.example.myapplication.ApplicationClass.ACTION_PREVIOUS;
import static com.example.myapplication.ApplicationClass.CHANNEL_ID_2;
import static com.example.myapplication.MainActivity.musicFiles;
import static com.example.myapplication.MainActivity.repeatBoolean;
import static com.example.myapplication.MainActivity.shuffleBoolean;
import static com.example.myapplication.MusicAdapter.music_Files;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.nio.channels.InterruptedByTimeoutException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Random;



public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

    private TextView song_name, artist_name, duration_played, duration_finished;
    private ImageView back_btn, cover_art, next_btn, prev_btn, shuffle_btn, repeat_btn;
    private FloatingActionButton play_pause_btn;
    private SeekBar seekBar;
    private int position = -1;
    public static Uri uri;
    //public static MediaPlayer mediaPlayer; //Để phát nhạc và sử dụng các thao tác liên quan tới phát nhạc
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    private Handler handler = new Handler(); //Xử lý cập nhật giao diện người dùng
    private Thread playThread, prevThread, nextThread; //Xử lý sự kiện: Play/Pause, Previous, Next
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    //Thiết lập giao diện người dùng và gọi các phương thức khởi tạo khác
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
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
            if (musicService.isPlaying()) {
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
                showNotification(R.drawable.baseline_pause);
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
                showNotification(R.drawable.baseline_play_arrow);
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
            if (musicService.isPlaying()) {
                musicService.stop();
                musicService.release();

                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(listSongs.size() - 1);
                } else if (!shuffleBoolean && !repeatBoolean) {
                    position = ((position + 1) % listSongs.size());
                }
                //else position will be position...

                uri = Uri.parse(listSongs.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri);
                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());

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
                showNotification(R.drawable.baseline_pause);
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
                showNotification(R.drawable.baseline_play_arrow);
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
                play_pause_btn.setImageResource(R.drawable.baseline_play_arrow);
                showNotification(R.drawable.baseline_play_arrow);
                musicService.pause();
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
                showNotification(R.drawable.baseline_pause);
                play_pause_btn.setImageResource(R.drawable.baseline_pause);
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

    //Chuyển đơn vị giây -> phút:giây
    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalout;
        }
    }

    //Trước 25 - 02 - 2024: Lấy info vị trí bài hát và khởi tạo các thành phần khác
    //25 - 02 - 2024: Hàm này mới thêm "sender" để chạy được nhạc trong Albums
    private void getIntentMethod() {
        try {
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
            }
            showNotification(R.drawable.baseline_pause);
            Intent i = new Intent(this, MusicService.class);
            i.putExtra("servicePosition", position);
            startService(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    }

    //Giải thích MetadataRetriever: Truy xuất info từ các tệp multimedia: music, video,... Truy xuất nội dung của tệp: Title, Artist, Album,...
    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_finished.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            imageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gredient = findViewById(R.id.imageViewGredient);
                        RelativeLayout mContainer = findViewById(R.id.container);
                        gredient.setBackgroundResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gredient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBG);

                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    } else {
                        ImageView gredient = findViewById(R.id.imageViewGredient);
                        RelativeLayout mContainer = findViewById(R.id.container);
                        gredient.setBackgroundResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gredient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBG);

                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.question_mark)
                    .into(cover_art);

            ImageView gredient = findViewById(R.id.imageViewGredient);
            RelativeLayout mContainer = findViewById(R.id.container);
            gredient.setBackgroundResource(R.drawable.gredient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);

            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }

    //26 - 02 - 2024
    //Effect khi chuyển nhạc khác
    public void imageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap) {
        Animation animationOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animationIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animationOut);
    }


    //27 - 02 - 2024
    //Hàm thông báo khi kết nối service thành công / ngắt kết nối
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
//        Toast.makeText(this, "Kết nối thành công" + musicService,
//                Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.OnCompleted();
    }

    //27 - 02 - 2024
    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    void showNotification(int playPauseBtn) throws IOException {

        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,  0, intent, PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.
                getBroadcast(this, 0, prevIntent,
              PendingIntent.FLAG_IMMUTABLE);
//                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.
                getBroadcast(this, 0, pauseIntent,
              PendingIntent.FLAG_IMMUTABLE);
//               PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.
                getBroadcast(this, 0, nextIntent,
             PendingIntent.FLAG_IMMUTABLE);
//               PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = getAlbumArt(listSongs.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark);
        }

        //mediaSessionCompat didn't work
        if (mediaSessionCompat == null) {
            Log.e("PlayerActivity", "mediaSessionCompat is null");
            return;
        }
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                    .setSmallIcon(playPauseBtn)
                    .setLargeIcon(thumb)
                    .setContentTitle(listSongs.get(position).getTitle())
                    .setContentText(listSongs.get(position).getArtist())
                    .addAction(R.drawable.baseline_skip_previous, "Previous", prevPending)
                    .addAction(playPauseBtn, "Pause", pausePending)
                    .addAction(R.drawable.baseline_skip_next, "Next", nextPending)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
//                    .setContentIntent(contentIntent)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
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