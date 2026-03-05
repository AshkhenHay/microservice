package com.epam.resourceservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
public class ResourceFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    @Column(nullable = false)
    private byte[] data;

    public ResourceFile(Long id, byte[] data) {
        this.id = id;
        this.data = data;
    }


    public ResourceFile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}