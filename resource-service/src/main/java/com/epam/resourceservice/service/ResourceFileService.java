package com.epam.resourceservice.service;

import com.epam.resourceservice.client.SongMetadataClient;
import com.epam.resourceservice.dto.SongMetaCreateDto;
import com.epam.resourceservice.entity.ResourceFile;
import com.epam.resourceservice.exception.NotFoundException;
import com.epam.resourceservice.exception.ValidationException;
import com.epam.resourceservice.repository.ResourceFileRepository;
import com.epam.resourceservice.util.Mp3MetadataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class ResourceFileService {
    private static final Logger log = LoggerFactory.getLogger(ResourceFileService.class);

    @Autowired
    private ResourceFileRepository repo;

    @Autowired
    private SongMetadataClient songMetadataClient;

    public ResourceFile saveResource(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty or null");
        }
        String contentType = file.getContentType();
        if (!Objects.equals(contentType, "audio/mpeg")) {
            throw new ValidationException("Invalid file format: " + contentType + ". Only MP3 files are allowed");
        }

        try {
            byte[] fileBytes = file.getBytes();
            if (fileBytes.length == 0) {
                throw new ValidationException("File content cannot be empty");
            }

            // Save resource file first
            ResourceFile entity = new ResourceFile();
            entity.setData(fileBytes);
            ResourceFile saved = repo.save(entity);
            log.info("Successfully saved resource with ID: {}", saved.getId());

            // Extract metadata and create song metadata in Song Service
            try {
                SongMetaCreateDto metadata = Mp3MetadataExtractor.extractMetadata(fileBytes, saved.getId());
                songMetadataClient.createSongMeta(metadata);
                log.info("Successfully created song metadata for resource {}", saved.getId());
            } catch (Exception e) {
                log.error("Failed to extract or create song metadata for resource {}: {}", saved.getId(), e.getMessage());
                // Delete the resource if metadata creation fails
                repo.deleteById(saved.getId());
                throw new ValidationException("Failed to extract MP3 metadata: " + e.getMessage());
            }

            return saved;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to save resource: {}", e.getMessage(), e);
            throw new ValidationException("Failed to save resource: " + e.getMessage());
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
            // Delete metadata from Song Service
            try {
                String idsCsv = String.join(",", existing.stream().map(String::valueOf).toList());
                songMetadataClient.deleteSongMetas(idsCsv);
                log.info("Successfully deleted song metadata for resources: {}", idsCsv);
            } catch (Exception e) {
                log.warn("Failed to delete song metadata: {}", e.getMessage());
                // Continue with deletion even if metadata deletion fails
            }

            repo.deleteAllById(existing);
            log.info("Successfully deleted {} resources", existing.size());
        }

        return existing;
    }
}



