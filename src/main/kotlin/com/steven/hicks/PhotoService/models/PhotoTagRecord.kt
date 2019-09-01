package com.steven.hicks.PhotoService.models

import javax.persistence.Entity
import javax.persistence.Id

//@Entity
data class PhotoTagRecord (
       @Id
       val fileName : String,

       val weight: Int,
       val tag: Tag
)