package com.jinmifood.jinmi.user.repository;

import com.jinmifood.jinmi.user.domain.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findAllByOrderByAccessTimeDesc();
}
