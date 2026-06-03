package com.neusoft.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload-dir:./uploads}")
    private String uploadPath;

    public String upload(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        // 保存文件
        File dest = new File(uploadPath + File.separator + fileName);
        file.transferTo(dest);

        // 返回文件访问路径（这里简单处理，实际项目用Nginx或OSS）
        return "/uploads/" + fileName;
    }
}