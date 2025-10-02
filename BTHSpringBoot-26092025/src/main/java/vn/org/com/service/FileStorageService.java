package vn.org.com.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path folder = root.resolve(subFolder);
        Files.createDirectories(folder);
        String ext = OptionalExt.getExt(file.getOriginalFilename());
        String name = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path dest = folder.resolve(name).normalize();
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        // Trả về path tương đối dưới uploads/
        return subFolder + "/" + name;
    }

    public void deleteIfExists(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) return;
        Path p = root.resolve(relativePath).normalize();
        Files.deleteIfExists(p);
    }

    // helper
    static class OptionalExt {
        static String getExt(String name) {
            if (name == null) return "";
            int i = name.lastIndexOf('.');
            return (i >= 0) ? name.substring(i + 1) : "";
        }
    }
}
