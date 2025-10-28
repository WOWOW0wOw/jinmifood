package com.jinmifood.jinmi.common.S3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        //  파일 이름이 중복되지 않도록 UUID를 사용
        String originalFilename = multipartFile.getOriginalFilename();
        String uniqueFilename = dirName + "/" + UUID.randomUUID().toString() + "-" + originalFilename;

        log.info("S3에 업로드할 파일 이름: {}", uniqueFilename);

        //  S3에 업로드하기 위한 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFilename)
                .build();

        //  S3에 파일 업로드
        // RequestBody.fromInputStream을 사용하여 파일의 InputStream과 길이를 전달
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

        //  업로드된 파일의 URL을 가져오기
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucket)
                .key(uniqueFilename)
                .build();

        String uploadedFileUrl = s3Client.utilities().getUrl(getUrlRequest).toString();
        log.info("S3에 업로드된 파일 URL: {}", uploadedFileUrl);

        return uploadedFileUrl;
    }

}
