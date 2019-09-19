package com.steven.hicks.PhotoService

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
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
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Service
class DatabaseSetup(val tagService: TagService,
                    val photoService: PhotoService) {

    @Value("\${photos.folder}")
    private lateinit var photosPath: String

    @Bean
    fun setupDatabase(): CommandLineRunner {
        return CommandLineRunner { _ ->

            val tagList = ClassPathResource("tagList.txt")
            val reader = FileReader(tagList.file)
            val lines = reader.readLines()
            lines.forEach { line -> tagService.saveTag(line) }

            val photosFolder = Path.of(photosPath)
            if (!photosFolder.toFile().exists())
                Files.createDirectory(photosFolder)

            val resources = ClassPathResource("photoManifest.csv")

            val parser = CSVParser(Files.newBufferedReader(resources.file.toPath()), CSVFormat.DEFAULT.withFirstRecordAsHeader())
            val records = parser.records
            records.forEach{ record ->
                println(record)
                val recordMap = record.toMap()
                println(recordMap.toString())

                val name = requireNotNull(recordMap.get("filename"))
                val description = requireNotNull(recordMap.get("description"))
                val title = requireNotNull(recordMap.get("title"))

                val tags = getTags(recordMap)

                val imageFile = Paths.get(photosPath + File.separator + name)
                val t = ImageMetadataReader.readMetadata(imageFile.toFile())

                val directory = t.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                val dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                val dateTakenLocalDateTime = dateTaken.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()

                val exposureTime = if (directory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME))
                    directory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) else ""
                val fStop = if (directory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER))
                        directory.getString(ExifSubIFDDirectory.TAG_FNUMBER) else ""
                val iso = if (directory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT))
                        directory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT) else ""
                val focalLength = if (directory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH))
                    directory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH) else ""
                val lensModel = if (directory.containsTag(ExifSubIFDDirectory.TAG_LENS_MODEL))
                    directory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL) else ""

                var longetude = ""
                var latitude = ""
                var altitude = ""

                val context = MathContext(6, RoundingMode.HALF_EVEN)

                if (t.getFirstDirectoryOfType(GpsDirectory::class.java) != null) {
                    val gpsDirectory = t.getFirstDirectoryOfType(GpsDirectory::class.java)

                    longetude = getLongitude(gpsDirectory, context)
                    latitude = getLatitude(gpsDirectory, context)
                    altitude = gpsDirectory.getString(GpsDirectory.TAG_ALTITUDE)
                }

                val thumbNailName = "${name.substring(0, name.indexOf("."))}_small.jpg"
                val thumbnailPath = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName)
                if (!thumbnailPath.toFile().exists())
                {
                    val resizeMe = ImageIO.read(imageFile.toFile())
                    val dimension = Dimension(450, 300)
                    val newImage = Scalr.resize(resizeMe, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, dimension.width, dimension.height)
                    val thumbnailFile = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName).toFile()
                    ImageIO.write(newImage, "jpg", thumbnailFile)

                    val newPhoto = Photo(name, title,
                            description, latitude, longetude, altitude, exposureTime, fStop, iso, focalLength, lensModel,
                            LocalDateTime.now(), dateTakenLocalDateTime, tags)
                    photoService.savePhoto(newPhoto)
                }
                else
                {
                    val oldPhoto = photoService.getPhotoByFilename(name)
                    val newOldPhoto = oldPhoto.copy(description = description, tags = tags, taken = dateTakenLocalDateTime, title = title)
                    photoService.savePhoto(newOldPhoto)
                }
            }
        }
    }

    private fun getTags(recordMap: Map<String, String>): List<Tag> {
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
        if (!tag1.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag1)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag2.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag2)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag3.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag3)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag4.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag4)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag5.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag5)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag6.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag6)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag7.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag7)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag8.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag8)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag9.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag9)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag10.isNullOrBlank())
        {
            val thisTag = checkNotNull(tag10)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        return tags
    }

    private fun getLongitude(gpsDirectory: Directory, context: MathContext): String {
        val dmsLongetude = gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE) + " " +gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF)
        val longTokens = dmsLongetude.split(Pattern.compile("/\\d+\\s"))

        val longStuff = dmsLongetude.split(" ")
        var longDegrees = BigDecimal(longStuff[0].split("/")[0])
        longDegrees = longDegrees.divide(BigDecimal(longStuff[0].split("/")[1]), context)

        var longMinutes = BigDecimal(longStuff[1].split("/")[0])
        longMinutes = longMinutes.divide(BigDecimal(longStuff[1].split("/")[1]), context)

        var longSeconds = BigDecimal(longStuff[2].split("/")[0])
        longSeconds = longSeconds.divide(BigDecimal(longStuff[2].split("/")[1]), context)

        val longetude = longDegrees
                .plus(longMinutes.divide(BigDecimal("60"), context))
                .plus(longSeconds.divide(BigDecimal("3600"), context)).toString() + longTokens[3]

        return longetude
    }

    private fun getLatitude(gpsDirectory: Directory, context: MathContext): String {
        val dmsLatitude = gpsDirectory.getString(GpsDirectory.TAG_LATITUDE) + " " + gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF)
        val latTokens = dmsLatitude.split(Pattern.compile("/\\d+\\s"))

        val latStuff = dmsLatitude.split(" ")
        var latDegrees = BigDecimal(latStuff[0].split("/")[0])
        latDegrees = latDegrees.divide(BigDecimal(latStuff[0].split("/")[1]), context)

        var latMinutes = BigDecimal(latStuff[1].split("/")[0])
        latMinutes = latMinutes.divide(BigDecimal(latStuff[1].split("/")[1]), context)

        var latSeconds = BigDecimal(latStuff[2].split("/")[0])
        latSeconds = latSeconds.divide(BigDecimal(latStuff[2].split("/")[1]), context)

        val latitude = latDegrees
                .plus(latMinutes.divide(BigDecimal("60"), context))
                .plus(latSeconds.divide(BigDecimal("3600"), context)).toString() + latTokens[3]

        return latitude
    }
}