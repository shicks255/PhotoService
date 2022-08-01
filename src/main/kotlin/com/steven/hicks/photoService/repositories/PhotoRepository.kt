package com.steven.hicks.photoService.repositories

import com.steven.hicks.photoService.models.Photo
import org.springframework.data.repository.CrudRepository

interface PhotoRepository : CrudRepository<Photo, String> {

    // open fun findByTag(): List<Photo>

    open fun findAllByOrderByTakenDesc(): List<Photo>
}
