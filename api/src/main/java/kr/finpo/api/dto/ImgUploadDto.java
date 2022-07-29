package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImgUploadDto(
    List<String> imgUrls,
    List<MultipartFile> imgFiles
) {

    public ImgUploadDto {
    }

    public static ImgUploadDto response(List<String> imgUrls) {
        return new ImgUploadDto(imgUrls, null);
    }
}
