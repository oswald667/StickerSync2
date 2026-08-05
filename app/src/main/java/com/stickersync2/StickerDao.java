package com.stickersync2;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface StickerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StickerEntity sticker);

    @Delete
    void delete(StickerEntity sticker);

    @Update
    void update(StickerEntity sticker);

    @Query("SELECT * FROM stickers ORDER BY timestamp DESC")
    LiveData<List<StickerEntity>> getAll();

    @Query("SELECT * FROM stickers WHERE sourceApp = :sourceApp ORDER BY timestamp DESC")
    LiveData<List<StickerEntity>> getAllBySourceApp(String sourceApp);
}
