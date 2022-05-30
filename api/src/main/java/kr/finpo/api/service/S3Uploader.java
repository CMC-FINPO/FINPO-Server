package kr.finpo.api.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
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
    return newFileName;
  }

  private String putS3(File uploadFile, String fileName) {
    amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
    return amazonS3Client.getUrl(bucket, fileName).toString();
  }

  private void removeNewFile(File targetFile) {
    if (targetFile.delete()) return;
  }

  private Optional<File> convert(MultipartFile file) {
//    File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
//    convertFile.createNewFile();
//    try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//      fos.write(file.getBytes());
//    }
    try {
      File convertFile = resizeImageFile(file, System.getProperty("user.dir") + "/" + file.getOriginalFilename(), "png");
      return Optional.of(convertFile);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
    }
  }

  private File resizeImageFile(MultipartFile file, String filePath, String formatName) {
    try {
//    BufferedImage inputImage = ImageIO.read(file.getInputStream());
      BufferedImage inputImage = getBufferedImage(file);
      int originWidth = inputImage.getWidth();
      int originHeight = inputImage.getHeight();
      int newWidth = 500;

      int newHeight = (originHeight * newWidth) / originWidth;
      Image resizeImage = inputImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
      BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
      Graphics graphics = newImage.getGraphics();
      graphics.drawImage(resizeImage, 0, 0, null);
      graphics.dispose();
      // 이미지 저장
      File newFile = new File(filePath);
      ImageIO.write(newImage, formatName, newFile);
      return newFile;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
    }
  }

  private BufferedImage getBufferedImage(MultipartFile file) {
    try {
      final java.awt.Image image = Toolkit.getDefaultToolkit().createImage(file.getBytes());

      final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
      final ColorModel RGB_OPAQUE =
          new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

      PixelGrabber pg = new PixelGrabber(image, 0, 0, -1, -1, true);
      pg.grabPixels();
      int width = pg.getWidth(), height = pg.getHeight();
      DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
      WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
      return new BufferedImage(RGB_OPAQUE, raster, false, null);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.IMAGE_UPLOAD_ERROR, e);
    }
  }
}