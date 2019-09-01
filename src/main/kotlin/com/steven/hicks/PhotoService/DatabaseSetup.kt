package com.steven.hicks.PhotoService

import com.steven.hicks.PhotoService.repositories.PhotoService
import com.steven.hicks.PhotoService.repositories.TagService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileReader
import java.nio.file.Files

@Service
class DatabaseSetup(val tagService: TagService,
                    val photoService: PhotoService) {

    @Bean
    fun setupDatabase(): CommandLineRunner {
        return CommandLineRunner { args ->

            val tagList = ClassPathResource("tagList.txt")
            if (tagList.file is File) {
                val reader = FileReader(tagList.file)
                val lines = reader.readLines()

                lines.forEach { line -> tagService.saveTag(line) }
            }


            val resources = ClassPathResource("photoManifest.csv")

            val parser = CSVParser(Files.newBufferedReader(resources.file?.toPath()), CSVFormat.DEFAULT.withFirstRecordAsHeader())
            val records = parser.records
            records.forEach{ record ->
                println(record)
                val recordMap = record.toMap()
                println(recordMap.toString())
            }
        }
    }

}