package com.steven.hicks.PhotoService.repositories

import com.steven.hicks.PhotoService.models.Photo
import org.springframework.stereotype.Service

@Service
class PhotoService(val photoRepository: PhotoRepository) {

    fun getPhotoByFilename(fileName: String): Photo {
        requireNotNull(fileName)
        check(photoRepository.existsById(fileName))

        return photoRepository.findById(fileName).get()
    }

    fun savePhoto(photo: Photo): Photo = photoRepository.save(photo)

    fun getAllPhotos(): List<Photo> = photoRepository.findAll().filterNotNull()

    //fun getPhotosByTag(tag: String) = photoRepository.findByTag()

}