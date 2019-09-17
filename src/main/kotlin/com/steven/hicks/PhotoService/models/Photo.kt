package com.steven.hicks.PhotoService.models

import org.hibernate.annotations.Cascade
import java.time.LocalDate
import javax.persistence.*

@Entity
data class Photo(
        @Id
        val fileName: String,
        val title: String,
        val description: String,
        val lat: String?,
        val long: String?,
        val altitude: String?,
        val exposureTime: String?,
        val fNumber: String?,
        val iso: String?,
        val focalLength: String?,
        val lensModel: String?,
        val addedOn: LocalDate,
        val taken: LocalDate?,
        @Cascade(org.hibernate.annotations.CascadeType.MERGE)
        @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
        @JoinTable(
                name = "photo_tag_records"
        )
        val tags: List<Tag>)
