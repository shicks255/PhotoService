package com.steven.hicks.PhotoService

import com.steven.hicks.PhotoService.models.Photo
import com.steven.hicks.PhotoService.models.Tag
import com.steven.hicks.PhotoService.repositories.PhotoService
import com.steven.hicks.PhotoService.repositories.TagService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import javax.imageio.ImageIO

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

                val name = recordMap.get("filename")
                val description = recordMap.get("description")
                val dateTaken = recordMap.get("dateTaken")

                val tag1 = recordMap.get("tag1")
                val tag2 = recordMap.get("tag2")
                val tag3 = recordMap.get("tag3")
                val tag4 = recordMap.get("tag4")
                val tag5 = recordMap.get("tag5")

                val tags = mutableListOf<Tag>()
                if (tag1 != null)
                {
                    val tag = tagService.createIfNotExists(tag1)
                    tags.add(tag)
                }

                if (tag2 != null)
                {
                    val tag = tagService.createIfNotExists(tag2)
                    tags.add(tag)
                }

                if (tag3 != null)
                {
                    val tag = tagService.createIfNotExists(tag3)
                    tags.add(tag)
                }

                if (tag4 != null)
                {
                    val tag = tagService.createIfNotExists(tag4)
                    tags.add(tag)
                }

                if (tag5 != null)
                {
                    val tag = tagService.createIfNotExists(tag5)
                    tags.add(tag)
                }

                val thumbNailName = "${name}_small"

                val thumbNail = ClassPathResource("photos" + File.separator + thumbNailName)
                val imageFile = ClassPathResource("photos" + File.separator + name)
                if (!thumbNail.exists())
                {
                    val image = ImageIO.read(imageFile.file)

                    val scaledWidth = 450
                    val scaledHeight = 300

                    val resizedImage = BufferedImage(scaledWidth, scaledHeight, image.type)
                    val g2d = resizedImage.createGraphics()

                    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
                    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)

                    g2d.drawImage(image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), 0, 0, scaledWidth, scaledHeight, null)
                    g2d.dispose()

                    val thumbnailFile = File("photos" + File.separator + thumbNailName + ".jpg")

                    ImageIO.write(resizedImage, thumbNailName, thumbnailFile)
                }


                val newPhoto = Photo(name ?: "", name+"_small", 1, description ?: "", "", LocalDate.now(), LocalDateTime.now(), tags)
            }
        }
    }

}