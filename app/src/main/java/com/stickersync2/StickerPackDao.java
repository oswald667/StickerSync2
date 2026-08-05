package com.stickersync2;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StickerPackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StickerPackEntity pack);

    @Delete
    void delete(StickerPackEntity pack);

    @Query("SELECT * FROM sticker_packs")
    LiveData<List<StickerPackEntity>> getAll();

    @Query("SELECT * FROM sticker_packs WHERE sourceApp = :sourceApp")
    LiveData<List<StickerPackEntity>> getBySourceApp(String sourceApp);

    @Query("SELECT * FROM sticker_packs WHERE name = :name LIMIT 1")
    StickerPackEntity getByName(String name);
}
