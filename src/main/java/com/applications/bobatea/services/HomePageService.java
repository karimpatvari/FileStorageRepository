package com.applications.bobatea.services;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.dto.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

public interface HomePageService {

    HomePageDto getHomePageData(Principal principal, String path) throws MinioClientException;

    void uploadFile(Principal principal, UploadFileDto uploadFileDto) throws MinioClientException, UsernameNotFoundException;

    void deleteFile(Principal principal, String filePath) throws MinioClientException;

    void createFolder(Principal principal, CreateFolderDto createFolderDto) throws MinioClientException, IllegalArgumentException, UsernameNotFoundException;

    void deleteFolder(Principal principal, DeleteFolderDto deleteFolderDto) throws MinioClientException, UsernameNotFoundException, IllegalArgumentException;

    void uploadFolder(Principal principal, UploadFolderDto uploadFolderDto) throws MinioClientException;
}
