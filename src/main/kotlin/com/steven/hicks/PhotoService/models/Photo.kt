package com.steven.hicks.PhotoService.models

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Photo(
        @Id
        val fileName: String,
        val thumbNailName: String,
        val size: Long,
        val description: String,
        val geotag: String,
        val addedOn: LocalDate,
        val taken: LocalDateTime,
        @OneToMany(fetch = FetchType.EAGER)
        val tags: List<Tag>)

