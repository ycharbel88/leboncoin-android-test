package fr.leboncoin.data.local

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlbumDaoTest {

    private lateinit var db: AlbumDatabase
    private lateinit var dao: AlbumDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AlbumDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.albumDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── PagingSource ──────────────────────────────────────────────────────────

    @Test
    fun `getAlbumsPagingSource returns items ordered by id ascending`() = runTest {
        dao.upsertAlbums(listOf(entity(30), entity(10), entity(20)))

        val source = dao.getAlbumsPagingSource()
        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf(10, 20, 30), result.data.map { it.id })
    }

    // ── refreshAtomically – favorite preservation ─────────────────────────────

    @Test
    fun `refreshAtomically preserves favorites for albums present in both old and new set`() = runTest {
        // Seed DB: album 1 is favorite, album 2 is not
        dao.upsertAlbums(listOf(entity(1, favorite = true), entity(2)))

        // Refresh with fresh data (isFavorite=false as the repo passes)
        dao.refreshAtomically(listOf(entity(1), entity(2), entity(3)))

        val all = dao.getAlbumByIdDirect(1)
        assertTrue("Album 1 should still be favorite", all!!.isFavorite)

        val album2 = dao.getAlbumByIdDirect(2)
        assertTrue("Album 2 should not be favorite", !album2!!.isFavorite)
    }

    @Test
    fun `refreshAtomically removes albums no longer in the new set`() = runTest {
        dao.upsertAlbums(listOf(entity(1), entity(2)))

        dao.refreshAtomically(listOf(entity(1)))  // album 2 not included

        val album2 = dao.getAlbumByIdDirect(2)
        assertEquals(null, album2)
    }

    @Test
    fun `refreshAtomically does not affect isFavorite for newly added albums`() = runTest {
        dao.upsertAlbums(listOf(entity(1)))

        // Album 99 is new — not in old data
        dao.refreshAtomically(listOf(entity(1), entity(99)))

        val album99 = dao.getAlbumByIdDirect(99)
        assertEquals(false, album99?.isFavorite)
    }

    // ── Invalidation ──────────────────────────────────────────────────────────

    @Test
    fun `new PagingSource after refreshAtomically reflects updated data`() = runTest {
        dao.upsertAlbums(listOf(entity(1)))

        dao.refreshAtomically(listOf(entity(1), entity(2)))

        // A freshly obtained PagingSource should see both rows
        val newSource = dao.getAlbumsPagingSource()
        val result = newSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false,
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf(1, 2), result.data.map { it.id })
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun entity(id: Int, favorite: Boolean = false) = AlbumEntity(
        id = id, albumId = id + 100, title = "Album $id",
        url = "https://example.com/$id", thumbnailUrl = "https://example.com/t$id",
        isFavorite = favorite,
    )
}