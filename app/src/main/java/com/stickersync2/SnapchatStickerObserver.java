package com.stickersync2;

import android.content.Context;
import android.os.FileObserver;
import java.io.File;

/**
 * Observer for detecting new stickers in the Snapchat directory.
 * Handles common Snapchat sticker extensions (.webp, .png) and forwards them
 * to StickerRepository for processing.
 */
public class SnapchatStickerObserver extends FileObserver {

    private final Context context;
    private final StickerRepository repository;

    public SnapchatStickerObserver(Context context, StickerRepository repository) {
        // Adjust path based on actual Snapchat directory structure
        super(new File("/Android/data/com.snapchat.android/data/com/snapchat/Stickers/"), true);
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

        // Simple heuristic for Snapchat sticker naming
        String sourceApp = "Snapchat";
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
        return 512; // Default width for Snapchat stickers
    }

    private int estimateHeight(File file) {
        return 512; // Default height for Snapchat stickers
    }
}