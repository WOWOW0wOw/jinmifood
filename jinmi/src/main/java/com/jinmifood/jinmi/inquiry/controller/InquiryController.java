package com.jinmifood.jinmi.inquiry.controller;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import com.jinmifood.jinmi.inquiry.dto.request.AddInquiryRequest;
import com.jinmifood.jinmi.inquiry.dto.request.UpdateInquiryRequest;
import com.jinmifood.jinmi.inquiry.dto.response.ViewInquiryResponse;
import com.jinmifood.jinmi.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;


    @GetMapping("/listByItem")
    public StatusResponseDTO listByItem(@RequestParam Long itemId) {

        List<ViewInquiryResponse> list = inquiryService.findAllInquiryByItemId(itemId);

        return StatusResponseDTO.ok(list);
    }

    @GetMapping("/listAll")
    public StatusResponseDTO ListAll() {

        List<ViewInquiryResponse> list = inquiryService.findAllInquiry();

        return StatusResponseDTO.ok(list);
    }

    @PostMapping("/add")
    public StatusResponseDTO add(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddInquiryRequest request) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();

        inquiryService.addInquiry(request, userId);


        return StatusResponseDTO.ok("문의가 성공적으로 저장되었습니다.");
    }

    @PostMapping("/remove")
    public StatusResponseDTO removeInquiry(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long InquiryId) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        inquiryService.deleteInquiry(InquiryId);

        return StatusResponseDTO.ok("문의가 성공적으로 삭제되었습니다.");
    }

    @PostMapping("/update")
    public StatusResponseDTO updateInquiry(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateInquiryRequest request,@RequestParam Long InquiryId) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();

        inquiryService.updateInquiry(request, InquiryId, userId);

        return StatusResponseDTO.ok("문의가 성공적으로 수정되었습니다.");
    }

}
