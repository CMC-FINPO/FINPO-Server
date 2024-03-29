package kr.finpo.api.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.ImgUploadDto;
import kr.finpo.api.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${upload.url}")
    private String uploadUrl;

    public S3Uploader(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public ImgUploadDto uploadImg(ImgUploadDto dto, String path) {
        try {
            List<String> imgUrls = new ArrayList<>();

            Optional.ofNullable(dto.imgFiles()).ifPresent(imgFiles -> {
                imgFiles.forEach(imgFile -> {
                    imgUrls.add(uploadFile(path, imgFile));
                });
            });
            return ImgUploadDto.response(imgUrls);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Resource downloadFile(String fileName) throws IOException {
        S3Object s3Object = amazonS3Client.getObject(bucket, fileName);
        byte[] content = IOUtils.toByteArray(s3Object.getObjectContent());
        return new ByteArrayResource(content);
    }

    public String uploadFile(String filePath, MultipartFile multipartFile) {
        try {
            File uploadFile = convert(multipartFile).get();
            return upload(filePath, uploadFile);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
        }
    }

    private String upload(String filePath, File uploadFile) {
        String fileName = uploadFile.getName();
        String newFileName = filePath + "/" + UUID.randomUUID() + fileName.substring(fileName.lastIndexOf('.'));

        String uploadImageUrl = putS3(uploadFile, newFileName);
        removeNewFile(uploadFile);
        return uploadUrl + newFileName;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
            new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            return;
        }
    }

    private Optional<File> convert(MultipartFile file) {
        try {
            File convertFile = resizeImageFile(file, System.getProperty("user.dir") + "/" + file.getOriginalFilename(),
                "png");
            return Optional.of(convertFile);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
        }
    }

    private File resizeImageFile(MultipartFile file, String filePath, String formatName) {
        try {
            BufferedImage inputImage = ImageIO.read(file.getInputStream());
            int originWidth = inputImage.getWidth();
            int originHeight = inputImage.getHeight();
            int newWidth = 500;

            int newHeight = (originHeight * newWidth) / originWidth;
            Image resizeImage = inputImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = newImage.getGraphics();
            graphics.drawImage(resizeImage, 0, 0, null);
            graphics.dispose();

            File newFile = new File(filePath);
            ImageIO.write(newImage, formatName, newFile);
            return newFile;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
        }
    }
}