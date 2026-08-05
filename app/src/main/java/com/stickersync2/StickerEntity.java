package com.stickersync2;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stickers")
public class StickerEntity implements Parcelable {

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

    // Parcelable implementation
    protected StickerEntity(Parcel in) {
        id = in.readString();
        sourceApp = in.readString();
        filePath = in.readString();
        packName = in.readString();
        width = in.readInt();
        height = in.readInt();
        format = in.readString();
        timestamp = in.readLong();
        isFavorite = in.readByte() != 0;
        destinationPackId = in.readString();
    }

    public static final Creator<StickerEntity> CREATOR = new Creator<StickerEntity>() {
        @Override
        public StickerEntity createFromParcel(Parcel in) {
            return new StickerEntity(in);
        }

        @Override
        public StickerEntity[] newArray(int size) {
            return new StickerEntity[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sourceApp);
        dest.writeString(filePath);
        dest.writeString(packName);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(format);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeString(destinationPackId);
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

    // Setters
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
