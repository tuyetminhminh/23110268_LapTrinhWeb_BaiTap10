package vn.org.com.web;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final Path uploadDir = Paths.get("uploads"); // thư mục ở project root

    public UploadController() throws IOException {
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","Empty file"));
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String name = System.currentTimeMillis() + "-" + Math.abs(original.hashCode()) + ext;
        Path dest = uploadDir.resolve(name);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        Map<String,Object> result = new HashMap<>();
        result.put("fileName", name);               // lưu vào DB/GraphQL
        result.put("url", "/uploads/" + name);      // để hiển thị ngay
        return ResponseEntity.ok(result);
    }
}
