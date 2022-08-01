package com.steven.hicks.photoService.service

import com.steven.hicks.photoService.models.Tag
import com.steven.hicks.photoService.repositories.TagRepository
import org.springframework.stereotype.Service

@Service
class TagService(val tagRepository: TagRepository) {

    fun deleteAll() = tagRepository.deleteAll()

    fun getAllTags(): List<Tag> = tagRepository.findAll().filterNotNull()

    fun getTagByName(name: String) = tagRepository.findById(name)

    fun saveTag(name: String) = tagRepository.save(Tag(name))

    fun createIfNotExists(tagName: String): Tag {
        if (!tagRepository.existsById(tagName))
            synchronized(this) {
                return saveTag(tagName)
            }
        else
            return getTagByName(tagName).get()
    }
}
