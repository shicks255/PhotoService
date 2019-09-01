package com.steven.hicks.PhotoService.controllers

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/image")
class PhotoController {

    @GetMapping(produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getAllPhotos(response: HttpServletResponse) {
        val resource = ClassPathResource("photos/picture.jpg")
        response.contentType = MediaType.IMAGE_JPEG_VALUE
        StreamUtils.copy(resource.inputStream, response.outputStream)
    }

}