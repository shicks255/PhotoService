package com.steven.hicks.PhotoService


fun String.getThumbnailName() = this.replace(".jpg", "_small.jpg")