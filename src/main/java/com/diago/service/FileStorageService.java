package com.diago.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Enregistre les photos envoyées par l'admin dans le dossier "uploads/cars" du projet. */
@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads", "cars");

    /** Retourne l'adresse de la photo (/uploads/cars/xxx.jpg), ou null si aucun fichier choisi. */
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier choisi doit être une image (jpg, png, webp...).");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')).toLowerCase() : ".jpg";
        if (!ext.matches("\\.(jpg|jpeg|png|webp|gif)")) ext = ".jpg";
        try {
            Files.createDirectories(root);
            String stored = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), root.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/cars/" + stored;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String> saveAll(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files == null) return urls;
        for (MultipartFile f : files) {
            String u = save(f);
            if (u != null) urls.add(u);
        }
        return urls;
    }
}