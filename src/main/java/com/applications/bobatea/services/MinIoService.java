package com.applications.bobatea.services;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.dto.FileDto;
import com.applications.bobatea.dto.PathDto;
import com.applications.bobatea.dto.RenameFolderDto;
import com.applications.bobatea.dto.ZipDto;
import com.applications.bobatea.models.User;
import io.minio.BucketExistsArgs;
import io.minio.errors.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

public interface MinIoService {

    void CreateBucket() throws MinioClientException;

    void createFolder(User user) throws MinioClientException;

    List<FileDto> listFilesInFolder(User user, String path) throws MinioClientException;

    void deleteFile(User user, String filePath) throws MinioClientException;

    void uploadFile(User user, String path, MultipartFile file) throws MinioClientException;

    InputStreamResource downloadFile(User user, String filePath) throws MinioClientException;

    void createNewFolder(User user, String folderName, String path) throws MinioClientException;

    void deleteFolder(User user, String folderPath) throws MinioClientException;

    ArrayList<PathDto> breadcrumbs(String path);

    List<FileDto> listFilesRecursively(User user, String path) throws MinioClientException;

    ZipDto downloadFolder(User user, String path) throws MinioClientException;

    void renameFile(User user, String oldFilePath, String newFilePath) throws MinioClientException;

    void renameFolder(User user, RenameFolderDto renameFolderDto) throws MinioClientException;

    List<FileDto> findFiles(User user, String keyword) throws MinioClientException;
}
