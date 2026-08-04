package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerRepository;

public class FacebookStickersViewModel extends AndroidViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public FacebookStickersViewModel(@NonNull Application application) {
        super(application);
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("Facebook");
    }

    public LiveData<List<StickerEntity>> getStickers() {
        return stickers;
    }

    public void refreshStickers() {
        // For simplicity, we'll just trigger a requery by re-assigning
        // In a more complex implementation, this could trigger a file rescan
        repository.getStickersBySourceApp("Facebook");
    }
}