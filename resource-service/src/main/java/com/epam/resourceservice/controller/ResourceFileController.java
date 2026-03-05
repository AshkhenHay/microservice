package com.epam.resourceservice.controller;

import com.epam.resourceservice.dto.ResourceIdDto;
import com.epam.resourceservice.entity.ResourceFile;
import com.epam.resourceservice.service.ResourceFileService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
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
        var file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", audio);
        var saved = service.saveResource(file);
        return ResponseEntity.ok(new ResourceIdDto(saved.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Long id) {
        ResourceFile resource = service.getById(id);
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
            throw new ValidationException("CSV too long");
        }

        try {
            List<Long> ids = Arrays.stream(id.split(","))
                    .map(Long::parseLong)
                    .toList();
            List<Long> deletedIds = service.deleteByIds(ids);
            return ResponseEntity.ok(Map.of("ids", deletedIds));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid ID format: IDs must be numeric");
        }
    }
}

