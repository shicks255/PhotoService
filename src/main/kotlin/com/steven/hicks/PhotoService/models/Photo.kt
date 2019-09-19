package com.steven.hicks.PhotoService.models

import org.hibernate.annotations.Cascade
import java.time.LocalDateTime
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

        @Column(columnDefinition = "TIMESTAMP")
        val addedOn: LocalDateTime,
        @Column(columnDefinition = "TIMESTAMP")
        val taken: LocalDateTime?,
        @Cascade(org.hibernate.annotations.CascadeType.MERGE)
        @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
        @JoinTable(
                name = "photo_tag_records"
        )
        val tags: List<Tag>)
