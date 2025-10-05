package com.example.mymusic;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.mymusic.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SongDownloadManager {

    private static SongDownloadManager instance;
    private static final String PREFS_NAME = "MyMusicDownloads";
    private static final String DOWNLOADED_SONGS_KEY = "downloadedSongIds";
    private DownloadManager downloadManager;
    private Context context;
    private SharedPreferences sharedPreferences;

    private SongDownloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SongDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new SongDownloadManager(context);
        }
        return instance;
    }

    /**
     * Bắt đầu tải một bài hát.
     * File tải về sẽ được lưu trong thư mục nhạc riêng của ứng dụng.
     * Chúng ta sử dụng DownloadManager của Android để quá trình tải xuống được ổn định.
     *
     * Lưu ý: Để quản lý siêu dữ liệu của bài hát đã tải, chúng ta dùng SharedPreferences.
     * Firestore Offline Persistence rất tốt cho việc cache dữ liệu Firestore để xem offline,
     * nhưng để quản lý các file local, dùng SharedPreferences hoặc Room DB sẽ trực tiếp và đơn giản hơn.
     */
    public void downloadSong(Song song) {
        if (song == null || song.getSongID() == null || song.getFileUrl().isEmpty()) {
            Toast.makeText(context, "Link nhạc không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSongDownloaded(song.getSongID())) {
            Toast.makeText(context, "Bài hát đã được tải về", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(song.getFileUrl()));
        request.setTitle(song.getTitle());
        request.setDescription("Đang tải xuống");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Lưu trong thư mục nhạc riêng của ứng dụng.
        // Dùng ID bài hát làm tên file là một cách làm tốt để tránh ký tự đặc biệt và đảm bảo tính duy nhất.
        String fileName = song.getSongID() + ".mp3";
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, fileName);

        // Đưa yêu cầu tải vào hàng đợi. DownloadManager sẽ xử lý nó trong nền.
        downloadManager.enqueue(request);

        // Chúng ta sẽ lạc quan thêm bài hát vào danh sách đã tải.
        // Một cách triển khai nâng cao hơn sẽ dùng BroadcastReceiver để lắng nghe
        // DownloadManager.ACTION_DOWNLOAD_COMPLETE để xác nhận tải thành công trước khi thêm.
        addDownloadedSongId(song.getSongID());
        Toast.makeText(context, "Bắt đầu tải: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void addDownloadedSongId(String songId) {
        List<String> downloadedIds = getDownloadedSongIds();
        if (!downloadedIds.contains(songId)) {
            downloadedIds.add(songId);
            saveDownloadedSongIds(downloadedIds);
        }
    }

    public List<String> getDownloadedSongIds() {
        String idsString = sharedPreferences.getString(DOWNLOADED_SONGS_KEY, "");
        if (TextUtils.isEmpty(idsString)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(idsString.split(",")));
    }

    private void saveDownloadedSongIds(List<String> songIds) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String idsString = TextUtils.join(",", songIds);
        editor.putString(DOWNLOADED_SONGS_KEY, idsString);
        editor.apply();
    }

    public boolean isSongDownloaded(String songId) {
        return getDownloadedSongIds().contains(songId);
    }

    /**
     * Lấy File local của một bài hát đã tải.
     * @param songId ID của bài hát.
     * @return Đối tượng File nếu nó tồn tại, ngược lại trả về null.
     */
    public File getSongFile(String songId) {
        String fileName = songId + ".mp3";
        File musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (musicDir != null) {
            File songFile = new File(musicDir, fileName);
            if (songFile.exists()) {
                return songFile;
            }
        }
        // Nếu không tìm thấy file, có thể người dùng đã xóa nó.
        // Chúng ta nên xóa nó khỏi danh sách đã tải.
        removeDownloadedSongId(songId);
        return null;
    }
    
    public void removeDownloadedSongId(String songId) {
        if (songId == null) return;

        // Xóa file nhạc thật
        String fileName = songId + ".mp3";
        File musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (musicDir != null) {
            File songFile = new File(musicDir, fileName);
            if (songFile.exists()) {
                boolean deleted = songFile.delete();
                if (!deleted) {
                    Toast.makeText(context, "Không thể xóa file", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Xóa ID khỏi danh sách SharedPreferences
        List<String> downloadedIds = getDownloadedSongIds();
        if (downloadedIds.contains(songId)) {
            downloadedIds.remove(songId);
            saveDownloadedSongIds(downloadedIds);
        }
    }


}
