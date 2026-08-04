package com.stickersync2;

import android.content.Context;
import android.os.FileObserver;
import java.io.File;

public class WhatsAppStickerObserver extends FileObserver {

    private final Context context;
    private final StickerRepository repository;

    public WhatsAppStickerObserver(Context context, StickerRepository repository) {
        super(new File("/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers/"), true);
        this.context = context;
        this.repository = repository;
    }

    @Override
    public void onChange(final boolean selfChange) {
        // Scan for new stickers in WhatsApp directory
        File stickersDir = getFile().listFiles();
        if (stickersDir != null) {
            for (File file : stickersDir) {
                if (file.isFile() && (file.getName().endsWith(".webp") || file.getName().endsWith(".png"))) {
                    // Process new sticker file
                    processNewSticker(file);
                }
            }
        }
    }

    private void processNewSticker(File file) {
        // Extract sticker metadata
        String fileName = file.getName();
        String packName = fileName.substring(0, fileName.lastIndexOf('.'));
        String sourceApp = "WhatsApp";
        int width = estimateWidth(file); // Simplified - actual implementation would get dimensions
        int height = estimateHeight(file);
        String format = file.getName().endsWith(".webp") ? "webp" : "png";
        long timestamp = file.lastModified();

        // Create StickerEntity and save to database
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
        // Simplified - in real app, use MediaMetadataRetriever to get actual dimensions
        return 512; // Default width for WhatsApp stickers
    }

    private int estimateHeight(File file) {
        // Simplified - in real app, use MediaMetadataRetriever to get actual dimensions
        return 512; // Default height for WhatsApp stickers
    }
}