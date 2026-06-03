package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadUtil fileUploadUtil;

    // 上传头像/体检报告
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        try {
            String url = fileUploadUtil.upload(file);
            return R.ok(url);
        } catch (IOException e) {
            return R.fail("上传失败：" + e.getMessage());
        }
    }
}