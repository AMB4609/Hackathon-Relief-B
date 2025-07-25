package org.example.relief.repository;

import org.example.relief.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByImageId(Long imageId);
    Optional<Image> findByImagePublicId(String publicId);
}
