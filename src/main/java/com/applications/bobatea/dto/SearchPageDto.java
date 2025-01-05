package com.applications.bobatea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class SearchPageDto {
    private UserDto userDto;
    private ArrayList<FileDto> fileDtoList;
    private String keyword;
}
