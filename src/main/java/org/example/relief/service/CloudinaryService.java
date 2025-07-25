package org.example.relief.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map uploadImage(MultipartFile file) throws IOException;

    Map uploadImage(MultipartFile file, String folder) throws IOException;
}
