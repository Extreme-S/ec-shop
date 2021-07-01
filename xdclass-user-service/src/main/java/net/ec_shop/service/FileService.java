package net.ec_shop.service;


import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadUserImg(MultipartFile file);
}
