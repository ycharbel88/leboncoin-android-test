package fr.leboncoin.data.mapper

import fr.leboncoin.data.network.model.AlbumDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun toDomain_mapsDtoToAlbumCorrectly() {
        val dto = AlbumDto(
            id = 12,
            albumId = 34,
            title = "Test Title",
            url = "https://example.com/image.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg"
        )

        val domainModel = dto.toDomain(isFavorite = true)

        assertEquals(12, domainModel.id)
        assertEquals(34, domainModel.albumId)
        assertEquals("Test Title", domainModel.title)
        assertEquals("https://example.com/image.jpg", domainModel.url)
        assertEquals("https://example.com/thumb.jpg", domainModel.thumbnailUrl)
        assertTrue(domainModel.isFavorite)
    }

    @Test
    fun toDomain_defaultIsFavoriteIsFalse() {
        val dto = AlbumDto(
            id = 1,
            albumId = 1,
            title = "Title",
            url = "url",
            thumbnailUrl = "thumb"
        )

        val domainModel = dto.toDomain()

        assertFalse(domainModel.isFavorite)
    }
}