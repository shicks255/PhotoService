package com.steven.hicks.photoService.controllers

import com.steven.hicks.photoService.getThumbnailName
import com.steven.hicks.photoService.models.Photo
import com.steven.hicks.photoService.service.PhotoService
import io.micrometer.core.annotation.Timed
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.file.Path
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/image")
class PhotoController(private val photoService: PhotoService) {

    @Value("\${photos.folder}")
    private var photosPath: String = ""

    @GetMapping
    @Timed
    fun getAllPhotos(): List<Photo> {
        val allPhotos = photoService.getAllPhotos()
        return allPhotos
    }

    @GetMapping("/{fileName}", produces = [MediaType.IMAGE_JPEG_VALUE])
    @Timed
    fun getPhotoByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(photosPath + File.separator + "compressed" + File.separator + fileName).toFile()
        file.inputStream().use { stream ->
            StreamUtils.copy(stream, response.outputStream)
        }
    }

    @GetMapping("/{fileName}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
    @Timed
    fun getThumbnailByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(
            photosPath + File.separator + "thumbnails" + File.separator + fileName.getThumbnailName()
        ).toFile()
        file.inputStream().use { stream ->
            StreamUtils.copy(stream, response.outputStream)
        }
    }
}
