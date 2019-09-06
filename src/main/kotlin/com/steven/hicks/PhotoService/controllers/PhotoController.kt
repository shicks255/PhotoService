package com.steven.hicks.PhotoService.controllers

import com.steven.hicks.PhotoService.getThumbnailName
import com.steven.hicks.PhotoService.models.Photo
import com.steven.hicks.PhotoService.repositories.PhotoService
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
    private var PHOTOS_PATH: String = ""

    @GetMapping
    fun getAllPhotos(): List<Photo> {
        val allPhotos = photoService.getAllPhotos()
        return allPhotos
    }

    @GetMapping(value = "/{fileName}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getPhotoByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(PHOTOS_PATH + File.separator + fileName).toFile()

        val stream = file.inputStream()

        StreamUtils.copy(stream, response.outputStream)
    }

    @GetMapping(value = "/{fileName}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getThumbnailByName(response: HttpServletResponse, @PathVariable(value = "fileName") fileName: String) {
        response.contentType = MediaType.IMAGE_JPEG_VALUE

        val file = Path.of(PHOTOS_PATH + File.separator + "thumbnails" + File.separator + fileName.getThumbnailName()).toFile()

        val stream = file.inputStream()

        StreamUtils.copy(stream, response.outputStream)
    }

}