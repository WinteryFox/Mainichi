package app.mainichi

import app.mainichi.objects.Bucket
import java.io.File

val bucket = Bucket()

fun main() {
    bucket.uploadAvatar(File("C:\\Users\\Amyyyy\\Desktop\\avatar.png"))
}