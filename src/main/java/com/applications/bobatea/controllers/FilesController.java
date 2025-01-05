package com.applications.bobatea.controllers;

import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.dto.*;
import com.applications.bobatea.models.User;
import com.applications.bobatea.services.HomePageService;
import com.applications.bobatea.services.MinIoService;
import com.applications.bobatea.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.List;

@Controller
public class FilesController {

    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);
    private MinIoService minIoService;
    private UserService userService;
    private HomePageService homePageService;

    @Autowired
    public FilesController(MinIoService minIoService, UserService userService, HomePageService homePageService) {
        this.minIoService = minIoService;
        this.userService = userService;
        this.homePageService = homePageService;
    }

    @GetMapping("/")
    public String showHomePage(@RequestParam(value = "path", required = false) String path,
                               Principal principal, Model model) {
        try {
            validatePrincipal(principal);

            HomePageDto homePageDto = homePageService.getHomePageData(principal, path);
            model.addAttribute("homePageDto", homePageDto);
            return "index";

        } catch (IllegalArgumentException e) {
            return "preview-page";

        } catch (Exception e) {
            logger.error("Error while showing home page: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute UploadFileDto uploadFileDto,
                             Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);

            redirectAttributes.addAttribute("path", uploadFileDto.getPath());

            if ( uploadFileDto.getFile() == null || uploadFileDto.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Uploaded file is empty. Please select a valid file.");
                return "redirect:/";
            }

            homePageService.uploadFile(principal, uploadFileDto);
            redirectAttributes.addFlashAttribute("successMessage", "File uploaded successfully.");
            return "redirect:/";

        }  catch (Exception e) {
            logger.error("Error while uploading file: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/delete")
    public String deleteFile(@ModelAttribute DeleteFileDto deleteFileDto,
                             Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (deleteFileDto.getFilePath() == null || deleteFileDto.getFilePath().isEmpty()) {
                throw new IllegalArgumentException("File path is empty");
            }

            validatePrincipal(principal);
            homePageService.deleteFile(principal, deleteFileDto.getFilePath());

            redirectAttributes.addFlashAttribute("successMessage", "File deleted successfully.");
            redirectAttributes.addAttribute("path", deleteFileDto.getPath());
            return "redirect:/";

        }  catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath, Principal principal) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            validatePrincipal(principal);
            User user = userService.getAuthenticatedUser(principal);

            // Загрузка файла из MinIO
            InputStreamResource resource = minIoService.downloadFile(user, filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/new-folder")
    public String createFolder(@ModelAttribute CreateFolderDto createFolderDto,
                               Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);

            redirectAttributes.addAttribute("path", createFolderDto.getPath());
            try {
                homePageService.createFolder(principal, createFolderDto);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/";
            }

            return "redirect:/";

        } catch (Exception e) {
            logger.error("Error creating new folder: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@ModelAttribute DeleteFolderDto deleteFolderDto,
                               Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);

            if (deleteFolderDto.getFolderPath() == null || deleteFolderDto.getFolderPath().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Folder path is empty. Please select a valid folder.");
                return "redirect:/";
            }

            homePageService.deleteFolder(principal, deleteFolderDto);

            redirectAttributes.addAttribute("path", deleteFolderDto.getPath());
            redirectAttributes.addFlashAttribute("successMessage", "Folder deleted successfully.");
            return "redirect:/";

        }  catch (Exception e) {
            logger.error("Error deleting folder: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/upload-folder")
    public String uploadFolder(@ModelAttribute UploadFolderDto uploadFolderDto,
                               Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);
            homePageService.uploadFolder(principal, uploadFolderDto);

            redirectAttributes.addAttribute("path", uploadFolderDto.getPath());
            return "redirect:/";

        } catch (Exception e) {
            logger.error("Error uploading folder: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/download-folder")
    public ResponseEntity<byte[]> downloadFolder(@RequestParam("folderPath") String folderPath, Principal principal) {
        try {
            validatePrincipal(principal);

            User user = userService.getAuthenticatedUser(principal);
            ZipDto zipDto = minIoService.downloadFolder(user, folderPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipDto.getFileName() + ".zip\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipDto.getByteArrayOutputStream().toByteArray());

        } catch (Exception e) {
            logger.error("Error downloading folder: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/rename")
    public String renameFile(@ModelAttribute RenameFileDto renameFileDto,
                             Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);
            User user = userService.getAuthenticatedUser(principal);

            redirectAttributes.addAttribute("path", renameFileDto.getPath());

            if (renameFileDto.getNewFileName() == null || renameFileDto.getNewFileName().isBlank() || !renameFileDto.getNewFileName().matches("^[a-zA-Z]+$")) {
                redirectAttributes.addFlashAttribute("errorMessage","File name can't be empty, or contain invalid characters");
                return "redirect:/";
            }

            String newFilePath = renameFileDto.getPath() + renameFileDto.getNewFileName();
            minIoService.renameFile(user, renameFileDto.getFilePath(), newFilePath);

            redirectAttributes.addFlashAttribute("successMessage", "File renamed successfully.");
            return "redirect:/";

        }  catch (Exception e) {
            logger.error("Error renaming file: {}", e.getMessage(), e);
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

    }

    @PostMapping("/rename-folder")
    public String renameFolder(@ModelAttribute RenameFolderDto renameFolderDto,
                               Principal principal, Model model, RedirectAttributes redirectAttributes) {
        try {
            validatePrincipal(principal);
            User user = userService.getAuthenticatedUser(principal);

            redirectAttributes.addAttribute("path", renameFolderDto.getPath());

            if (renameFolderDto.getNewFolderName() == null || renameFolderDto.getNewFolderName().isBlank() || !renameFolderDto.getNewFolderName().matches("^[a-zA-Z]+$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Folder name can't be empty, or contain invalid characters");
                return "redirect:/";
            }

            minIoService.renameFolder(user, renameFolderDto);

            redirectAttributes.addFlashAttribute("successMessage", "Folder renamed successfully.");
            return "redirect:/";

        }  catch (Exception e) {
            logger.error("Error renaming folder: {}", e.getMessage(), e);

            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    private void validatePrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isEmpty()) {
            logger.error("Invalid principal: User is not authenticated.");
            throw new IllegalArgumentException("Invalid principal: User is not authenticated.");
        }
    }
}
