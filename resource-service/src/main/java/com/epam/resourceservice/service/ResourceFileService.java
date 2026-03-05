package com.epam.resourceservice.service;

import com.epam.resourceservice.entity.ResourceFile;
import com.epam.resourceservice.exception.NotFoundException;
import com.epam.resourceservice.repository.ResourceFileRepository;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerErrorException;

import java.util.List;
import java.util.Objects;

@Service
public class ResourceFileService {
    private static final Logger log = LoggerFactory.getLogger(ResourceFileService.class);

    @Autowired
    private ResourceFileRepository repo;

    public ResourceFile saveResource(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty or null");
        }
        if (!Objects.equals(file.getContentType(), "audio/mpeg")) {
            throw new ValidationException("Invalid MP3 - content type must be audio/mpeg");
        }

        try {
            ResourceFile entity = new ResourceFile();
            entity.setData(file.getBytes());
            ResourceFile saved = repo.save(entity);
            log.info("Successfully saved resource with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save resource: {}", e.getMessage(), e);
            throw new ServerErrorException("Failed to save resource", e);
        }
    }

    public ResourceFile getById(Long id) {
        if (id == null || id < 1) {
            throw new ValidationException("ID must be a positive number");
        }
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource with ID=" + id + " not found"));
    }

    public List<Long> deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("No IDs provided for deletion");
        }

        List<Long> existing = repo.findAllById(ids)
                .stream()
                .map(ResourceFile::getId)
                .toList();

        if (!existing.isEmpty()) {
            repo.deleteAllById(existing);
            log.info("Successfully deleted {} resources", existing.size());
        }

        return existing;
    }
}

