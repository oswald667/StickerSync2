package com.stickersync2;

import android.content.Context;
import android.os.FileObserver;
import java.io.File;

/**
 * Observer for detecting new stickers in the Facebook Messenger directory.
 * Processes new stickers and stores them via StickerRepository.
 */
public class FacebookStickerObserver extends FileObserver {

    private final Context context;
    private final StickerRepository repository;

    public FacebookStickerObserver(Context context, StickerRepository repository) {
        // Adjust path based on actual Facebook app structure
        super(new File("/Android/data/com.facebook.orca/app_data/com/facebook/orca/stickers/"), true);
        this.context = context;
        this.repository = repository;
    }

    @Override
    public void onChange(final boolean selfChange) {
        File stickersDir = getFile().listFiles();
        if (stickersDir != null) {
            for (File file : stickersDir) {
                if (file.isFile() && (file.getName().endsWith(".webp") || file.getName().endsWith(".png"))) {
                    processNewSticker(file);
                }
            }
        }
    }

    private void processNewSticker(File file) {
        // Extract sticker metadata
        String fileName = file.getName();
        String packName = fileName.substring(0, fileName.lastIndexOf('.'));

        // Simple heuristic for Facebook sticker naming
        String sourceApp = "Facebook";
        int width = estimateWidth(file);
        int height = estimateHeight(file);
        String format = file.getName().endsWith(".webp") ? "webp" : "png";
        long timestamp = file.lastModified();

        StickerEntity sticker = new StickerEntity(
            java.util.UUID.randomUUID().toString(),
            sourceApp,
            file.getAbsolutePath(),
            packName,
            width,
            height,
            format,
            timestamp,
            false,
            null
        );

        repository.addSticker(sticker);
    }

    private int estimateWidth(File file) {
        return 512; // Default width for Facebook stickers
    }

    private int estimateHeight(File file) {
        return 512; // Default height for Facebook stickers
    }
}