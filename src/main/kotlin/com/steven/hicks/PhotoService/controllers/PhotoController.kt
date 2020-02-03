package com.steven.hicks.PhotoService.controllers

import com.steven.hicks.PhotoService.getThumbnailName
import com.steven.hicks.PhotoService.models.Photo
import com.steven.hicks.PhotoService.repositories.PhotoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Path
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/image")
class PhotoController(private val photoService: PhotoService) {

    @Value("\${photos.folder}")
    private var PHOTOS_PATH: String = ""

    @GetMapping
    fun getAllPhotos(): List<Photo> {
        val allPhotos = photoService.getAllPhotos()
        return allPhotos
    }

    @GetMapping("/{fileName}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getPhotoByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(PHOTOS_PATH + File.separator + "compressed" + File.separator + fileName).toFile()
        file.inputStream().use { stream ->
            StreamUtils.copy(stream, response.outputStream)
        }
    }

    @GetMapping("/{fileName}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getThumbnailByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(PHOTOS_PATH + File.separator + "thumbnails" + File.separator + fileName.getThumbnailName()).toFile()
        file.inputStream().use { stream ->
            StreamUtils.copy(stream, response.outputStream)
        }
    }

    @GetMapping("/{fileName}/tiny", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getTinyByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(PHOTOS_PATH + File.separator + "downScaled" + File.separator + fileName).toFile()
        file.inputStream().use { stream ->
            StreamUtils.copy(stream, response.outputStream)
        }
    }

}