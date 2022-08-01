package com.steven.hicks.photoService.repositories

import com.steven.hicks.photoService.models.Tag
import org.springframework.data.repository.CrudRepository

interface TagRepository : CrudRepository<Tag, String>
