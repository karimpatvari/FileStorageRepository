package com.applications.bobatea.services.impl;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.dto.FileDto;
import com.applications.bobatea.dto.PathDto;
import com.applications.bobatea.dto.RenameFolderDto;
import com.applications.bobatea.dto.ZipDto;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.MinIoService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinIoServiceImpl implements MinIoService {

    private static final Logger logger = LoggerFactory.getLogger(MinIoServiceImpl.class);
    @Value("${default.bucket.name}")
    private String defaultBucket;
    private MinioClient minioClient;

    @Autowired
    public MinIoServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void CreateBucket() throws MinioClientException {

        try {
            // Create bucket if it does not exist
            if (!bucketExists()) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(defaultBucket)
                        .build());
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {

            logger.error("Error creating bucket: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while creating bucket. Please try again later.");
        }
    }

    @Override
    public void createFolder(User user) throws MinioClientException {

        String folderName = user.getFolder();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(folderName) // Add trailing slash to indicate a folder
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1) // Empty content
                            .build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {

            logger.error("Error creating root user folder: {}", e.getMessage(), e);
            throw new MinioClientException("An error occurred while creating user folder. Please try again later.");
        }

    }

    @Override
    public void createNewFolder(User user, String folderName, String path) throws MinioClientException {

        String prefix = user.getFolder();

        if (folderName == null || folderName.isEmpty()) {
            throw new MinioClientException("Invalid folder data: folder name is missing.");
        }

        if (path != null && !path.isEmpty()) {
            path = (path.endsWith("/") ? path : path + "/");
        }

        // Construct the folder's full object key
        String fullPath = prefix + path + folderName + "/";

        try {
            // Upload an empty object with the full path as the key
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket) // Ensure this bucket exists
                            .object(fullPath)   // Full path of the folder
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1) // Empty content
                            .build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {

            logger.error("Error creating folder: {}", e.getMessage(), e);
            throw new MinioClientException("An error occurred while creating new folder with name :" + folderName + ". Please try again later.");
        }
    }

    @Override
    public void deleteFolder(User user, String folderPath) throws MinioClientException {

        String prefix = user.getFolder();

        if (folderPath == null || folderPath.isEmpty()) {
            throw new MinioClientException("Invalid folder data: folder path is missing.");
        }

        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        folderPath = prefix + folderPath;

        // List all objects in the folder
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(folderPath)
                        .recursive(true) // Include all objects in subfolders
                        .build()
        );

        try {
            // Iterate through and delete each object
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // Delete the object
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(defaultBucket)
                                .object(objectName)
                                .build()
                );
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error deleting folder: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public ArrayList<PathDto> breadcrumbs(String fullPath) {

        ArrayList<PathDto> breadcrumbPaths = new ArrayList<>();

        // Remove trailing slash if it exists to handle edge cases
        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }

        // Split the path into parts
        String[] parts = fullPath.split("/");

        // Build progressively shorter paths
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            pathBuilder.append(parts[i]).append("/");

            String string = pathBuilder.toString();
            String lastPart = getLastPart(string);

            PathDto pathDto = new PathDto(pathBuilder.toString(), lastPart);
            breadcrumbPaths.add(pathDto);
        }

        return breadcrumbPaths;
    }

    @Override
    public List<FileDto> listFilesRecursively(User user, String path) throws MinioClientException {
        List<FileDto> fileDtos = new ArrayList<>();

        // Ensure the folderPath ends with a slash
        String prefix = user.getFolder();
        String filePath = prefix;

        if (path != null && !path.isEmpty()) {
            // Ensure the path ends with a slash
            filePath += path.endsWith("/") ? path : path + "/";
        }else {
            throw new IllegalArgumentException("Path is empty. Please try again later.");
        }

        // List objects with the given prefix
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(filePath) // Filter by folder path
                        .recursive(true) // Include all files in subfolders
                        .build()
        );

        try {
            // Collect file names from the result
            for (Result<Item> result : results) {
                Item item = result.get();

                if (!item.objectName().endsWith("/")) {
                    // Handle regular file (not a folder)
                    FileDto fileDto = new FileDto(
                            getFileName(item, prefix),  // Get the file name
                            getFilePath(item, prefix),   // Get the full file path
                            false);
                    fileDtos.add(fileDto);  // Add file DTO to list
                }
            }

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error listing files recursively: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }

        return fileDtos;
    }

    @Override
    public ZipDto downloadFolder(User user, String path) throws MinioClientException {

        List<FileDto> fileDtos = listFilesRecursively(user, path);
        List<InputStreamResource> resources = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path is empty. Please try again later.");
        }

        try {
            for (FileDto fileDto : fileDtos) {
                InputStreamResource inputStreamResource = downloadFile(user, fileDto.getFilePath());
                resources.add(inputStreamResource);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                for (int i = 0; i < resources.size(); i++) {
                    InputStreamResource resource = resources.get(i);

                    System.out.println(fileDtos.get(i).getFilePath());
                    // Create a zip entry with a unique name
                    ZipEntry zipEntry = new ZipEntry(fileDtos.get(i).getFilePath()); // Change extension as needed
                    zipOutputStream.putNextEntry(zipEntry);

                    // Copy data from the resource to the zip entry
                    resource.getInputStream().transferTo(zipOutputStream);

                    // Close the current entry
                    zipOutputStream.closeEntry();
                }
            }
            List<String> list = Arrays.stream(path.split("/")).toList();
            int size = list.size();
            String s = list.get(size - 1);

            return new ZipDto(byteArrayOutputStream, s);
        } catch (IOException e) {
            logger.error("Error downloading folder: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public void renameFile(User user, String oldFilePath, String newFilePath) throws MinioClientException {

        String folder = user.getFolder();

        if (oldFilePath == null || oldFilePath.isEmpty()) {
            throw new IllegalArgumentException("Old file path is empty. Please try again later.");
        }

        if (newFilePath == null || newFilePath.isEmpty()) {
            throw new IllegalArgumentException("new file path is empty. Please try again later.");
        }

        try {

            // Step 1: Copy the object to the new name
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(folder + newFilePath + getFileExtension(oldFilePath))
                            .source(CopySource.builder()
                                    .bucket(defaultBucket)
                                    .object(folder + oldFilePath)
                                    .build())
                            .build()
            );

            deleteFile(user, oldFilePath);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error renaming files: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public void renameFolder(User user, RenameFolderDto renameFolderDto) throws MinioClientException {
        String folderPath = renameFolderDto.getFolderPath();
        String newFolderName = renameFolderDto.getNewFolderName();
        String path = renameFolderDto.getPath();

        validateRenameInputs(folderPath, newFolderName, path);

        if (!newFolderName.endsWith("/")) {
            newFolderName += "/";
        }

        try {
            List<FileDto> fileDtos = listFilesRecursively(user, folderPath);

            createNewFolder(user, newFolderName, path);

            String folder = user.getFolder();
            for (FileDto fileDto : fileDtos) {

                String oldFilePath = fileDto.getFilePath();
                String newFilePath = oldFilePath.replace(folderPath, newFolderName);

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(defaultBucket)
                                .object(folder + path + newFilePath)
                                .source(CopySource.builder()
                                        .bucket(defaultBucket)
                                        .object(folder + oldFilePath)
                                        .build())
                                .build()
                );
            }

            deleteFolder(user, folderPath);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {

            logger.error("Error renaming folder: {}", e.getMessage(), e);
            throw new MinioClientException("An error occurred while renaming folder. Please try again later.");
        }
    }

    @Override
    public List<FileDto> findFiles(User user, String keyword) throws MinioClientException {
        ArrayList<FileDto> matchingFiles = new ArrayList<>();

        // List all objects in the bucket with the given prefix
        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(defaultBucket).prefix(user.getFolder()).recursive(true).build()
        );

        try {
            for (Result<Item> itemResult : items) {
                Item item = itemResult.get();
                String fileName = item.objectName();

                // Check if the file name contains the keyword
                if (fileName.contains(keyword)) {
                    FileDto fileDto = new FileDto(
                            getFileName(item, user.getFolder()),
                            getFolder(item, user.getFolder()),
                            false
                    );
                    matchingFiles.add(fileDto);
                }
            }
        }  catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error finding files: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
        return matchingFiles;
    }

    @Override
    public void uploadFile(User user, String path, MultipartFile file) throws MinioClientException {

        String prefix = user.getFolder();
        String filePath = prefix;

        if (path != null && !path.isEmpty()) {
            if (path.endsWith("/")) {
                filePath += path;
            } else {
                filePath += path + "/";
            }
        }

        try {
            String fileName = file.getOriginalFilename();
            InputStream fileStream = file.getInputStream();
            long fileSize = file.getSize();
            String contentType = file.getContentType();

            // Upload the file directly to the bucket root
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket) // Specify the bucket
                            .object(filePath + fileName)      // File name as the object key
                            .stream(fileStream, fileSize, -1) // InputStream and file size
                            .contentType(contentType) // MIME type of the file
                            .build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public void deleteFile(User user, String filePath) throws MinioClientException {

        String prefix = user.getFolder();

        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is empty. Please try again later.");
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(prefix + filePath)
                    .build());
        }  catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error deleting file: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public InputStreamResource downloadFile(User user, String filePath) throws MinioClientException {

        String prefix = user.getFolder();

        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is empty. Please try again later.");
        }

        try {
            return new InputStreamResource(minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(prefix + filePath) // Full path of the file in MinIO
                            .build()
            ));
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error downloading file: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    @Override
    public List<FileDto> listFilesInFolder(User user, String path) throws MinioClientException {
        List<FileDto> fileDtos = new ArrayList<>();

        // Ensure the folderPath ends with a slash
        String prefix = user.getFolder();
        String filePath = prefix;

        if (path != null && !path.isEmpty()) {
            // Ensure the path ends with a slash
            filePath += path.endsWith("/") ? path : path + "/";
        }

        // List objects with the given prefix
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(filePath) // Filter by folder path
                        .recursive(false) // Include all files in subfolders
                        .build()
        );

        try {
            // Collect file names from the result
            for (Result<Item> result : results) {
                Item item = result.get();

                if (item.objectName().endsWith("/")) {

                    if (item.objectName().equals(filePath)) {
                        continue;
                    }

                    // Handle folder (you can create a special folder DTO or just skip it)
                    FileDto folderDto = new FileDto(
                            getFilePath(item, prefix),  // You could use the folder name if you want
                            getFilePath(item, prefix),   // Full path for the folder
                            true
                    );
                    fileDtos.add(folderDto);  // Add folder DTO to list if needed
                } else {

                    // Handle regular file (not a folder)
                    FileDto fileDto = new FileDto(
                            getFileName(item, prefix),  // Get the file name
                            getFilePath(item, prefix),   // Get the full file path
                            false
                    );
                    fileDtos.add(fileDto);  // Add file DTO to list
                }
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error listing files in folder: {}", e.getMessage(), e);

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }

        return fileDtos;
    }

    private boolean bucketExists() throws MinioClientException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(defaultBucket).build());
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();

            throw new MinioClientException("An error occurred while accessing files. Please try again later.");
        }
    }

    private String getFolder(Item item ,String prefix){
        String fileName = getFileName(item, prefix);

        String filePath = getFilePath(item, prefix);
        return filePath.replace(fileName, "");
    }

    private String getFileName(Item item, String prefix) {
        // Get the full object name (which is like a path)
        String objectName = item.objectName();

        // Remove the prefix to get the actual file or folder part
        String fileName = objectName.substring(prefix.length());

        // If it's a file (not a folder), remove the folder structure, leaving only the file name
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        return fileName;
    }

    private String getFilePath(Item item, String prefix) {
        // Get the full object name (which is like a path)
        String objectName = item.objectName();

        // Remove the prefix to get the relative path (this will keep the folder structure)
        return objectName.substring(prefix.length());
    }

    private String getLastPart(String path) {
        if (path == null || path.isEmpty()) {
            return ""; // Return empty for null or empty strings
        }

        // Remove the trailing slash if it exists
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Find the position of the last slash
        int lastSlashIndex = path.lastIndexOf("/");

        // Extract everything after the last slash
        return lastSlashIndex != -1 ? path.substring(lastSlashIndex + 1) : path;
    }

    private static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex); // Includes the dot, e.g., ".jpg"
        }
        return ""; // No extension found
    }

    private void validateRenameInputs(String folderPath, String newFolderName, String path) throws MinioClientException {
        if (folderPath == null || newFolderName == null || path == null) {
            throw new MinioClientException("Invalid folder data: Folder path, new folder name, or path is missing.");
        }
    }

}
