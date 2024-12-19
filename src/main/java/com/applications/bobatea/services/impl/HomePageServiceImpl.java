package com.applications.bobatea.services.impl;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.dto.*;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.HomePageService;
import com.applications.bobatea.services.MinIoService;
import com.applications.bobatea.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomePageServiceImpl implements HomePageService {

    private MinIoService minIoService;
    private UserService userService;
    @Value("${file.upload.max-size}")
        private long MAX_FILE_SIZE;

    @Autowired
    public HomePageServiceImpl(MinIoService minIoService, UserService userService) {
        this.minIoService = minIoService;
        this.userService = userService;
    }

    @Override
    public HomePageDto getHomePageData(Principal principal, String path) throws MinioClientException, IllegalArgumentException {

        if (principal == null) {
            throw new UsernameNotFoundException("You must be logged in to access this page.");
        }

        User user = userService.getAuthenticatedUser(principal);
        List<FileDto> fileDtos = minIoService.listFilesInFolder(user, path);
        UserDto userDto = new UserDto(user.getId(), user.getUsername());

        return new HomePageDto(path != null ? minIoService.breadcrumbs(path) : new ArrayList<>(),
                path, fileDtos, userDto);
    }

    @Override
    public void uploadFile(Principal principal, UploadFileDto uploadFileDto) throws MinioClientException, UsernameNotFoundException {

        if (principal == null) {
            throw new UsernameNotFoundException("You must be logged in to access this page.");
        }

        User user = userService.getAuthenticatedUser(principal);
        minIoService.uploadFile(user, uploadFileDto.getPath(), uploadFileDto.getFile());
    }

    @Override
    public void deleteFile(Principal principal, String filePath) throws MinioClientException {
        User user = userService.getAuthenticatedUser(principal);
        minIoService.deleteFile(user, filePath);
    }

    @Override
    public void createFolder(Principal principal, CreateFolderDto createFolderDto) throws MinioClientException, IllegalArgumentException, UsernameNotFoundException {

        String folderName = createFolderDto.getFolderName();
        String path = createFolderDto.getPath();

        User user = userService.getAuthenticatedUser(principal);

        if (folderName == null || folderName.isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be null or empty.");
        }

        minIoService.createNewFolder(user, folderName, path);

    }

    @Override
    public void deleteFolder(Principal principal, DeleteFolderDto deleteFolderDto) throws MinioClientException, UsernameNotFoundException, IllegalArgumentException {
        User user = userService.getAuthenticatedUser(principal);

        if (deleteFolderDto.getFolderPath() == null || deleteFolderDto.getFolderPath().isEmpty()) {
            throw new IllegalArgumentException("Folder path cannot be null or empty.");
        }

        minIoService.deleteFolder(user, deleteFolderDto.getFolderPath());
    }

    @Override
    public void uploadFolder(Principal principal, UploadFolderDto uploadFolderDto) throws MinioClientException {
        User user = userService.getAuthenticatedUser(principal);

        for (MultipartFile multipartFile : uploadFolderDto.getFileList()) {
            minIoService.uploadFile(user, uploadFolderDto.getPath(), multipartFile);
        }
    }
}
