package com.alexis.proyecto;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {
    private Context mContext;
    static ArrayList<MusicFiles> mfiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mfiles){
        this.mfiles = mfiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int Position) {
        holder.songName.setText(mfiles.get(holder.getAdapterPosition()).getTitle());
        holder.songName.setSelected(true);
        holder.artistName.setText(mfiles.get(holder.getAdapterPosition()).getArtist());
        byte[] image = getAlbumArt(mfiles.get(holder.getAdapterPosition()).getPath());
        if (image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.albumArt);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", holder.getAdapterPosition());
                NowPlaying.setBtn();
                mContext.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) ->{
                    switch (item.getItemId()){
                        case R.id.delete:
                        deleteFile(holder.getAdapterPosition(), view);
                        break;
                    }
                    return true;
                });
            }
        });
    }

    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mfiles.get(position).getId()));
        File file = new File(mfiles.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mfiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mfiles.size());
            Snackbar.make(view, "File Deleted!", Snackbar.LENGTH_LONG).show();
        } else{
            Snackbar.make(view, "Can´t be Deleted!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mfiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView songName, artistName;
        ImageView albumArt, menuMore;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            songName = itemView.findViewById(R.id.musicFileName);
            artistName = itemView.findViewById(R.id.musicFileArtist);
            albumArt = itemView.findViewById(R.id.musicImg);
            menuMore = itemView.findViewById(R.id.more);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte[] art = mmr.getEmbeddedPicture();
        mmr.release();
        return art;
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mfiles = new ArrayList<>();
        mfiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }

}
