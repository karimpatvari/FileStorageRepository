package com.applications.bobatea;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class main {

    public static void main(String[] args) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Initialize MinIO Client
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000") // MinIO server URL
                .credentials("minioadmin", "minioadmin") // Access and secret keys
                .build();

        // Check if the bucket exists
        String bucketName = "my-bucket";
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        // Create bucket if it does not exist
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println("Bucket created: " + bucketName);
        }
    }
}
