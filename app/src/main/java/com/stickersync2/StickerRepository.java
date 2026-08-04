package com.stickersync2;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.stickersync2.StickerDao;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerPackEntity;

public class StickerRepository {
    private final StickerDao stickerDao;
    private final StickerPackDao stickerPackDao;

    public StickerRepository(Context context) {
        StickerDatabase db = StickerDatabase.getDatabase(context);
        stickerDao = db.stickerDao();
        stickerPackDao = db.stickerPackDao();
    }

    public LiveData<List<StickerEntity>> getStickersBySourceApp(String sourceApp) {
        return stickerDao.getAllBySourceApp(sourceApp);
    }

    public LiveData<List<StickerPackEntity>> getStickerPacksBySourceApp(String sourceApp) {
        return stickerPackDao.getBySourceApp(sourceApp);
    }

    public void addSticker(StickerEntity sticker) {
        stickerDao.insert(sticker);
    }

    public void removeSticker(StickerEntity sticker) {
        stickerDao.delete(sticker);
    }

    public void updateSticker(StickerEntity sticker) {
        stickerDao.update(sticker);
    }

    public LiveData<List<StickerEntity>> getAllStickers() {
        return stickerDao.getAll();
    }
}