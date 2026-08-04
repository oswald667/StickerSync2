@Dao
public interface StickerDao {
    @Insert
    void insert(StickerEntity sticker);

    @Delete
    void delete(StickerEntity sticker);

    @Update
    void update(StickerEntity sticker);

    @Query("SELECT * FROM stickers")
    LiveData<List<StickerEntity>> getAll();

    @Query("SELECT * FROM stickers WHERE sourceApp = :sourceApp")
    LiveData<List<StickerEntity>> getAllBySourceApp(String sourceApp);
}