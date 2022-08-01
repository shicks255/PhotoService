package com.steven.hicks.photoService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Suppress("SpreadOperator")
class PhotoServiceApplication

fun main(args: Array<String>) {
    runApplication<PhotoServiceApplication>(*args)
}
