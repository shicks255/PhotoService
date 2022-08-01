package com.steven.hicks.photoService.models

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Tag(
    @Id
    val name: String
)
