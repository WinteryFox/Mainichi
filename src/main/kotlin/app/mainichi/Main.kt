package app.mainichi

import app.mainichi.objects.Bucket
import java.io.File
import java.nio.file.Path

val bucket = Bucket()

fun main() {
    bucket.putAndHash("mainichi", Path.of("/avatars/"), File("/home/amy/Pictures/avatar.png"))

    bucket.setVisible("mainichi", "avatars/*", true)
}