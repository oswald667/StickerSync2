package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class FacebookStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public FacebookStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("Facebook");
    }

    public LiveData<List<StickerEntity>> getStickers() { return stickers; }

    public void refreshStickers() {
        repository.scanStickersForApp(getApplication(), "Facebook");
    }
}
