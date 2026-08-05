package com.stickersync2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StickerRepository {

    private static final String TAG = "StickerRepository";
    private final StickerDao stickerDao;
    private final StickerPackDao stickerPackDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StickerRepository(Context context) {
        StickerDatabase db = StickerDatabase.getDatabase(context);
        stickerDao = db.stickerDao();
        stickerPackDao = db.stickerPackDao();
    }

    public LiveData<List<StickerEntity>> getStickersBySourceApp(String sourceApp) {
        return stickerDao.getAllBySourceApp(sourceApp);
    }

    public LiveData<List<StickerEntity>> getAllStickers() {
        return stickerDao.getAll();
    }

    public void addSticker(StickerEntity sticker) {
        executor.execute(() -> stickerDao.insert(sticker));
    }

    public void removeSticker(StickerEntity sticker) {
        executor.execute(() -> stickerDao.delete(sticker));
    }

    public void updateSticker(StickerEntity sticker) {
        executor.execute(() -> stickerDao.update(sticker));
    }

    public void scanAllStickers(Context context) {
        executor.execute(() -> {
            scanStickersForApp(context, "WhatsApp");
            scanStickersForApp(context, "TikTok");
            scanStickersForApp(context, "Snapchat");
            scanStickersForApp(context, "Facebook");
        });
    }

    public void scanStickersForApp(Context context, String appName) {
        executor.execute(() -> {
            File dir = getStickerDirForApp(appName);
            if (dir == null || !dir.exists()) {
                Log.w(TAG, "Sticker dir not found for: " + appName);
                return;
            }
            File[] files = dir.listFiles(f -> f.getName().endsWith(".webp")
                    || f.getName().endsWith(".png")
                    || f.getName().endsWith(".gif"));
            if (files == null) return;
            for (File f : files) {
                StickerEntity entity = new StickerEntity(
                        UUID.randomUUID().toString(),
                        appName,
                        f.getAbsolutePath(),
                        appName + " Pack",
                        512, 512, "webp",
                        f.lastModified(), true, null
                );
                stickerDao.insert(entity);
            }
        });
    }

    private File getStickerDirForApp(String appName) {
        File sd = Environment.getExternalStorageDirectory();
        switch (appName) {
            case "WhatsApp":
                return new File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers");
            case "TikTok":
                return new File(sd, "Android/data/com.zhiliaoapp.musically/data/Stickers");
            case "Snapchat":
                return new File(sd, "Android/data/com.snapchat.android/data/com/snapchat/Stickers");
            case "Facebook":
                return new File(sd, "Android/data/com.facebook.orca/app_data/stickers");
            default:
                return null;
        }
    }
}
