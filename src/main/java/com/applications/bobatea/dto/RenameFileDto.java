package com.applications.bobatea.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFileDto {
    String filePath;
    String path;
    String newFileName;
}
