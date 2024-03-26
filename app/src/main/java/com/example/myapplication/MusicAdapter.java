package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.viewHolder> {

    private Context music_Context;
    static ArrayList<MusicFiles> music_Files;
    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth firebaseAuth;

    MusicAdapter(Context music_Context, ArrayList<MusicFiles> music_Files) {
        this.music_Files = music_Files;
        this.music_Context = music_Context;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
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

        loadImageSongFromFirestore(holder.image_art, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(music_Context, PlayerActivity.class);
                i.putExtra("position", position);
                music_Context.startActivity(i);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(music_Context);
                builder.setMessage("Bạn có muốn xóa bài này ?").setPositiveButton("Xác nhận", (dialog, which) -> {
                            deleteMusicFromFirebase(position);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return music_Files.size();
    }

    //Lưu trữ các thành phần UI (User interface) cho mỗi mục trong list: file + ảnh album
    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView image_art;
        TextView file_Name;
        ImageView menuMore;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            file_Name = itemView.findViewById(R.id.music_file_name);
            image_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }


    private void loadImageSongFromFirestore(ImageView imageView, int position) {
        DocumentReference documentReference = db.collection("music").document(music_Files.get(position).getId_song());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String image = document.get("image").toString().replace("//", "/");
                        if (image != null) {
                            Glide.with(music_Context).load(image).into(imageView);
                        } else {
                            Glide.with(music_Context).load(R.drawable.question_mark).into(imageView);
                        }
                    }
                }
            }
        });
    }

    private void deleteMusicFromFirebase(int position) {
        String titleSong = music_Files.get(position).getPath().replace("//", "/");
        DocumentReference documentReference = FirebaseFirestore.getInstance().document("music/" + titleSong);
        documentReference.delete()
                .addOnSuccessListener(unused -> {
                    Log.d("MusicAdapter", "Firestore music doc deleted");
                })
                .addOnFailureListener(e -> Log.w("MusicAdapter", "Firestore music doc deletion failed", e));
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        music_Files.clear();
        music_Files.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
