package com.applications.bobatea.controllers;

import com.applications.bobatea.models.User;
import com.applications.bobatea.services.MinIoService;
import com.applications.bobatea.services.UserService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;

@Controller
public class FilesController {

    private MinIoService minIoService;
    private UserService userService;

    @Autowired
    public FilesController(MinIoService minIoService, UserService userService) {
        this.minIoService = minIoService;
        this.userService = userService;
    }

    /*
    @GetMapping("/create-bucket")
    public String test() throws UserExistsException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        minIoService.CreateBucket();
        return "redirect:/";
    }

    @GetMapping("/create-folder")
    public String test1(Principal principal, @RequestParam("folderName") String folderName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        User byUsername = userService.findByUsername(principal.getName()).orElseThrow(RuntimeException::new);

        minIoService.createNewFolder(byUsername, folderName, "mamka");

        return "redirect:/";
    }
    */

    @PostMapping("/upload")
    public String test2(@RequestParam("file") MultipartFile file, Principal principal, Model model) {
        try {
            User user = userService.findByUsername(principal.getName()).get();

            String fileName = file.getOriginalFilename();
            InputStream fileStream = file.getInputStream();
            long fileSize = file.getSize();
            String contentType = file.getContentType();

            // Upload file to MinIO
            minIoService.uploadFile(user, fileName, fileStream, fileSize, contentType);
        }catch (Exception e){
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String test3(@RequestParam("fileName") String fileName, Principal principal, Model model) {
        try {
            User user = userService.findByUsername(principal.getName()).get();

            minIoService.deleteFile(user, fileName);

        }catch (Exception e){
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
        return "redirect:/";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") String fileName, Principal principal) {
        try {
            // Логирование
            System.out.println("Downloading file: " + fileName);

            User user = userService.findByUsername(principal.getName()).orElseThrow(() ->
                    new RuntimeException("User not found: " + principal.getName()));

            System.out.println("User found: " + user.getUsername());

            // Загрузка файла из MinIO
            InputStream fileStream = minIoService.downloadFile(user, fileName);

            System.out.println("File stream acquired");

            InputStreamResource resource = new InputStreamResource(fileStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace(); // Вывод стека ошибок в лог
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/")
    public String showHomePage(Principal principal, Model model) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        try {
            User user = userService.findByUsername(principal.getName()).get();
            List<String> files = minIoService.listFilesInFolder(user);
            model.addAttribute("files", files);

            return "index";
        }catch (Exception e){
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

    }

}
