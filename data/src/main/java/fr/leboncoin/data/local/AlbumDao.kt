package fr.leboncoin.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY id ASC")
    fun getAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY id ASC")
    fun getAlbumsPagingSource(): PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM albums WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: Int): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumByIdDirect(id: Int): AlbumEntity?

    @Query("SELECT isFavorite FROM albums WHERE id = :id")
    suspend fun isFavorite(id: Int): Boolean?

    @Query("SELECT id FROM albums WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("UPDATE albums SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums(): Int

    /**
     * Atomically replaces all album rows while preserving isFavorite for any album that the
     * user has already toggled. Running inside a transaction prevents a concurrent toggleFavorite
     * from being overwritten: the toggle's UPDATE will either commit before this transaction
     * begins, or it will wait until this transaction has committed.
     *
     * [newAlbums] should be the freshly-fetched entities with isFavorite = false; the method
     * reads the current favorite IDs and applies them before re-inserting.
     */
    @Transaction
    suspend fun refreshAtomically(newAlbums: List<AlbumEntity>) {
        val favoriteIds = getFavoriteIds().toHashSet()
        deleteAllAlbums()
        upsertAlbums(newAlbums.map { entity ->
            if (entity.id in favoriteIds) entity.copy(isFavorite = true) else entity
        })
    }
}