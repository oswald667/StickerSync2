package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerRepository;

public class TikTokStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public TikTokStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("TikTok");
    }

    public LiveData<List<StickerEntity>> getStickers() {
        return stickers;
    }

    public void refreshStickers() {
        // Force refresh: we could call repository to requery
        // For now, just touch the livedata
        repository.getStickersBySourceApp("TikTok");
    }
}