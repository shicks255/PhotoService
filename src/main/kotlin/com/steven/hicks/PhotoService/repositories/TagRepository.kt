package com.steven.hicks.PhotoService.repositories

import com.steven.hicks.PhotoService.models.Tag
import org.springframework.data.repository.CrudRepository

interface TagRepository: CrudRepository<Tag, String>