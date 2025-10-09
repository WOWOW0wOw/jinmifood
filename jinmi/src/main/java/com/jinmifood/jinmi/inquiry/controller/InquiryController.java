package com.jinmifood.jinmi.inquiry.controller;

import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import com.jinmifood.jinmi.inquiry.dto.request.AddInquiryRequest;
import com.jinmifood.jinmi.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;


    @GetMapping("/listByItem")
    public StatusResponseDTO listByItem(Long itemId) {
        return StatusResponseDTO.ok();
    }

    @GetMapping("/ListAll")
    public StatusResponseDTO ListAll() {
        return StatusResponseDTO.ok();
    }

    @PostMapping("/add")
    public StatusResponseDTO add(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddInquiryRequest request) {
        return StatusResponseDTO.ok();
    }

    @PostMapping("/remove")
    public StatusResponseDTO removeInquiry(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long itemId) {
        return StatusResponseDTO.ok();
    }

    @PostMapping("/update")
    public StatusResponseDTO updateInquiry(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddInquiryRequest request) {
        return StatusResponseDTO.ok();
    }

}
