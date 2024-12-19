package com.applications.bobatea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileDto {

    private String fileName;
    private String filePath;
    private boolean isFolder;

}
