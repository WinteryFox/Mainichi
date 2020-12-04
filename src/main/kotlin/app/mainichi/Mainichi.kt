package app.mainichi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

//val bucket = Bucket("s3.nl-ams.scw.cloud", "nl-ams", System.getenv("bucket-access-key"), System.getenv("bucket-secret-key"))

@SpringBootApplication
@EnableR2dbcRepositories
class Mainichi

fun main() {
    //bucket.putAndHash("mainichi", Path.of("/avatars/"), File("/home/amy/Pictures/avatar.png"))
    //bucket.setVisible("mainichi", "avatars/*", true)

    runApplication<Mainichi>()
}