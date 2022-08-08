package com.steven.hicks.photoService.service

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.steven.hicks.photoService.models.Photo
import org.imgscalr.Scalr
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Path
import java.time.ZoneId
import java.util.regex.Pattern
import javax.imageio.ImageIO

@Service
class PhotoUtilsService(
    val photoService: PhotoService
//    @Value("\${photos.folder}")
//    val photosPath: String = ""
) {

//    init {
//        photosPath =
//            Path.of(System.getProperty("user.dir") + "/photos")
//    }

    val logger: Logger = LoggerFactory.getLogger(PhotoUtilsService::class.java)

    companion object {
        val wideDimension = Dimension(1920, 1080)
        val thumbDimension = Dimension(450, 300)
        const val three = 3
        const val six = 6
    }

    fun getPhotoPath(fileName: String, subFolder: String? = null): Path {
        logger.info("Path is ${System.getProperty("user.dir")}")
        if (subFolder != null) {
            return Path.of(
                System.getProperty("user.dir") + "/photos" + File.separator +
                    subFolder + File.separator + fileName
            )
        }

//        return Path.of(photosPath + File.separator + fileName)
        return Path.of(
            System.getProperty("user.dir") + "/photos" +
                File.separator + fileName
        )
    }

    fun createAndResizePhoto(photo: Photo) {

        val imageFile = getPhotoPath(photo.fileName)
        val newPhoto = extractMetadata(photo)

        createResizedImage {
            Pair(
                Scalr.resize(
                    ImageIO.read(imageFile.toFile()),
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    wideDimension.width,
                    wideDimension.height
                ),
                getPhotoPath(photo.fileName, "compressed")
            )
        }

        createResizedImage {
            Pair(
                Scalr.resize(
                    ImageIO.read(imageFile.toFile()),
                    Scalr.Method.QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    thumbDimension.width,
                    thumbDimension.height
                ),
                getPhotoPath(photo.fileName.split(".")[0] + "_small" + ".jpg", "thumbnails")
            )
        }

        if (photoService.photoExists(photo.fileName)) {
            val oldPhoto = photoService.getPhotoByFilename(photo.fileName)
            if (hasPhotoChanged(oldPhoto, newPhoto)) {
                logger.info("Updating photo {}", newPhoto)
                photoService.savePhoto(newPhoto)
            }
        } else {
            logger.info("Saving new photo {}", newPhoto)
            photoService.savePhoto(newPhoto)
        }
    }

    fun extractMetadata(photo: Photo): Photo {
        val imageFile = getPhotoPath(photo.fileName)
        val imageMetadata = ImageMetadataReader.readMetadata(imageFile.toFile())

        val exifSubIFDDirectory = imageMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)

        val dateTaken = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
        val dateTakenUTC = dateTaken.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()

        val exposureTime = getMetadata(ExifSubIFDDirectory.TAG_EXPOSURE_TIME, exifSubIFDDirectory)
        val fstop = getMetadata(ExifSubIFDDirectory.TAG_FNUMBER, exifSubIFDDirectory)
        val iso = getMetadata(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT, exifSubIFDDirectory)
        val focalLength = getMetadata(ExifSubIFDDirectory.TAG_FOCAL_LENGTH, exifSubIFDDirectory)
        val lensModel = getMetadata(ExifSubIFDDirectory.TAG_LENS_MODEL, exifSubIFDDirectory)

        var latitude: String? = null
        var longitude: String? = null
        var altitude: String? = null

        val mathContext = MathContext(six, RoundingMode.HALF_EVEN)

        if (imageMetadata.getFirstDirectoryOfType(GpsDirectory::class.java) != null) {
            val gpsDirectory = imageMetadata.getFirstDirectoryOfType(GpsDirectory::class.java)

            longitude = getLongitude(gpsDirectory, mathContext)
            latitude = getLatitude(gpsDirectory, mathContext)
            altitude = gpsDirectory.getString(GpsDirectory.TAG_ALTITUDE)
        }

        return photo.copy(
            lat = latitude,
            long = longitude,
            altitude = altitude,
            exposureTime = exposureTime,
            fNumber = fstop,
            iso = iso,
            focalLength = focalLength,
            lensModel = lensModel,
            taken = dateTakenUTC,
        )
    }

    fun hasPhotoChanged(oldPhoto: Photo, newPhoto: Photo): Boolean {

        var photoChanged = true

        if (oldPhoto == newPhoto) {
            photoChanged = false
        }

        if (oldPhoto.hashCode() == newPhoto.copy(addedOn = oldPhoto.addedOn).hashCode()) {
            photoChanged = false
        }

        return photoChanged
    }

    fun createResizedImage(rszr: () -> Pair<BufferedImage, Path>) {
        val newImage = rszr()

        if (!newImage.second.toFile().exists()) {
            ImageIO.write(newImage.first, "jpg", newImage.second.toFile())
        }
    }

    fun getMetadata(field: Int, directory: ExifSubIFDDirectory): String =
        if (directory.containsTag(field)) {
            directory.getString(field)
        } else ""

    fun getLongitude(gpsDirectory: Directory, context: MathContext): String {
        val dmsLongetude =
            gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE) + " " +
                gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF)
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
            .plus(longSeconds.divide(BigDecimal("3600"), context)).toString() + longTokens[three]

        return longetude
    }

    fun getLatitude(gpsDirectory: Directory, context: MathContext): String {
        val dmsLatitude = gpsDirectory.getString(GpsDirectory.TAG_LATITUDE) + " " +
            gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF)
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
            .plus(latSeconds.divide(BigDecimal("3600"), context)).toString() + latTokens[three]

        return latitude
    }
}
