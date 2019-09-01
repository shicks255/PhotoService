package com.steven.hicks.PhotoService.repositories

import com.steven.hicks.PhotoService.models.Photo
import org.springframework.data.repository.CrudRepository


interface PhotoRepository: CrudRepository<Photo, String> {

    //open fun findByTag(): List<Photo>

}