package com.applications.bobatea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HomePageDto {
    private ArrayList<PathDto> breadcrumbs;
    private String path;
    private List<FileDto> fileDtos;
    private UserDto userDto;
}
