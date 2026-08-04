package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerRepository;

public class OthersStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public OthersStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("Autres");
    }

    public LiveData<List<StickerEntity>> getStickers() {
        return stickers;
    }

    public void refreshStickers() {
        // Trigger refresh
        repository.getStickersBySourceApp("Autres");
    }
}