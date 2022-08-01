package com.steven.hicks.photoService.models

import javax.persistence.Id

// @Entity
data class PhotoTagRecord(
    @Id
    val fileName: String,

    val weight: Int,
    val tag: Tag
)
