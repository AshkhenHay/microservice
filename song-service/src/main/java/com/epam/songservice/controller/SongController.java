package com.epam.songservice.controller;

import com.epam.songservice.dto.SongDto;
import com.epam.songservice.dto.SongIdDto;
import com.epam.songservice.dto.SongMetaCreateDto;
import com.epam.songservice.exception.ValidationException;
import com.epam.songservice.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService service;

    @PostMapping
    public ResponseEntity<SongIdDto> createSong(@RequestBody SongDto dto) {
        var song = service.createSong(dto);
        return ResponseEntity.ok(new SongIdDto(song.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable String id) {
        Long parsedId;
        try {
            parsedId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid value '" + id + "' for ID. Must be a positive integer");
        }
        if (parsedId <= 0) {
            throw new ValidationException("Invalid value '" + id + "' for ID. Must be a positive integer");
        }
        var song = service.getSong(parsedId);
        var dto = new SongDto();
        BeanUtils.copyProperties(song, dto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteSongs(@RequestParam String id) {
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
            List<Long> deleted = service.deleteSongs(ids);
            return ResponseEntity.ok(Map.of("ids", deleted));
        } catch (ValidationException e) {
            throw e;
        }
    }

    // Endpoints used by Resource Service via Feign client
    @PostMapping("/metadata")
    public ResponseEntity<Void> createSongMeta(@RequestBody SongMetaCreateDto meta) {
        // Convert to SongDto and save
        SongDto dto = new SongDto();
        dto.setId(meta.getResourceId());
        dto.setName(meta.getTitle());
        dto.setArtist(meta.getArtist());
        dto.setAlbum(meta.getAlbum());
        dto.setDuration(meta.getDuration());
        dto.setYear(meta.getReleaseYear());
        service.createSong(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/metadata")
    public ResponseEntity<Void> deleteSongMetas(@RequestParam List<Long> resourceIds) {
        service.deleteSongs(resourceIds);
        return ResponseEntity.ok().build();
    }
}