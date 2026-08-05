package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class TikTokStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public TikTokStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("TikTok");
    }

    public LiveData<List<StickerEntity>> getStickers() { return stickers; }

    public void refreshStickers() {
        repository.scanStickersForApp(getApplication(), "TikTok");
    }
}
