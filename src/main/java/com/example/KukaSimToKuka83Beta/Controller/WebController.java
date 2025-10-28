package com.example.KukaSimToKuka83Beta.Controller;

import com.example.KukaSimToKuka83Beta.service.ContentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class WebController {

    private final ContentService contentService;

    public WebController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/")
    public String index() {
        return "index2";
    }

    @PostMapping("/upload-eight-six-to-eight-three")
    public ResponseEntity<InputStreamResource> uploadTwoFilesToOld(
            @RequestParam("srcFile") MultipartFile srcFile,
            @RequestParam("datFile") MultipartFile datFile
    ) {
        try {
            String transformedSrc = contentService.transformSrcToKukaOld(new String(srcFile.getBytes()));
            String transformedDat = contentService.transformDatToKukaOld(new String(datFile.getBytes()));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zos.putNextEntry(new ZipEntry(srcFile.getOriginalFilename()));
                zos.write(transformedSrc.getBytes());
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry(datFile.getOriginalFilename()));
                zos.write(transformedDat.getBytes());
                zos.closeEntry();
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            InputStreamResource resource = new InputStreamResource(bais);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=FromKukaSimProgram.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(baos.size())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/upload-eight-three-to-eight-six")
    public ResponseEntity<InputStreamResource> uploadTwoFilesFromOld(
            @RequestParam("srcFile") MultipartFile srcFile,
            @RequestParam("datFile") MultipartFile datFile
    ) {
        try {
            String transformedSrc = contentService.transformSrcFromKukaOld(new String(srcFile.getBytes()));
            String transformedDat = contentService.transformDatFromKukaOld(new String(datFile.getBytes()));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zos.putNextEntry(new ZipEntry(srcFile.getOriginalFilename()));
                zos.write(transformedSrc.getBytes());
                zos.closeEntry();

                zos.putNextEntry(new ZipEntry(datFile.getOriginalFilename()));
                zos.write(transformedDat.getBytes());
                zos.closeEntry();
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            InputStreamResource resource = new InputStreamResource(bais);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ToKukaSimProgram.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(baos.size())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}