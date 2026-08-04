package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerRepository;

public class AllStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public AllStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        // We could add a getAllStickers method in repository; for now reuse getStickersBySourceApp with a special source or all.
        // Since repository only has getStickersBySourceApp, we'll create a method to get all.
        // For now, we'll get WhatsApp and combine? Better to add getAllStickers in repository.
        // We'll just use getStickersBySourceApp("All") but need to implement.
        stickers = repository.getAllStickers();
    }

    public LiveData<List<StickerEntity>> getStickers() {
        return stickers;
    }

    public void refreshStickers() {
        repository.getAllStickers();
    }
}