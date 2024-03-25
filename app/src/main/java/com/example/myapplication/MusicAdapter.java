package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.viewHolder> {

    private Context music_Context;
    static ArrayList<MusicFiles> music_Files;
    FirebaseFirestore db;
    FirebaseStorage storage;

    MusicAdapter(Context music_Context, ArrayList<MusicFiles> music_Files) {
        this.music_Files = music_Files;
        this.music_Context = music_Context;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    // Tạo và trả về 1 đối tượng 'viewHolder' khi cần một viewholder mới
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(music_Context).inflate(R.layout.music_items, parent, false);
        return new viewHolder(v);
    }

    @Override
    // Gắn data từ list 'musicFiles' vào các thành phần của mỗi 'viewHolder' đc tạo từ 'onCreateViewHolder'
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_Name.setText(music_Files.get(position).getTitle());

        loadAlbumArtFromFirestore(music_Files.get(position).getImage(), holder.album_Art);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(music_Context, PlayerActivity.class);
                i.putExtra("position", position);
                music_Context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return music_Files.size();
    }

    //Lưu trữ các thành phần UI (User interface) cho mỗi mục trong list: file + ảnh album
    public class viewHolder extends RecyclerView.ViewHolder {
        TextView file_Name;
        ImageView album_Art, menuMore;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            file_Name = itemView.findViewById(R.id.music_file_name);
            album_Art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }


    private void loadAlbumArtFromFirestore(String imageId, ImageView imageView) {
        imageId = imageId.replaceAll("//", "/");
        db.collection("music").document(imageId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("image");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Load image using Glide
                    Glide.with(music_Context).load(imageUrl).into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.question_mark);
                }
            } else {
                imageView.setImageResource(R.drawable.question_mark);
            }
        }).addOnFailureListener(e -> {
            imageView.setImageResource(R.drawable.question_mark);
            e.printStackTrace();
        });
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        music_Files.clear();
        music_Files.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
