package com.alexis.proyecto;

import static android.content.Context.MODE_PRIVATE;
import static com.alexis.proyecto.MainActivity.ARTIST_NAME;
import static com.alexis.proyecto.MainActivity.ARTIST_TO_FRAG;
import static com.alexis.proyecto.MainActivity.PATH_TO_FRAG;
import static com.alexis.proyecto.MainActivity.SHOW_MINI_PLAYER;
import static com.alexis.proyecto.MainActivity.SONG_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlaying extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artistName, songName;
    static FloatingActionButton playPauseBtnMini;
    View view;
    MusicService musicService;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlaying() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        artistName = view.findViewById(R.id.songArtistMini);
        songName = view.findViewById(R.id.songNameMini);
        albumArt = view.findViewById(R.id.bottomAlbumArt);
        nextBtn = view.findViewById(R.id.skipNextBottom);
        playPauseBtnMini = view.findViewById(R.id.playPauseBtnMini);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService != null){
                    musicService.nextBtnClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artist = preferences.getString(ARTIST_NAME, null);
                        String song = preferences.getString(SONG_NAME, null);
                        if (path!=null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            SONG_TO_FRAG = song;
                            ARTIST_TO_FRAG = artist;
                        }
                        if (SHOW_MINI_PLAYER){
                            if (PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }
                                songName.setText(SONG_TO_FRAG);
                                artistName.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });
        playPauseBtnMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService != null){
                    musicService.playBtnClicked();
                    if (musicService.isPlaying()){
                        playPauseBtnMini.setImageResource(R.drawable.ic_pause);
                    } else {
                        playPauseBtnMini.setImageResource(R.drawable.ic_play);
                    }
                }
            }
        });
        return view;
    }

    public static void setBtn(){
        playPauseBtnMini.setImageResource(R.drawable.ic_pause);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext()).load(art).into(albumArt);
                }
                songName.setText(SONG_TO_FRAG);
                artistName.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null){
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null){
           getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte[] art = mmr.getEmbeddedPicture();
        mmr.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        musicService = binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }
}