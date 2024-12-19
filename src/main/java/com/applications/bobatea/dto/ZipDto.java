package com.applications.bobatea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;

@Getter
@Setter
@AllArgsConstructor
public class ZipDto {
    private ByteArrayOutputStream byteArrayOutputStream;
    private String fileName;
}
