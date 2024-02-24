package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; //Các hoạt động sử dụng hành vi ứng dụng với thanh hành động ở trên và nút quay lại
import androidx.core.app.ActivityCompat; //Hỗ trợ permissions trong Android
import androidx.core.content.ContextCompat; //Cung cấp phương tiện tiện ích cho việc làm việc với ngữ cảnh (context)
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Currency;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static ArrayList<MusicFiles> albums = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
    }

    private void permission() { //Permission provide file, photos, music,...
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //(Có dòng import android.Manifest mới xài được WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , REQUEST_CODE);
        }
        else {
            musicFiles = getSongs(this);
            initViewPager();
        }
    }

    @Override
    // Cấp quyền getSongs để vào ViewPager, nếu allow thì sẽ được cấp nhạc từ file trong điện thoại, nếu từ chối thì không cấp nhạc từ đó
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do what ever you want permission related
                Toast.makeText(this, "Quyền đã được cấp", Toast.LENGTH_LONG).show();
                musicFiles = getSongs(this);
                initViewPager();
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE);
                Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViewPager() { //Function show fragments - ViewPager + TabLayout cho 2 fragments: Song + Album
        ViewPager vPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter vPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        vPagerAdapter.addFragments(new SongsFragment(), "Songs");
        vPagerAdapter.addFragments(new AlbumFragment(), "Album");
        vPager.setAdapter(vPagerAdapter);
        tabLayout.setupWithViewPager(vPager);
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter { //Quản lý fragments và titles
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public static ArrayList<MusicFiles> getSongs(Context context) {
        ArrayList<String> duplicate = new ArrayList<>();

        ArrayList<MusicFiles> tempAudioList = new ArrayList<MusicFiles>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //Uniform Resource Identifier - Nhận diện tài nguyên thống nhất
        //VD tài nguyên mạng như: documents, pictures, photos, audios, videos,...
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, //Data for path (đường dẫn)
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                @SuppressLint("Range")String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range")String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                @SuppressLint("Range")String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                @SuppressLint("Range")String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                @SuppressLint("Range")String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);
                //Log.e for check info music
                Log.e("Path: " + path, "Album" + album);
                tempAudioList.add(musicFiles);

                //25 - 02 - 2024: Đã add thêm code -> Album không bị duplicate
                if (!duplicate.contains(album)) {
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return tempAudioList;
    }
}