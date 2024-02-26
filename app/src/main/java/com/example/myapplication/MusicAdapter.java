package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.viewHolder> {

    private Context music_Context;
    static ArrayList<MusicFiles> music_Files;

     MusicAdapter(Context music_Context, ArrayList<MusicFiles> music_Files) {
        this.music_Files = music_Files;
        this.music_Context = music_Context;
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
        try {
            byte[] image = getAlbumArt(music_Files.get(position).getPath());
            if (image != null) {
                Glide.with(music_Context).asBitmap()
                        .load(image)
                        .into(holder.album_Art);
            } else {
                Glide.with(music_Context)
                        .load(R.drawable.question_mark)
                        .into(holder.album_Art);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(music_Context, PlayerActivity.class);
                    i.putExtra("position", position);
                    music_Context.startActivity(i);
                }
            });

            //Menu trên item để xóa nếu muốn
            holder.menuMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(music_Context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    Toast.makeText(music_Context, "Đang xóa...", Toast.LENGTH_SHORT).show();
                                    deleteFile(position, v);
                                    break;
                            }
                            return true;
                        }
                    });
                }

            });
        }
        catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, e.g., load a default image or log the error
            Glide.with(music_Context)
                    .load(R.drawable.question_mark)
                    .into(holder.album_Art);
        }
    }

    //Xóa file nhạc
    private void deleteFile(final int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(music_Files.get(position).getId())); //Context//

        File file = new File(music_Files.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            music_Context.getContentResolver().delete(contentUri, null, null);
            music_Files.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, music_Files.size());
            Snackbar.make(v, "Đã xóa", Snackbar.LENGTH_SHORT).show();
        }
        else {
            Snackbar.make(v, "Không thể xóa", Snackbar.LENGTH_SHORT).show();
        }
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

    //Giải thích MetadataRetriever: Truy xuất info từ các tệp multimedia: music, video,... Truy xuất nội dung của tệp: Title, Artist, Album,...
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

    //26 - 02 - 2024
    void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
         music_Files = new ArrayList<>();
         music_Files.addAll(musicFilesArrayList);
         notifyDataSetChanged();
    }
}
