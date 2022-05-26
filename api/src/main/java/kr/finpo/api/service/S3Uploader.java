package kr.finpo.api.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class S3Uploader {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public S3Uploader(AmazonS3Client amazonS3Client) {
    this.amazonS3Client = amazonS3Client;
  }

  public Resource downloadFile(String fileName) throws IOException {
    S3Object s3Object = amazonS3Client.getObject(bucket, fileName);
    byte[] content = IOUtils.toByteArray(s3Object.getObjectContent());
    Resource resource = new ByteArrayResource(content);
    return resource;
  }

  public String uploadFile(String filePath, MultipartFile multipartFile) throws Exception {
    File uploadFile = convert(multipartFile)
        .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
    return upload(filePath, uploadFile);
  }

  private String upload(String filePath, File uploadFile) {
    String fileName = filePath + "/" + UUID.randomUUID() + uploadFile.getName();
    String uploadImageUrl = putS3(uploadFile, fileName);
    removeNewFile(uploadFile);
    return fileName;
  }

  private String putS3(File uploadFile, String fileName) {
    amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
    return amazonS3Client.getUrl(bucket, fileName).toString();
  }

  private void removeNewFile(File targetFile) {
    if (targetFile.delete()) return;
  }

  private Optional<File> convert(MultipartFile file) throws Exception {
//    File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
//    convertFile.createNewFile();
//    try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//      fos.write(file.getBytes());
//    }
    File convertFile = resizeImageFile(file, System.getProperty("user.dir") + "/" + file.getOriginalFilename(), "png");
    return Optional.of(convertFile);
  }

  // 이미지 크기 줄이기
  private File resizeImageFile(MultipartFile file, String filePath, String formatName) throws Exception {
    // 이미지 읽어 오기
    BufferedImage inputImage = ImageIO.read(file.getInputStream());
    // 이미지 세로 가로 측정
    int originWidth = inputImage.getWidth();
    int originHeight = inputImage.getHeight();
    // 변경할 가로 길이
    int newWidth = 500;

    int newHeight = (originHeight * newWidth) / originWidth;
    Image resizeImage = inputImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
    BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics graphics = newImage.getGraphics();
    graphics.drawImage(resizeImage, 0, 0, null);
    graphics.dispose();
    // 이미지 저장
    File newFile = new File(filePath);
    ImageIO.write(newImage, formatName, newFile);
    return newFile;
  }
}