package org.example.relief.repository;

import org.example.relief.model.LocalNoti;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalNotiRepository extends JpaRepository<LocalNoti, Long> {

    List<LocalNoti> findByReceiverUserIdOrderBySentAtDesc(Long userId);
}
