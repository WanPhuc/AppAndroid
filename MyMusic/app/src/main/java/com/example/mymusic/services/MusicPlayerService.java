package com.example.mymusic.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.mymusic.models.Song;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private final IBinder binder = new MusicPlayerBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> originalSongs;
    private ArrayList<Song> playingSongs;
    private int currentSongIndex = -1;
    private boolean isShuffling = false;
    // 0 = no repeat, 1 = repeat all, 2 = repeat one
    private int repeatMode = 0;

    private PlayerListener playerListener;

    public interface PlayerListener {
        void onSongChanged(Song song);
        void onPlayerStateChanged(boolean isPlaying);
        void onPlaylistChanged(List<Song> playlist);
    }

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playingSongs == null || playingSongs.isEmpty()) return;

        if (repeatMode == 2) {
            // 🔁1 Lặp lại đúng bài hiện tại
            _playSong(playingSongs.get(currentSongIndex));
        } else if (repeatMode == 1) {
            // 🔂 Lặp toàn playlist
            playNext();
        } else {
            // ❌ Không lặp — chỉ phát tiếp nếu chưa đến bài cuối
            if (currentSongIndex < playingSongs.size() - 1) {
                playNext();
            } else {
                // Hết danh sách thì dừng luôn
                if (playerListener != null) {
                    playerListener.onPlayerStateChanged(false);
                }
            }
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (playerListener != null) {
            playerListener.onSongChanged(getCurrentSong());
            playerListener.onPlayerStateChanged(true);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void setSongs(List<Song> songs) {
        this.originalSongs = new ArrayList<>(songs);
        this.playingSongs = new ArrayList<>(songs);
        this.currentSongIndex = -1;
         if (isShuffling) {
            if (playerListener != null) {
                playerListener.onPlaylistChanged(playingSongs);
            }
        }
    }

    public void playSong(Song song) {
         if (playingSongs == null) return;
        int songIndex = playingSongs.indexOf(song);
        if (songIndex != -1) {
            this.currentSongIndex = songIndex;
            _playSong(song);
        }
    }


    private void _playSong(Song song) {
        if (song == null) return;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFileUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (playerListener != null) {
                playerListener.onPlayerStateChanged(false);
            }
        } else {
            // Nếu đã có bài đang phát rồi -> tiếp tục phát
            if (currentSongIndex != -1) {
                mediaPlayer.start();
                if (playerListener != null) {
                    playerListener.onPlayerStateChanged(true);
                }
            }
            // Nếu chưa có bài nào -> chọn bài đầu hoặc random nếu bật shuffle
            else if (playingSongs != null && !playingSongs.isEmpty()) {
                if (isShuffling) {
                    currentSongIndex = new Random().nextInt(playingSongs.size());
                } else {
                    currentSongIndex = 0;
                }
                _playSong(playingSongs.get(currentSongIndex));
            }
        }
    }


    public void playNext() {
        if (playingSongs == null || playingSongs.isEmpty()) return;

        if (isShuffling) {
            currentSongIndex = new Random().nextInt(playingSongs.size());
        } else {
            currentSongIndex = (currentSongIndex + 1) % playingSongs.size();
        }

        _playSong(playingSongs.get(currentSongIndex));
    }


    public void playPrevious() {
        if (playingSongs == null || playingSongs.isEmpty()) return;

        // Luôn phát bài trước, không cần random
        currentSongIndex = (currentSongIndex - 1 + playingSongs.size()) % playingSongs.size();

        _playSong(playingSongs.get(currentSongIndex));
    }
    public void toggleRepeat() {
        // 0 -> 1 -> 2 -> 0 vòng lại
        repeatMode = (repeatMode + 1) % 3;

        // Báo cho UI biết để đổi icon nếu cần
        if (playerListener != null) {
            playerListener.onPlayerStateChanged(isPlaying());
        }
    }
    public void toggleShuffle() {
        if (originalSongs == null || originalSongs.isEmpty()) {
            return; // Nothing to shuffle
        }
        isShuffling = !isShuffling;

        Song currentSong = null;
        if (currentSongIndex != -1 && currentSongIndex < playingSongs.size()) {
            currentSong = playingSongs.get(currentSongIndex);
        }

        if (playerListener != null) {
            playerListener.onPlaylistChanged(playingSongs);
        }
    }



    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        if (currentSongIndex != -1 && playingSongs != null && !playingSongs.isEmpty() && currentSongIndex < playingSongs.size()) {
            return playingSongs.get(currentSongIndex);
        }
        return null;
    }

    public void setPlayerListener(PlayerListener listener) {
        this.playerListener = listener;
    }

    public boolean isShuffling() {
        return isShuffling;
    }
    public int getRepeatMode() {
        return repeatMode;
    }



}
