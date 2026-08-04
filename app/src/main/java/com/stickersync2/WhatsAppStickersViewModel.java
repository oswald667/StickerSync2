package com.stickersync2;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.stickersync2.StickerEntity;
import com.stickersync2.StickerRepository;

public class WhatsAppStickersViewModel extends ViewModel {

    private final StickerRepository repository;
    private final LiveData<List<StickerEntity>> stickers;

    public WhatsAppStickersViewModel(@NonNull Application application) {
        repository = new StickerRepository(application);
        stickers = repository.getStickersBySourceApp("WhatsApp");
    }

    public WhatsAppStickersViewModel(WhatsAppStickersFragment fragment) {
        this(fragment.getActivity().getApplication());
    }

    public LiveData<List<StickerEntity>> getStickers() {
        return stickers;
    }

    public void refreshStickers() {
        // Force refresh - call repository method if needed
        // This will be handled by the observer triggering
        // We could also call the observer or trigger a file rescan
    }
}