package com.steven.hicks.photoService.models

import org.hibernate.annotations.Cascade
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
data class Photo(
    @Id
    val fileName: String,
    val title: String,
    val description: String,
    val lat: String? = null,
    val long: String? = null,
    val altitude: String? = null,
    val exposureTime: String? = null,
    val fNumber: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val lensModel: String? = null,

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    val addedOn: LocalDateTime,
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    val taken: LocalDateTime? = null,
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "photo_tag_records"
    )
    val tags: List<Tag>
)
