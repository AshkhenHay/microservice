package com.epam.resourceservice.client;

import com.epam.resourceservice.dto.SongMetaCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "song-service", url = "${feign.client.song-service.url:http://localhost:8081}")
public interface SongMetadataClient {

    @PostMapping("/songs")
    void createSongMeta(@RequestBody SongMetaCreateDto metaDto);

    @DeleteMapping("/songs")
    void deleteSongMetas(@RequestParam(value = "id") String resourceIds);
}

