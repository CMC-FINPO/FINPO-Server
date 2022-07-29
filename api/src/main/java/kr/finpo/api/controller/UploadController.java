package kr.finpo.api.controller;

import java.io.IOException;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.ImgUploadDto;
import kr.finpo.api.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {

    private final S3Uploader s3Uploader;

    @GetMapping("/{*path}")
    public ResponseEntity<Resource> getUploadedFile(@PathVariable("path") String path) throws IOException {
        Resource resource = s3Uploader.downloadFile(path.substring(1));
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "image/" + path.substring(path.lastIndexOf(".")) + 1);
        return new ResponseEntity<>(resource, header, HttpStatus.OK);
    }

    @PostMapping("/{path}")
    public DataResponse<Object> upload(
        @ModelAttribute ImgUploadDto body,
        @PathVariable("path") String path
    ) {
        return DataResponse.of(s3Uploader.uploadImg(body, path));
    }
}
