package com.livefox.video.repository;

import com.livefox.video.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Integer> {
    Video findById(int id);
}
