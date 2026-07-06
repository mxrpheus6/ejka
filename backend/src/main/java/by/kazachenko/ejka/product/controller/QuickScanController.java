package by.kazachenko.ejka.product.controller;

import by.kazachenko.ejka.product.rabbitmq.ImageProcessingResponse;
import by.kazachenko.ejka.product.service.impl.QuickScanServiceImpl;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/scans")
@RequiredArgsConstructor
public class QuickScanController {

    private final QuickScanServiceImpl quickScanService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeImage(@RequestParam("file") MultipartFile file) {
        String scanId = quickScanService.initiateScan(file);
        return ResponseEntity.ok(Map.of("scanId", scanId));
    }

    @GetMapping("/result/{scanId}")
    public ResponseEntity<ImageProcessingResponse> getResult(@PathVariable String scanId) {
        ImageProcessingResponse result = quickScanService.getResult(scanId);

        if (result == null) {
            return ResponseEntity.accepted().build();
        }

        return ResponseEntity.ok(result);
    }
}
