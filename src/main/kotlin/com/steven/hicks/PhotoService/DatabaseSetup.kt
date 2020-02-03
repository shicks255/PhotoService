package com.steven.hicks.PhotoService

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.steven.hicks.PhotoService.models.Photo
import com.steven.hicks.PhotoService.models.Tag
import com.steven.hicks.PhotoService.repositories.PhotoService
import com.steven.hicks.PhotoService.repositories.TagService
import kotlinx.coroutines.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.imgscalr.Scalr
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Dimension
import java.io.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.LocalTime
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

            println(photosPath)
            photoService.deleteAll()
            tagService.deleteAll()

            val job = GlobalScope.async {
                createTags()
            }

            val photosFolder = Path.of(photosPath)
            if (!photosFolder.toFile().exists())
                Files.createDirectory(photosFolder)

            val resources = ClassPathResource("photoManifest.csv")
            val t = BufferedReader(InputStreamReader(resources.inputStream))
            val parser = CSVParser(t, CSVFormat.DEFAULT.withFirstRecordAsHeader())
            val records = parser.records
            val completedPhotos: List<Deferred<Any>> = records.map {
                GlobalScope.async { createPhoto(it) }
            }

            runBlocking {
                job.await()
                completedPhotos.awaitAll()
            }
            println("Database and Photo setup is complete")
        }
    }

    fun createTags() {
        val tagList = ClassPathResource("tagList.txt")
        val lines = BufferedReader(InputStreamReader(tagList.inputStream)).readLines()
        lines.forEach { line -> tagService.createIfNotExists(line) }

        println("finished at " + LocalTime.now())
    }

    fun createPhoto(csv: CSVRecord) {
        println(csv)
        val csvMap = csv.toMap()
        println(csvMap.toString())

        val fileName = requireNotNull(csvMap.get("filename"))
        val description = requireNotNull(csvMap.get("description"))
        val title = requireNotNull(csvMap.get("title"))

        val tags = getTags(csvMap)

        val imageFile = Paths.get(photosPath + File.separator + fileName)
        val imageMetaData = ImageMetadataReader.readMetadata(imageFile.toFile())

        val exifSubIFDDirectory = imageMetaData.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
        val dateTaken = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
        val dateTakenLocalDateTime = dateTaken.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()

        val exposureTime = if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME))
            exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) else ""
        val fStop = if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER))
            exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER) else ""
        val iso = if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT))
            exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT) else ""
        val focalLength = if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH))
            exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH) else ""
        val lensModel = if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_LENS_MODEL))
            exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL) else ""

        var longetude = ""
        var latitude = ""
        var altitude = ""

        val context = MathContext(6, RoundingMode.HALF_EVEN)

        if (imageMetaData.getFirstDirectoryOfType(GpsDirectory::class.java) != null) {
            val gpsDirectory = imageMetaData.getFirstDirectoryOfType(GpsDirectory::class.java)

            longetude = getLongitude(gpsDirectory, context)
            latitude = getLatitude(gpsDirectory, context)
            altitude = gpsDirectory.getString(GpsDirectory.TAG_ALTITUDE)
        }

        createThumbnail(fileName, imageFile)
        createCompressed(fileName, imageFile)
        createDownScaled(fileName, imageFile)

        if (photoService.photoExists(fileName)) {
            val oldPhoto = photoService.getPhotoByFilename(fileName)
            //doing this in case anything changed
            val newOldPhoto = oldPhoto.copy(description = description, tags = tags, taken = dateTakenLocalDateTime, title = title)
            photoService.savePhoto(newOldPhoto)
        } else {
            val newPhoto = Photo(fileName, title,
                    description, latitude, longetude, altitude, exposureTime, fStop, iso, focalLength, lensModel,
                    LocalDateTime.now(), dateTakenLocalDateTime, tags)
            photoService.savePhoto(newPhoto)
        }
    }

    private fun createCompressed(fileName: String, imageFile: Path) {
        val compressed = Path.of(photosPath + File.separator + "compressed" + File.separator + fileName + ".jpg")
        if (compressed.toFile().exists())
            Files.delete(compressed)
        val compressMe = ImageIO.read(imageFile.toFile())
        val dimension = Dimension(1920, 1080)
        val newImage = Scalr.resize(compressMe, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, dimension.width, dimension.height)
        val compressFile = Path.of(photosPath + File.separator + "compressed" + File.separator + fileName).toFile()
        ImageIO.write(newImage, "jpg", compressFile)
    }

    private fun createDownScaled(fileName: String, imageFile: Path) {
        val compressed = Path.of(photosPath + File.separator + "downScaled" + File.separator + fileName + ".jpg")
        if (compressed.toFile().exists())
            Files.delete(compressed)
        val compressMe = ImageIO.read(imageFile.toFile())
        val dimension = Dimension(100, 45)
        val newImage = Scalr.resize(compressMe, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, dimension.width, dimension.height)
        val compressFile = Path.of(photosPath + File.separator + "downScaled" + File.separator + fileName).toFile()
        ImageIO.write(newImage, "jpg", compressFile)
    }

    private fun createThumbnail(fileName: String, imageFile: Path) {
        val thumbNailName = "${fileName.substring(0, fileName.indexOf("."))}_small.jpg"
        val thumbnailPath = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName)
        if (!thumbnailPath.toFile().exists()) {
            val resizeMe = ImageIO.read(imageFile.toFile())
            val dimension = Dimension(450, 300)
            val newImage = Scalr.resize(resizeMe, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, dimension.width, dimension.height)
            val thumbnailFile = Path.of(photosPath + File.separator + "thumbnails" + File.separator + thumbNailName).toFile()
            ImageIO.write(newImage, "jpg", thumbnailFile)
        }
    }

    fun getTags(recordMap: Map<String, String>): List<Tag> {
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
        if (!tag1.isNullOrBlank()) {
            val thisTag = checkNotNull(tag1)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag2.isNullOrBlank()) {
            val thisTag = checkNotNull(tag2)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag3.isNullOrBlank()) {
            val thisTag = checkNotNull(tag3)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag4.isNullOrBlank()) {
            val thisTag = checkNotNull(tag4)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        if (!tag5.isNullOrBlank()) {
            val thisTag = checkNotNull(tag5)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag6.isNullOrBlank()) {
            val thisTag = checkNotNull(tag6)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag7.isNullOrBlank()) {
            val thisTag = checkNotNull(tag7)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag8.isNullOrBlank()) {
            val thisTag = checkNotNull(tag8)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag9.isNullOrBlank()) {
            val thisTag = checkNotNull(tag9)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }
        if (!tag10.isNullOrBlank()) {
            val thisTag = checkNotNull(tag10)
            val tag = tagService.createIfNotExists(thisTag)
            tags.add(tag)
        }

        return tags
    }

    fun getLongitude(gpsDirectory: Directory, context: MathContext): String {
        val dmsLongetude = gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE) + " " + gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF)
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

    fun getLatitude(gpsDirectory: Directory, context: MathContext): String {
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