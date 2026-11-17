package com.guanlan.notes.service;

import com.guanlan.notes.model.base.ApiResponse;
import com.guanlan.notes.model.vo.upload.ImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * 上传图片
     */
    ApiResponse<ImageVO> uploadImage(MultipartFile file);
}