package com.stickersync2;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {StickerEntity.class, StickerPackEntity.class}, version = 1)
public abstract class StickerDatabase extends RoomDatabase {
    private static volatile StickerDatabase INSTANCE;

    public abstract StickerDao stickerDao();
    public abstract StickerPackDao stickerPackDao();

    public static StickerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StickerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StickerDatabase.class, "sticker_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}