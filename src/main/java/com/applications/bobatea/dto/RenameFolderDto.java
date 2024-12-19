package com.applications.bobatea.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFolderDto {
    String path;
    String folderPath;
    String newFolderName;
}
