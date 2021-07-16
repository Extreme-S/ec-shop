package net.ec_shop.service;


import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传用户头像到OSS
     *
     * @param file
     * @return
     */
    String uploadUserImg(MultipartFile file);
}
