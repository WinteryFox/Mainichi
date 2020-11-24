package app.mainichi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

//val bucket = Bucket("s3.nl-ams.scw.cloud", "nl-ams", System.getenv("bucket-access-key"), System.getenv("bucket-secret-key"))

@SpringBootApplication
class Mainichi

fun main() {
    //bucket.putAndHash("mainichi", Path.of("/avatars/"), File("/home/amy/Pictures/avatar.png"))
    //bucket.setVisible("mainichi", "avatars/*", true)

    runApplication<Mainichi>()
}