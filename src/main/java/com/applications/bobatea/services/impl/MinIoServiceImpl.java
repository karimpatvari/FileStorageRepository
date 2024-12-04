package com.applications.bobatea.services.impl;

import com.applications.bobatea.models.User;
import com.applications.bobatea.services.MinIoService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinIoServiceImpl implements MinIoService {

    private final String defaultBucket = "user-files";

    private MinioClient minioClient;

    @Autowired
    public MinIoServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void CreateBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        // Check if the bucket exists;
        boolean bucketExists = bucketExists(defaultBucket);

        // Create bucket if it does not exist
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(defaultBucket)
                    .build());
            System.out.println("Bucket created: " + defaultBucket);
        }
    }

    @Override
    public void createFolder(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String folderName = "user-" + user.getId() + "-files/";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(defaultBucket)
                        .object(folderName) // Add trailing slash to indicate a folder
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1) // Empty content
                        .build()
        );
        System.out.println("Folder created: " + folderName);
    }

    /*
    @Override
    public void createNewFolder(User user, String folderName, String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String userFolder = "user-" + user.getId() + "-files/";
        path = (path.endsWith("/") ? path : path + "/");

        // Construct the folder's full object key
        String fullPath = userFolder + path + folderName + "/";

        // Upload an empty object with the full path as the key
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(rootBucket) // Ensure this bucket exists
                        .object(fullPath)   // Full path of the folder
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1) // Empty content
                        .build()
        );

        System.out.println("Folder created: " + fullPath);
    }
     */

    @Override
    public void uploadFile(User user, String fileName, InputStream fileStream, long fileSize, String contentType) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String userFolder = "user-" + user.getId() + "-files/";

        // Upload the file directly to the bucket root
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(defaultBucket) // Specify the bucket
                        .object(userFolder + fileName)      // File name as the object key
                        .stream(fileStream, fileSize, -1) // InputStream and file size
                        .contentType(contentType) // MIME type of the file
                        .build()
        );

        System.out.println("File uploaded successfully: " + fileName);
    }

    @Override
    public void deleteFile(User user, String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String prefix = "user-" + user.getId() + "-files/";

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(defaultBucket)
                .object(prefix + fileName)
                .build());

    }

    @Override
    public List<String> listFilesInFolder(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<String> fileNames = new ArrayList<>();

        // Ensure the folderPath ends with a slash
        String prefix = "user-" + user.getId() + "-files/";

        // List objects with the given prefix
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(prefix) // Filter by folder path
                        .recursive(true) // Include all files in subfolders
                        .build()
        );

        // Collect file names from the result
        for (Result<Item> result : results) {
            Item item = result.get();

            // Skip folders (optional, in case folder keys exist)
            if (item.isDir()) {
                continue;
            }

            // Extract just the file name (remove prefix if needed)
            String objectName = item.objectName();
            String fileName = objectName.substring(prefix.length());
            fileNames.add(fileName); // Add only the file name
        }

        return fileNames;
    }

    @Override
    public InputStream downloadFile(User user, String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String prefix = "user-" + user.getId() + "-files/";

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(defaultBucket)
                        .object(prefix + fileName) // Full path of the file in MinIO
                        .build()
        );
    }

    private boolean bucketExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }
}
