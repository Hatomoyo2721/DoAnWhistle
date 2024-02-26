package com.example.myapplication;

import static com.example.myapplication.AlbumDetailsAdapter.album_Files;
import static com.example.myapplication.MainActivity.musicFiles;
import static com.example.myapplication.MainActivity.repeatBoolean;
import static com.example.myapplication.MainActivity.shuffleBoolean;
import static com.example.myapplication.MusicAdapter.music_Files;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Context;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView song_name, artist_name, duration_played, duration_finished;
    ImageView back_btn, cover_art, next_btn, prev_btn, shuffle_btn, repeat_btn;
    FloatingActionButton play_pause_btn;
    SeekBar seekBar;
    int position = -1;
    static Uri uri;
    static MediaPlayer mediaPlayer; //Để phát nhạc và sử dụng các thao tác liên quan tới phát nhạc
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    private Handler handler = new Handler(); //Xử lý cập nhật giao diện người dùng
    private Thread playThread, prevThread, nextThread; //Xử lý sự kiện: Play/Pause, Previous, Next


    //Thiết lập giao diện người dùng và gọi các phương thức khởi tạo khác
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews(); //Ánh xạ
        getIntentMethod();
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override //Cho người dùng thay đổi thời gian của nhạc đang phát
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
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
                if (mediaPlayer != null) {
                    int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
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
                }
                else {
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
                }
                else {
                    repeatBoolean = true;
                    repeat_btn.setImageResource(R.drawable.baseline_repeat_on);
                }
            }
        });
    }

    @Override //Xử lý các nút Play/Pause, Next, Previous
    protected void onResume() {
        playThreadbtn();
        nextThreadbtn();
        prevThreadbtn();
        super.onResume();
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

    private void prevBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }


            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            play_pause_btn.setBackgroundResource(R.drawable.baseline_pause);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            play_pause_btn.setBackgroundResource(R.drawable.baseline_play_arrow);
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

    private void nextBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }
            //else position will be position...

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            play_pause_btn.setBackgroundResource(R.drawable.baseline_pause);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            play_pause_btn.setBackgroundResource(R.drawable.baseline_play_arrow);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
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

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            play_pause_btn.setImageResource(R.drawable.baseline_play_arrow);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
        else {
            play_pause_btn.setImageResource(R.drawable.baseline_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPostion = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPostion);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
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
        }
        else {
            return totalout;
        }
    }

    //Trước 25 - 02 - 2024: Lấy info vị trí bài hát và khởi tạo các thành phần khác
    //25 - 02 - 2024: Hàm này mới thêm "sender" để chạy được nhạc trong Albums
    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetail")) {
            listSongs = album_Files;
        }
        else {
            listSongs = music_Files; //Change a bit here musicFiles (MainActivity) -> music_Files (MusicAdapter)
        }

        if (listSongs != null) {
            play_pause_btn.setImageResource(R.drawable.baseline_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
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
            ImageAnimation(this, cover_art, bitmap);
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
                                new int[] {swatch.getRgb(), 0x00000000});
                        gredient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBG);

                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }
                    else {
                        ImageView gredient = findViewById(R.id.imageViewGredient);
                        RelativeLayout mContainer = findViewById(R.id.container);
                        gredient.setBackgroundResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {0xff000000, 0x00000000});
                        gredient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBG = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBG);

                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else {
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
    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap) {
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (mediaPlayer != null ) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}