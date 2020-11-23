package app.mainichi.objects

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.policy.Policy
import com.amazonaws.auth.policy.Principal
import com.amazonaws.auth.policy.Resource
import com.amazonaws.auth.policy.Statement
import com.amazonaws.auth.policy.actions.S3Actions
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

class Bucket {
    private val client = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("s3.nl-ams.scw.cloud", "nl-ams"))
        .withCredentials(
            AWSStaticCredentialsProvider(
                BasicAWSCredentials(
                    System.getenv("bucket-access-key"),
                    System.getenv("bucket-secret-key")
                )
            )
        )
        .build()

    fun put(bucket: String, key: String, file: File) {
        client.putObject(bucket, key, file)
    }

    fun putAndHash(bucket: String, path: Path, file: File) {
        val hash =
            Base64.getUrlEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(file.readBytes()))

        put(bucket, Paths.get(path.toString(), hash).toString() + "." + file.extension, file)
    }

    fun setVisible(bucket: String, key: String, isVisible: Boolean) {
        client.setBucketPolicy(
            bucket,
            Policy()
                .withStatements(
                    Statement(if (isVisible) Statement.Effect.Allow else Statement.Effect.Deny)
                        .withPrincipals(Principal.AllUsers)
                        .withActions(S3Actions.GetObject)
                        .withResources(Resource("arn::aws::s3:::$bucket/$key"))
                )
                .toJson()
        )
    }
}