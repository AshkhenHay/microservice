package com.epam.resourceservice.controller;

import com.epam.resourceservice.dto.ResourceIdDto;
import com.epam.resourceservice.entity.ResourceFile;
import com.epam.resourceservice.exception.ValidationException;
import com.epam.resourceservice.service.ResourceFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceFileController {
    private ResourceFileService service;

    public ResourceFileController(ResourceFileService service) {
        this.service = service;
    }

    @PostMapping(consumes = "audio/mpeg", produces = "application/json")
    public ResponseEntity<ResourceIdDto> uploadResource(@RequestBody byte[] audio) {
        if (audio == null || audio.length == 0) {
            throw new ValidationException("File content is empty");
        }
        var file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", audio);
        var saved = service.saveResource(file);
        return ResponseEntity.ok(new ResourceIdDto(saved.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable String id) {
        Long parsedId;
        try {
            parsedId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid value '" + id + "' for ID. Must be a positive integer");
        }
        if (parsedId <= 0) {
            throw new ValidationException("Invalid value '" + id + "' for ID. Must be a positive integer");
        }
        ResourceFile resource = service.getById(parsedId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(resource.getData());
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteResources(@RequestParam String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("ID parameter is required");
        }
        if (id.length() > 200) {
            throw new ValidationException("CSV string is too long: received " + id.length() + " characters, maximum allowed is 200");
        }

        try {
            List<Long> ids = Arrays.stream(id.split(","))
                    .map(String::trim)
                    .map(idStr -> {
                        try {
                            return Long.parseLong(idStr);
                        } catch (NumberFormatException e) {
                            throw new ValidationException("Invalid ID format: '" + idStr + "'. Only positive integers are allowed");
                        }
                    })
                    .filter(num -> num > 0)
                    .toList();
            List<Long> deletedIds = service.deleteByIds(ids);
            return ResponseEntity.ok(Map.of("ids", deletedIds));
        } catch (ValidationException e) {
            throw e;
        }
    }
}

