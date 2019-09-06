package com.steven.hicks.PhotoService.models

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Photo(
        @Id
        val fileName: String,
        val size: Long,
        val description: String?,
        val geotag: String?,
        val exposureTime: String?,
        val fNumber: String?,
        val iso: String?,
        val focalLength: String?,
        val lensModel: String?,
        val addedOn: LocalDate,
        val taken: LocalDate?,
        @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
        @JoinTable(
                name = "photo_tag_records"
        )
        val tags: List<Tag>)
