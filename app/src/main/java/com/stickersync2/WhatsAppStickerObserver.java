package com.stickersync2;

import android.content.Context;
import android.os.Environment;
import android.os.FileObserver;
import java.io.File;
import java.util.UUID;

public class WhatsAppStickerObserver extends FileObserver {

    private final Context context;
    private final StickerRepository repository;
    private final File watchedDir;
    private static final String SOURCE_APP = "WhatsApp";

    public WhatsAppStickerObserver(Context context, StickerRepository repository) {
        super(getStickerDir().getAbsolutePath(), FileObserver.CREATE | FileObserver.MOVED_TO);
        this.context = context;
        this.repository = repository;
        this.watchedDir = getStickerDir();
    }

    private static File getStickerDir() {
        File sd = Environment.getExternalStorageDirectory();
        switch (SOURCE_APP) {
            case "WhatsApp":
                return new File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers");
            case "Snapchat":
                return new File(sd, "Android/data/com.snapchat.android/data/com/snapchat/Stickers");
            case "Facebook":
                return new File(sd, "Android/data/com.facebook.orca/app_data/stickers");
            default:
                return sd;
        }
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null) return;
        File file = new File(watchedDir, path);
        if (file.isFile() && (path.endsWith(".webp") || path.endsWith(".png") || path.endsWith(".gif"))) {
            StickerEntity sticker = new StickerEntity(
                UUID.randomUUID().toString(),
                SOURCE_APP,
                file.getAbsolutePath(),
                SOURCE_APP + " Pack",
                512, 512, path.substring(path.lastIndexOf('.') + 1),
                file.lastModified(), true, null
            );
            repository.addSticker(sticker);
        }
    }
}
