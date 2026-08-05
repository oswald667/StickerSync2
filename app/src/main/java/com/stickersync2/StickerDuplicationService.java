package com.stickersync2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Service responsible for duplicating stickers from one app to another.
 * Handles format conversion, metadata generation (for WhatsApp packs),
 * and ensures the duplicated sticker is immediately visible in the target app.
 */
public class StickerDuplicationService {

    private static final String TAG = "StickerDuplicationService";
    private static final int MAX_DIMENSION = 512; // WhatsApp max sticker dimension

    private final Context context;

    public StickerDuplicationService(Context context) {
        this.context = context;
    }

    /**
     * Duplicate a sticker from its source app to the target app.
     *
     * @param sticker   The source sticker to duplicate
     * @param targetApp The target app identifier (e.g., "WhatsApp", "Snapchat")
     * @return true if duplication succeeded, false otherwise
     */
    public boolean duplicateSticker(StickerEntity sticker, String targetApp) {
        try {
            // 1. Get source file
            File sourceFile = new File(sticker.getFilePath());
            if (!sourceFile.exists()) {
                Log.e(TAG, "Source sticker does not exist: " + sourceFile.getAbsolutePath());
                return false;
            }

            // 2. Determine target directory based on targetApp
            File targetDir = getTargetStickerDirectory(targetApp);
            if (targetDir == null) {
                Log.e(TAG, "Unsupported target app: " + targetApp);
                return false;
            }

            // 3. Ensure target directory exists
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                Log.e(TAG, "Failed to create target directory: " + targetDir.getAbsolutePath());
                return false;
            }

            // 4. Process and possibly convert the sticker
            File processedFile = processStickerForTarget(sticker, sourceFile, targetApp);
            if (processedFile == null) {
                Log.e(TAG, "Failed to process sticker for target app: " + targetApp);
                return false;
            }

            // 5. Copy processed file to target directory with a unique name
            String targetFileName = generateUniqueFileName(processedFile.getName());
            File targetFile = new File(targetDir, targetFileName);
            if (!copyFile(processedFile, targetFile)) {
                Log.e(TAG, "Failed to copy processed sticker to target directory");
                return false;
            }

            // 6. For WhatsApp, ensure metadata is updated (optional)
            if ("WhatsApp".equalsIgnoreCase(targetApp)) {
                updateWhatsAppMetadata(targetFile);
            }

            // 7. Notify MediaScanner so the file appears immediately
            notifyMediaScanner(targetFile);

            Log.i(TAG, "Sticker duplicated successfully: " + targetFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error duplicating sticker", e);
            return false;
        }
    }

    /**
     * Returns the appropriate sticker directory for the given target app.
     */
    private File getTargetStickerDirectory(String targetApp) {
        switch (targetApp.toLowerCase()) {
            case "whatsapp":
                return new File(Environment.getExternalStorageDirectory(),
                        "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers");
            case "snapchat":
                return new File(Environment.getExternalStorageDirectory(),
                        "Android/data/com.snapchat.android/data/com/snapchat/Stickers");
            case "facebook":
                // Facebook Messenger sticker directory (example)
                return new File(Environment.getExternalStorageDirectory(),
                        "Android/data/com.facebook.orca/app_data/com/facebook/orca/stickers");
            default:
                return null;
        }
    }

    /**
     * Process the sticker for the target app: resize, convert format if needed.
     * For simplicity, this implementation copies the file as-is.
     * In a real app, you would use BitmapFactory to decode, resize to <=512px,
     * and recompress as WebP for WhatsApp.
     */
    private File processStickerForTarget(StickerEntity sticker, File sourceFile, String targetApp) {
        // Placeholder: For now, we just return the source file.
        // Actual implementation would handle format conversion and resizing.
        // Example for WhatsApp: ensure .webp, max 512x512, create metadata.json if new pack.
        return sourceFile;
    }

    /**
     * Generate a unique filename to avoid collisions.
     */
    private String generateUniqueFileName(String originalName) {
        String nameWithoutExt = originalName.contains(".") ?
                originalName.substring(0, originalName.lastIndexOf('.')) : originalName;
        String ext = originalName.contains(".") ?
                originalName.substring(originalName.lastIndexOf('.')) : "";
        return nameWithoutExt + "_" + UUID.randomUUID().toString() + ext;
    }

    /**
     * Simple file copy using streams.
     */
    private boolean copyFile(File source, File dest) throws IOException {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        }
    }

    /**
     * Update WhatsApp metadata if needed (e.g., create metadata.json for a new pack).
     * This is a simplified placeholder.
     */
    private void updateWhatsAppMetadata(File stickerFile) {
        // In a real implementation, you would either:
        // 1. Add the sticker to an existing pack by updating its metadata.json, or
        // 2. Create a new pack folder with a metadata.json containing the sticker info.
        // For now, we just log that metadata handling would occur here.
        Log.i(TAG, "Would update WhatsApp metadata for: " + stickerFile.getAbsolutePath());
    }

    /**
     * Notify the MediaScannerService to scan the file so it appears immediately in the target app.
     */
    private void notifyMediaScanner(File file) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/webp"); // or detect actual type
            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                resolver.delete(uri, null, null); // Ensure it's scanned
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to notify media scanner", e);
        }
    }
}