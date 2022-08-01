package com.steven.hicks.photoService.service

import com.steven.hicks.photoService.models.Photo
import com.steven.hicks.photoService.repositories.PhotoRepository
import org.springframework.stereotype.Service

@Service
class PhotoService(val photoRepository: PhotoRepository) {

    fun deleteAll() = photoRepository.deleteAll()

    fun getPhotoByFilename(fileName: String): Photo {
        check(photoRepository.existsById(fileName))

        return photoRepository.findById(fileName).get()
    }

    fun photoExists(fileName: String): Boolean = photoRepository.existsById(fileName)

    fun savePhoto(photo: Photo): Photo = photoRepository.save(photo)

    fun getAllPhotos(): List<Photo> = photoRepository.findAllByOrderByTakenDesc()
}
