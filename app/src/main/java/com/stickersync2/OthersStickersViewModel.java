package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class OthersStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public OthersStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("Other");
    }

    public LiveData<List<StickerEntity>> getStickers() { return stickers; }

    public void refreshStickers() {
        repository.scanStickersForApp(getApplication(), "Other");
    }
}
