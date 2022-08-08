package com.steven.hicks.photoService

import com.steven.hicks.photoService.models.Photo
import com.steven.hicks.photoService.models.Tag
import com.steven.hicks.photoService.service.PhotoService
import com.steven.hicks.photoService.service.PhotoUtilsService
import com.steven.hicks.photoService.service.TagService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class DatabaseSetup(
    val tagService: TagService,
    val photoService: PhotoService,
    val photoUtilsService: PhotoUtilsService
) {

    @Value("\${photos.folder}")
    private lateinit var photosPath: String

    val logger: Logger = LoggerFactory.getLogger(DatabaseSetup::class.java)

    @Bean
    fun setupDatabase(): CommandLineRunner {
        return CommandLineRunner { _ ->

            logger.info("Beginning database seeding and setup")
            logger.info("Folder ------ path={}", photosPath)
            logger.info("Folder abs path={}", Path.of(photosPath).toFile().absoluteFile)
            val startTime = System.currentTimeMillis()

//            val tagJob = GlobalScope.async {
                createTagsFromFile()
//            }

            val records = getPhotoRecordsFromCsv()
//            val photoJob: List<Deferred<Any>> = records.map {
//                GlobalScope.async { createPhoto(it) }
//            }

            records.forEach { createPhoto(it) }

//            runBlocking {
//                tagJob.await()
//                photoJob.awaitAll()
//            }

            val timeElapsed = System.currentTimeMillis() - startTime
            logger.info("Database and Photo setup completed in {} ms", timeElapsed)
        }
    }

    fun createTagsFromFile() {
        val tagList = ClassPathResource("tagList.txt")
        val lines = BufferedReader(InputStreamReader(tagList.inputStream)).readLines()
        lines.forEach { line -> tagService.createIfNotExists(line) }

        logger.info("finished creating tags")
    }

    fun getPhotoRecordsFromCsv(): List<CSVRecord> {
        val photosFolder = Path.of(photosPath)
        if (!photosFolder.toFile().exists())
            Files.createDirectory(photosFolder)

        val resources = ClassPathResource("photoManifest.csv")
        val t = BufferedReader(InputStreamReader(resources.inputStream))
        val parser = CSVParser(t, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        return parser.records
    }

    fun createPhoto(csv: CSVRecord) {
        val csvMap = csv.toMap()

        val fileName = requireNotNull(csvMap["filename"])
        val description = requireNotNull(csvMap["description"])
        val title = requireNotNull(csvMap["title"])

        val tags = mutableListOf<Tag>()

        for (i in 1 until csv.size()) {
            val tag = csvMap["tag$i"]
            if (!tag.isNullOrBlank()) {
                val tagRecord = tagService.createIfNotExists(tag)
                tags.add(tagRecord)
            }
        }

        val photo = Photo(
            fileName = fileName,
            title = title,
            description = description,
            addedOn = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime(),
            tags = tags
        )

        photoUtilsService.createAndResizePhoto(photo)
    }
}
