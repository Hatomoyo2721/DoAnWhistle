package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.LoginReg.AccountInfoActivity;
import com.example.myapplication.LoginReg.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final int REQUEST_CODE = 1;
    private static final String TAG = "songs";
    public static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static ArrayList<MusicFiles> albums = new ArrayList<>();
    private final String MY_SORT_PREF = "SortOrder";
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String ARTIST_TO_FRAG = null;
    public static String SONG_NAME_TO_FRAG = null;
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        musicFiles = new ArrayList<>();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent intentLogout = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intentLogout);
                        Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case R.id.about:
                        FirebaseUser userLogin = FirebaseAuth.getInstance().getCurrentUser();
                        if (userLogin != null) {
                            Intent intent = new Intent(MainActivity.this, AccountInfoActivity.class);
                            startActivity(intent);
                            break;
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                        }
                    case R.id.list_favourite_songs:
                        Toast.makeText(MainActivity.this, "List Favorite Songs", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.share:
                        Toast.makeText(MainActivity.this, "Share", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rate_us:
                        Toast.makeText(MainActivity.this, "Rate us", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        TextView drawerName = headerView.findViewById(R.id.drawer_name);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            DocumentReference documentReference = firestore.collection("users").document(firebaseUser.getUid());
            documentReference.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    name = "Hello, " + name;
                    drawerName.setText(name);
                } else {
                    drawerName.setText("Hello, User");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    drawerName.setText("Hello, User");
                }
            });
        }
        initViewPager();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    private void permission() { //Permission provide file, photos, music,...
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
//        } else {
//            musicFiles = getSongs(this);
//            initViewPager();
//        }
//    }

//    @Override
//    // Cấp quyền getSongs để vào ViewPager, nếu allow thì sẽ được cấp nhạc từ file trong điện thoại, nếu từ chối thì không cấp nhạc từ đó
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE) {
////            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //Do what ever you want permission related
//                musicFiles = getSongs(this);
//                initViewPager();
//            } else {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
//            }
//        }
//    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.custom_tab_layout);
                TextView tabTextView = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.customTab);
                tabTextView.setText(tab.getText());
            }
        }

    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter { //Quản lý fragments và titles
        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;

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

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    //Create search option on Menu Option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userInput = newText.toLowerCase();
                if (SongsFragment.musicAdapter != null) {
                    ArrayList<MusicFiles> searchResult = new ArrayList<>();
                    for (MusicFiles song : MainActivity.musicFiles) {
                        if (song.getTitle().toLowerCase().contains(userInput)) {
                            searchResult.add(song);
                        }
                    }
                    SongsFragment.musicAdapter.updateList(searchResult);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String userInput = newText;

        if (userInput.isEmpty()) {
            if (SongsFragment.musicAdapter != null) {
                SongsFragment.musicAdapter.updateList(musicFiles);
            }
            return true;
        }

        firebaseFirestore.collection("music")
                .whereEqualTo("title", userInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<MusicFiles> searchResult = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        MusicFiles musicFile = documentSnapshot.toObject(MusicFiles.class);
                        searchResult.add(musicFile);
                    }

                    if (SongsFragment.musicAdapter != null) {
                        SongsFragment.musicAdapter.updateList(searchResult);
                    }

                    if (searchResult.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Không tìm thấy bài hát", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        return true;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.by_default:
                break;
            case R.id.by_song:
                Collections.sort(musicFiles, (file1, file2) -> file1.getTitle().compareToIgnoreCase(file2.getTitle()));
                SongsFragment.musicAdapter.updateList(musicFiles);
                return true;
            case R.id.by_artist:
                Collections.sort(musicFiles, (file1, file2) -> file1.getArtist().compareToIgnoreCase(file2.getArtist()));
                SongsFragment.musicAdapter.updateList(musicFiles);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}