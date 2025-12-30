package com.jinmifood.jinmi.user.controller;

import com.jinmifood.jinmi.user.domain.AccessLog;
import com.jinmifood.jinmi.user.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/access-logs")
@RequiredArgsConstructor
public class AdminAccessLogController {
    private final AccessLogRepository accessLogRepository;

    @GetMapping
    public ResponseEntity<List<AccessLog>> getLogs() {
        return ResponseEntity.ok(accessLogRepository.findAllByOrderByAccessTimeDesc());
    }
}
