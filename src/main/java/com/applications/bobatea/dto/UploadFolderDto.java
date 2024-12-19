package com.applications.bobatea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UploadFolderDto {
    private List<MultipartFile> fileList;
    private String path;
}
