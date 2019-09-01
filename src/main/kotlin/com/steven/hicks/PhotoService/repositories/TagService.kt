package com.steven.hicks.PhotoService.repositories

import com.steven.hicks.PhotoService.models.Tag
import org.springframework.stereotype.Service

@Service
class TagService(val tagRepository: TagRepository) {

    fun getAllTags(): List<Tag> = tagRepository.findAll().filterNotNull()

    fun getTagByName(name: String) = tagRepository.findById(name)

    fun saveTag(name: String) = tagRepository.save(Tag(name))


}