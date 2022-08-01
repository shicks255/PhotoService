package com.steven.hicks.photoService.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.steven.hicks.photoService.models.Photo
import com.steven.hicks.photoService.service.PhotoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(controllers = [PhotoController::class])
@ExtendWith(MockitoExtension::class)
class PhotoControllerTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var photoService: PhotoService

    fun `jj`() {
        `when`(photoService.getPhotoByFilename("test.jpg"))
            .thenReturn(
                Photo(
                    fileName = "testPhoto.jpg",
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
            )

        val response = mockMvc.perform(
            get("/image/test.jpg")
        )
            .andExpect(status().isOk)
            .andReturn()

        verify(photoService, times(1))
            .getPhotoByFilename("test.jpg")
    }

    @Test
    fun `should do something`() {

        `when`(photoService.getAllPhotos())
            .thenReturn(
                listOf(
                    Photo(
                        fileName = "testPhoto.jpg",
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
                )
            )

        val response = mockMvc.perform(
            get("/image")
        )
            .andExpect(status().isOk)
            .andReturn()

        verify(photoService, times(1))
            .getAllPhotos()
//        assertThat(objectMapper.readValue(response.response.contentAsString, TypeReference<List<Photo>>()))
//            .isNotNull
    }
}
