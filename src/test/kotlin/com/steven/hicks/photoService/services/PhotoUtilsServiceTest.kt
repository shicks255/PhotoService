package com.steven.hicks.photoService.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.steven.hicks.photoService.models.Photo
import com.steven.hicks.photoService.models.Tag
import com.steven.hicks.photoService.service.PhotoService
import com.steven.hicks.photoService.service.PhotoUtilsService
import org.assertj.core.api.Assertions.assertThat
import org.imgscalr.Scalr
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import javax.imageio.ImageIO

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhotoUtilsServiceTest {

    @Mock
    lateinit var photoService: PhotoService

    lateinit var sut: PhotoUtilsService

    @BeforeEach
    fun setup() {
        sut = PhotoUtilsService(photoService, "src/test/resources")
    }

    @Test
    fun `should get photo path without subFolder`() {
        val photoPath = sut.getPhotoPath("accordFinal.jpg")

        println(photoPath)
        assertThat(photoPath).isNotNull
        assertThat(photoPath.toFile().exists()).isTrue
    }

    @Test
    fun `should get photo path with subFolder`() {
        val photoPath = sut.getPhotoPath("accordFinal.jpg", "compressed")

        println(photoPath)
        assertThat(photoPath).isNotNull
//        assertThat(photoPath.toFile().exists()).isTrue
    }

    @Test
    fun `should extractMetadata`() {
        val photo = Photo(
            fileName = "accordFinal.jpg",
            title = "My Test Photo",
            description = "This is a photo",
            lat = "",
            long = "",
            altitude = "",
            exposureTime = "",
            fNumber = "",
            iso = "",
            focalLength = "",
            lensModel = "",
            addedOn = LocalDateTime.MAX,
            taken = LocalDateTime.MAX,
            tags = emptyList()
        )

        val photoWithMetadata = sut.extractMetadata(photo)

        assertThat(photoWithMetadata.lat).isEqualTo("40.529587N")
        assertThat(photoWithMetadata.long).isEqualTo("74.8768562W")
        assertThat(photoWithMetadata.exposureTime).isEqualTo("1/9")
        assertThat(photoWithMetadata.fNumber).isEqualTo("10")
        assertThat(photoWithMetadata.iso).isEqualTo("200")
        assertThat(photoWithMetadata.focalLength).isEqualTo("18")
        assertThat(photoWithMetadata.lensModel).isEqualTo("XF18-55mmF2.8-4 R LM OIS")
    }

    @Test
    fun `should return true when photo has changed`() {
        val photo = Photo(
            fileName = "accordFinal.jpg",
            title = "My Test Photo",
            description = "This is a photo",
            lat = "",
            long = "",
            altitude = "",
            exposureTime = "",
            fNumber = "",
            iso = "",
            focalLength = "",
            lensModel = "",
            addedOn = LocalDateTime.MAX,
            taken = LocalDateTime.MAX,
            tags = emptyList()
        )

        val otherPhoto = photo.copy(
            lensModel = "Fuji XT3"
        )

        val response = sut.hasPhotoChanged(photo, otherPhoto)

        assertThat(response).isTrue
    }

    @Test
    fun `should return false when photo has not changed`() {
        val photo = Photo(
            fileName = "accordFinal.jpg",
            title = "My Test Photo",
            description = "This is a photo",
            lat = "",
            long = "",
            altitude = "",
            exposureTime = "",
            fNumber = "",
            iso = "",
            focalLength = "",
            lensModel = "",
            addedOn = LocalDateTime.MAX,
            taken = LocalDateTime.MAX,
            tags = listOf(Tag("test"))
        )

        val response = sut.hasPhotoChanged(photo, photo.copy(addedOn = LocalDateTime.MIN))

        assertThat(response).isFalse
    }

    @Test
    fun `should create a resizedImage`() {
        sut.createResizedImage {
            Pair(
                Scalr.resize(
                    ImageIO.read(sut.getPhotoPath("accordFinal.jpg").toFile()),
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    PhotoUtilsService.wideDimension.width,
                    PhotoUtilsService.wideDimension.height
                ),
                sut.getPhotoPath("accordFinal-wideScreen.jpg")
            )
        }

        assertThat(sut.getPhotoPath("accordFinal-wideScreen.jpg").toFile().exists()).isTrue
    }

    @Test
    fun `should do the whole flow`() {
        val photo = Photo(
            fileName = "accordFinal.jpg",
            title = "My Test Photo",
            description = "This is a photo",
            lat = "",
            long = "",
            altitude = "",
            exposureTime = "",
            fNumber = "",
            iso = "",
            focalLength = "",
            lensModel = "",
            addedOn = LocalDateTime.MAX,
            taken = LocalDateTime.MAX,
            tags = listOf(Tag("test"))
        )

        sut.createAndResizePhoto(photo)

        assertThat(sut.getPhotoPath("accordFinal.jpg", "compressed").toFile().exists()).isTrue
        assertThat(sut.getPhotoPath("accordFinal.jpg", "thumbnails").toFile().exists()).isTrue
        verify(photoService, times(1))
            .savePhoto(any())
    }

    @AfterAll
    fun `delete test photos`() {
        Files.deleteIfExists(sut.getPhotoPath("accordFinal-wideScreen.jpg"))
        File("src/test/resources/compressed").listFiles()
            ?.forEach { Files.deleteIfExists(it.toPath()) }
        File("src/test/resources/thumbnails").listFiles()
            ?.forEach { Files.deleteIfExists(it.toPath()) }
    }
}
