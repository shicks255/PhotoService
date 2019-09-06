package com.steven.hicks.PhotoService

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.makernotes.FujifilmMakernoteDirectory
import com.steven.hicks.PhotoService.models.Photo
import com.steven.hicks.PhotoService.models.Tag
import com.steven.hicks.PhotoService.repositories.PhotoService
import com.steven.hicks.PhotoService.repositories.TagService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.imgscalr.Scalr
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Dimension
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.ZoneId
import javax.imageio.ImageIO

@Service
class DatabaseSetup(val tagService: TagService,
                    val photoService: PhotoService) {

    @Value("\${photos.folder}")
    private lateinit var photosPath: String

    @Bean
    fun setupDatabase(): CommandLineRunner {
        return CommandLineRunner { args ->

            val tagList = ClassPathResource("tagList.txt")
            if (tagList.file is File) {
                val reader = FileReader(tagList.file)
                val lines = reader.readLines()

                lines.forEach { line -> tagService.saveTag(line) }
            }

            val photosFolder = Path.of(photosPath)
            if (!photosFolder.toFile().exists())
                Files.createDirectory(photosFolder)

            val resources = ClassPathResource("photoManifest.csv")

            val parser = CSVParser(Files.newBufferedReader(resources.file?.toPath()), CSVFormat.DEFAULT.withFirstRecordAsHeader())
            val records = parser.records
            records.forEach{ record ->
                println(record)
                val recordMap = record.toMap()
                println(recordMap.toString())

                val name = requireNotNull(recordMap.get("filename"))
                val description = recordMap.get("description")

                val tag1 = recordMap.get("tag1")
                val tag2 = recordMap.get("tag2")
                val tag3 = recordMap.get("tag3")
                val tag4 = recordMap.get("tag4")
                val tag5 = recordMap.get("tag5")
                val tag6 = recordMap.get("tag6")
                val tag7 = recordMap.get("tag7")
                val tag8 = recordMap.get("tag8")
                val tag9 = recordMap.get("tag9")
                val tag10 = recordMap.get("tag10")

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
                if (tag6 != null)
                {
                    val tag = tagService.createIfNotExists(tag6)
                    tags.add(tag)
                }
                if (tag7 != null)
                {
                    val tag = tagService.createIfNotExists(tag7)
                    tags.add(tag)
                }
                if (tag8 != null)
                {
                    val tag = tagService.createIfNotExists(tag8)
                    tags.add(tag)
                }
                if (tag9 != null)
                {
                    val tag = tagService.createIfNotExists(tag9)
                    tags.add(tag)
                }
                if (tag10 != null)
                {
                    val tag = tagService.createIfNotExists(tag10)
                    tags.add(tag)
                }

                val imageFile = Paths.get(photosPath + File.separator + name)
                val t = ImageMetadataReader.readMetadata(imageFile.toFile())

                val directory = t.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                val dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                val dateTaken2 = dateTaken.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                val exposureTime = if (directory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME))
                    directory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) else ""
                val fStop = if (directory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER))
                        directory.getString(ExifSubIFDDirectory.TAG_FNUMBER) else ""
                val iso = if (directory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT))
                        directory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT) else ""
                val dateTimeOrignal = directory.dateOriginal
                val focalLength = if (directory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH))
                    directory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH) else ""
                val lensModel = if (directory.containsTag(ExifSubIFDDirectory.TAG_LENS_MODEL))
                    directory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL) else ""

                val fujiDirectory = t.getFirstDirectoryOfType(FujifilmMakernoteDirectory::class.java)
                //val aperture = fujiDirectory.getString(FujifilmMakernoteDirectory.)

                val thumbNailName = "${name?.substring(0, name?.indexOf("."))}_small.jpg"
                val thumbnailPath = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName)
                if (!thumbnailPath.toFile().exists())
                {
                    val resizeMe = ImageIO.read(imageFile.toFile())
                    val dimension = Dimension(450, 300)
                    val newImage = Scalr.resize(resizeMe, Scalr.Method.QUALITY, dimension.width, dimension.height)
                    val thumbnailFile = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName).toFile()
                    ImageIO.write(newImage, "jpg", thumbnailFile)

                    val newPhoto = Photo(name, imageFile.toFile().length(),
                            description, "", exposureTime, fStop, iso, focalLength, lensModel,
                            LocalDate.now(), dateTaken2, tags)
                    photoService.savePhoto(newPhoto)
                }
                else
                {
                    val oldPhoto = photoService.getPhotoByFilename(name)
                    val newOldPhoto = oldPhoto.copy(description = description, tags = tags, taken = dateTaken2)
                    photoService.savePhoto(newOldPhoto)
                }
            }
        }
    }

}