package edu.iuc.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String PATH = "files/";
    
    static {
        try {
            Files.createDirectories(Paths.get(PATH));
        } catch (IOException e) {
            System.err.println("Files klasörü oluşturulamadı: " + e.getMessage());
        }
    }

    public static synchronized void saveFile(String fileName, String content) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("Dosya adı boş olamaz");
        }
        
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IOException("Geçersiz dosya adı: " + fileName);
        }
        
        Path filePath = Paths.get(PATH + fileName);
        String processedContent = content.replace("\\n", "\n");
        Files.write(filePath, processedContent.getBytes("UTF-8"));
    }

    public static synchronized String loadFile(String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("Dosya adı boş olamaz");
        }
        
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IOException("Geçersiz dosya adı: " + fileName);
        }
        
        Path filePath = Paths.get(PATH + fileName);
        if (!Files.exists(filePath)) {
            throw new IOException("Dosya bulunamadı: " + fileName);
        }
        
        return new String(Files.readAllBytes(filePath), "UTF-8");
    }
    
    public static synchronized List<String> listFiles() {
        List<String> fileList = new ArrayList<>();
        try {
            File dir = new File(PATH);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((file) -> file.isFile());
                if (files != null) {
                    for (File file : files) {
                        fileList.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Dosyalar listelenirken hata: " + e.getMessage());
        }
        return fileList;
    }
    
    public static synchronized boolean createFile(String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return false;
            }
            
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return false;
            }
            
            Path filePath = Paths.get(PATH + fileName);
            if (Files.exists(filePath)) {
                return false;
            }
            
            Files.write(filePath, "".getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
            System.err.println("Dosya oluşturulamadı: " + fileName + " - " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized boolean fileExists(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(PATH + fileName);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}

