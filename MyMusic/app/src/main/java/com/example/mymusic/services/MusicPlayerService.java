package com.example.mymusic.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class MusicPlayerService extends android.app.Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
    private final IBinder binder = new MusicPlayerBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> originalSongs;
    private ArrayList<Song> playingSongs;
    private ArrayList<Artist> artistList;
    private int currentSongIndex = -1;
    private boolean isShuffling = false;
    private int repeatMode = 0; // 0: no repeat, 1: repeat all, 2: repeat one
    private ArrayList<Song> songHistory = new ArrayList<>(); // Lịch sử phát bài
    private ArrayList<Song> transientSongs;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "music_channel";
    //private PlayerListener playerListener;
    private final List<PlayerListener> playerListeners = new ArrayList<>();
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private MediaSessionCompat mediaSession;
    public static final String ACTION_PLAY_PAUSE = "com.example.mymusic.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.example.mymusic.ACTION_NEXT";
    public static final String ACTION_PREV = "com.example.mymusic.ACTION_PREV";

    public ArrayList<Song> getTransientSongs() {
        return transientSongs;
    }

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
        Log.d("SERVICE_DEBUG", "Service created, ready for foreground playback");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    try {
                        progressHandler.postDelayed(this, 1000);
                    } catch (IllegalStateException e) {
                        // Ignore
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        if (mediaSession == null) {
            mediaSession = new MediaSessionCompat(this, "MyMusicSession");
        }
        // Set callback để handle notification/media controls
        mediaSession.setCallback(new MediaSessionCallback());
        // Enable flags cho media buttons (headset) và transport controls (notification/lockscreen)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    // Gỡ listener
    public void removePlayerListener(PlayerListener listener) {
        playerListeners.remove(listener);
    }

    // 🔔 Gửi sự kiện đến tất cả listener
    private void notifySongChanged(Song song) {
        for (PlayerListener listener : new ArrayList<>(playerListeners)) {
            if (listener != null) listener.onSongChanged(song);
        }
    }

    private void notifyPlayerStateChanged(boolean isPlaying) {
        for (PlayerListener listener : new ArrayList<>(playerListeners)) {
            if (listener != null) listener.onPlayerStateChanged(isPlaying);
        }
    }

    private void notifyPlaylistChanged(List<Song> playlist) {
        for (PlayerListener listener : new ArrayList<>(playerListeners)) {
            if (listener != null) listener.onPlaylistChanged(playlist);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playingSongs == null || playingSongs.isEmpty()) return;
        progressHandler.removeCallbacks(updateProgressRunnable);
        updateMediaSessionState(false);  // Update state paused khi completion

        if (repeatMode == 2) { // Repeat One
            _playSong(playingSongs.get(currentSongIndex));
        } else { // No Repeat or Repeat All
            playNext();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        notifySongChanged(getCurrentSong());
        notifyPlayerStateChanged(true);
        updateMediaSessionState(true);
        updateNotification();
        progressHandler.post(updateProgressRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest != null) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                }
            } else {
                audioManager.abandonAudioFocus(this);
            }
        }
    }

    public void setSongs(List<Song> songs) {
        ArrayList<Song> newSongs = new ArrayList<>(songs);

        if (isActuallyPlaying() && originalSongs != null && originalSongs.equals(newSongs)) {
            transientSongs = null;
            notifyPlaylistChanged(playingSongs);
            return;
        }

        if (isActuallyPlaying()) {
            this.transientSongs = newSongs;
            notifyPlaylistChanged(playingSongs);
        } else {
            this.originalSongs = newSongs;
            this.playingSongs = new ArrayList<>(newSongs);
            this.transientSongs = null;
            this.currentSongIndex = -1;
            songHistory.clear();
            isShuffling = false;
            repeatMode = 0;
            notifyPlaylistChanged(playingSongs);
        }
    }

    public void setArtists(List<Artist> artists) {
        this.artistList = new ArrayList<>(artists);
    }

    public void playSong(Song song) {
        int songIndex = -1;

        if (transientSongs != null && transientSongs.contains(song)) {
            this.originalSongs = new ArrayList<>(transientSongs);
            this.playingSongs = new ArrayList<>(transientSongs);
            songHistory.clear();
            isShuffling = false;
            repeatMode = 0;
            notifyPlaylistChanged(playingSongs);
            songIndex = this.playingSongs.indexOf(song);
        } else if (playingSongs != null) {
            songIndex = playingSongs.indexOf(song);
        }

        transientSongs = null;

        if (songIndex != -1) {
            Song currentSong = getCurrentSong();
            if (currentSong != null && !currentSong.equals(song)) {
                songHistory.add(currentSong);
            }
            this.currentSongIndex = songIndex;
            _playSong(song);
        }
    }

    private boolean requestAudioFocus() {
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void _playSong(Song song) {
        if (song == null || !requestAudioFocus()) return;
        progressHandler.removeCallbacks(updateProgressRunnable);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFileUrl());
            // Show notification ngay (với paused state, sẽ update khi prepared)
            updateNotification();
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyPlayerStateChanged(false);
        } else {
            mediaPlayer.start();
            notifyPlayerStateChanged(true);
        }
        updateNotification();
    }

    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            progressHandler.removeCallbacks(updateProgressRunnable);
            try {
                mediaPlayer.prepare(); // Chuẩn bị lại để có thể play lại sau
            } catch (IOException e) {
                e.printStackTrace();
            }
            notifyPlayerStateChanged(false);
            updateMediaSessionState(false);  // Update state stopped/paused
            updateNotification();
        }
    }

    public void playNext() {
        if (playingSongs == null || playingSongs.isEmpty()) return;
        Song currentSong = getCurrentSong();
        if (currentSong != null) {
            songHistory.add(currentSong);
        }

        if (isShuffling) {
            if (playingSongs.size() > 1) {
                int newIndex;
                do {
                    newIndex = new Random().nextInt(playingSongs.size());
                } while (newIndex == currentSongIndex);
                currentSongIndex = newIndex;
            } else {
                currentSongIndex = 0;
            }
        } else {
            currentSongIndex = (currentSongIndex + 1) % playingSongs.size();
        }
        _playSong(playingSongs.get(currentSongIndex));
    }

    public void playPrevious() {
        if (playingSongs == null || playingSongs.isEmpty()) return;

        // If song has played for more than 2 seconds, restart it.
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 2000) {
            _playSong(getCurrentSong());
            return;
        }

        // Otherwise, play the previous song.
        if (isShuffling && !songHistory.isEmpty()) {
            // In shuffle mode, retrieve the previous song from history.
            Song previousSong = songHistory.remove(songHistory.size() - 1);
            currentSongIndex = playingSongs.indexOf(previousSong);
            _playSong(previousSong);
        } else if (!isShuffling) {
            // In normal mode, just go to the previous index.
            currentSongIndex = (currentSongIndex - 1 + playingSongs.size()) % playingSongs.size();
            _playSong(playingSongs.get(currentSongIndex));
        }
        // If shuffling and history is empty, do nothing.
    }

    public void toggleRepeat() {
        repeatMode = (repeatMode + 1) % 3;
    }

    public void toggleShuffle() {
        isShuffling = !isShuffling;
        notifyPlaylistChanged(playingSongs);
    }

    public boolean isActuallyPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isPlaying() {
        if (transientSongs != null) {
            return false;
        }
        return isActuallyPlaying();
    }

    public boolean isShuffling() {
        if (transientSongs != null) {
            return false;
        }
        return isShuffling;
    }

    public boolean isSongImageVisible() {
        return transientSongs == null;
    }

    public int getRepeatMode() {
        if (transientSongs != null) {
            return 0;
        }
        return repeatMode;
    }

    public Song getCurrentSong() {

        if (currentSongIndex != -1 && playingSongs != null && !playingSongs.isEmpty() && currentSongIndex < playingSongs.size())
            return playingSongs.get(currentSongIndex);
        return null;
    }

    public void addPlayerListener(PlayerListener listener) {
        if (listener != null && !playerListeners.contains(listener)) {
            playerListeners.add(listener);
        }
    }

    public int getCurrentPlaylistSize() {
        return (originalSongs != null) ? originalSongs.size() : 0;
    }

    public ArrayList<Song> getOriginalSongs() {
        return originalSongs;
    }

    private String getArtistName(String artistId) {
        if (artistList == null || artistId == null) return "Unknown Artist";
        for (Artist artist : artistList) {
            if (artistId.equals(artist.getArtistID())) return artist.getName();
        }
        return "Unknown Artist";
    }

    // Helper method để update MediaSession state (gọi ở các nơi thay đổi playback)
    private void updateMediaSessionState(boolean isPlaying) {
        if (mediaSession == null || getCurrentSong() == null) return;

        long position = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        position,
                        isPlaying ? 1f : 0f)
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                (isPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY))
                .build());
    }

    private void createNotification(Song song, boolean isPlaying, @Nullable Bitmap albumArt) {
        if (mediaSession == null) {
            mediaSession = new MediaSessionCompat(this, "MyMusicSession");
            mediaSession.setCallback(new MediaSessionCallback());
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mediaSession.setActive(true);
        }

        Bitmap finalAlbumArt = albumArt != null ? albumArt : BitmapFactory.decodeResource(getResources(), R.drawable.img_default_song);

        // Update metadata cho lockscreen/media session
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName(song.getArtistID()))
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, finalAlbumArt)
                .build());

        // Update playback state
        updateMediaSessionState(isPlaying);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;

        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, new Intent(this, MusicPlayerService.class).setAction(ACTION_PREV), flags);
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, new Intent(this, MusicPlayerService.class).setAction(ACTION_PLAY_PAUSE), flags);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, new Intent(this, MusicPlayerService.class).setAction(ACTION_NEXT), flags);

        int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
        String playPauseTitle = isPlaying ? "Pause" : "Play";

        // Tạo notification với MediaStyle
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_apple_music_icon)
                .setContentTitle(song.getTitle())
                .setContentText(getArtistName(song.getArtistID()))
                .setLargeIcon(finalAlbumArt)
                .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying);

        Notification notification = notificationBuilder.build();

        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            stopForeground(false);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);
            }
        }
    }

    // ✅ Tiếp từ dòng 493: Hoàn thiện updateNotification()
    private void updateNotification() {
        Song current = getCurrentSong();
        if (current == null) return;

        boolean isPlaying = isActuallyPlaying();

        Glide.with(this)
                .asBitmap()
                .load(current.getCoverUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        createNotification(current, isPlaying, resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        createNotification(current, isPlaying, null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            Log.d("MUSIC_SERVICE", "Action received: " + intent.getAction());
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREV:
                    //playPrevious();
                    break;
            }
        } else {
            Log.d("MUSIC_SERVICE", "onStartCommand called but intent/action null");
        }
        return START_STICKY;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!mediaPlayer.isPlaying()) {
                    playPause();  // Resume nếu paused
                }
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                updateMediaSessionState(isActuallyPlaying());  // Update state dựa trên current
                updateNotification();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    playPause();  // Pause vĩnh viễn
                }
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(0f, 0f);
                }
                updateMediaSessionState(false);
                updateNotification();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    playPause();  // Pause tạm thời
                }
                updateMediaSessionState(false);
                updateNotification();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(0.1f, 0.1f);  // Giảm volume tạm thời
                    }
                }
                updateMediaSessionState(isActuallyPlaying());  // Giữ state playing nhưng volume thấp
                updateNotification();
                break;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
            Log.d("MEDIA_SESSION", "onPlay() called from notification/media button");  // Log debug
            playPause();  // Gọi logic toggle của bạn (nếu paused thì play, nếu playing thì pause)
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d("MEDIA_SESSION", "onPause() called from notification/media button");  // Log debug
            if (isActuallyPlaying()) {
                playPause();  // Chỉ pause nếu đang playing
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.d("MEDIA_SESSION", "onSkipToNext() called from notification");  // Log debug
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.d("MEDIA_SESSION", "onSkipToPrevious() called from notification");  // Log debug
            playPrevious();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            Log.d("MEDIA_SESSION", "onMediaButtonEvent called (headset)");
            return super.onMediaButtonEvent(mediaButtonIntent);
        }
    }
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

}
