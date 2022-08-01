package com.steven.hicks.photoService

fun String.getThumbnailName() = this.replace(".jpg", "_small.jpg")
