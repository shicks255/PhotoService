package com.steven.hicks.PhotoService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PhotoServiceApplication

fun main(args: Array<String>) {
	runApplication<PhotoServiceApplication>(*args)
}