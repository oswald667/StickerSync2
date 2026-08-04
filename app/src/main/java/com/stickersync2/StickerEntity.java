package com.stickersync2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stickers")
public class StickerEntity {
    @PrimaryKey
    private String id;
    private String sourceApp;
    private String filePath;
    private String packName;
    private int width;
    private int height;
    private String format;
    private long timestamp;
    private boolean isFavorite;
    private String destinationPackId;

    // Constructor
    public StickerEntity(String id, String sourceApp, String filePath, String packName,
                        int width, int height, String format, long timestamp,
                        boolean isFavorite, String destinationPackId) {
        this.id = id;
        this.sourceApp = sourceApp;
        this.filePath = filePath;
        this.packName = packName;
        this.width = width;
        this.height = height;
        this.format = format;
        this.timestamp = timestamp;
        this.isFavorite = isFavorite;
        this.destinationPackId = destinationPackId;
    }

    // Getters
    public String getId() { return id; }
    public String getSourceApp() { return sourceApp; }
    public String getFilePath() { return filePath; }
    public String getPackName() { return packName; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getFormat() { return format; }
    public long getTimestamp() { return timestamp; }
    public boolean isFavorite() { return isFavorite; }
    public String getDestinationPackId() { return destinationPackId; }

    // Setters (if needed)
    public void setId(String id) { this.id = id; }
    public void setSourceApp(String sourceApp) { this.sourceApp = sourceApp; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setPackName(String packName) { this.packName = packName; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setFormat(String format) { this.format = format; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setFavorite(boolean isFavorite) { this.isFavorite = isFavorite; }
    public void setDestinationPackId(String destinationPackId) { this.destinationPackId = destinationPackId; }
}