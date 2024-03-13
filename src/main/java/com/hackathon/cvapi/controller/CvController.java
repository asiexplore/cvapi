package com.hackathon.cvapi.controller;

import com.hackathon.cvapi.service.VertexAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class CvController {

    @Autowired
    private VertexAIService vertexAIService;

    @GetMapping("/cv/results")
    public ResponseEntity<String> getSearchResults(@RequestParam String qry) {
        String strResponse = vertexAIService.searchDocs(qry);
        return new ResponseEntity<>(strResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/single-file-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleFileUploadUsingCurl(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        vertexAIService.storeDoc(multipartFile);
        return ResponseEntity.ok("Uploaded File: " + multipartFile.getOriginalFilename());
    }
}
