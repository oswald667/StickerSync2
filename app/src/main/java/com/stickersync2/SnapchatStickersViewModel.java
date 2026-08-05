package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class SnapchatStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public SnapchatStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("Snapchat");
    }

    public LiveData<List<StickerEntity>> getStickers() { return stickers; }

    public void refreshStickers() {
        repository.scanStickersForApp(getApplication(), "Snapchat");
    }
}
