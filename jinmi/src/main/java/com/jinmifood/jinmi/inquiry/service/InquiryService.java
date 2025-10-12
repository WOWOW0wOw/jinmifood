package com.jinmifood.jinmi.inquiry.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import com.jinmifood.jinmi.inquiry.dto.request.AddInquiryRequest;
import com.jinmifood.jinmi.inquiry.dto.request.UpdateInquiryRequest;
import com.jinmifood.jinmi.inquiry.dto.response.ViewInquiryResponse;
import com.jinmifood.jinmi.inquiry.repository.InquiryRepository;
import com.jinmifood.jinmi.review.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public void addInquiry(AddInquiryRequest request,Long userId) {

        Inquiry inquiry = request.toEntity();
        log.info("userId = " + userId);
        inquiry.setUserId(userId);
        inquiryRepository.save(inquiry);

    }

    @Transactional
    public void deleteInquiry(Long InquiryId) {

        log.info("InquiryId = " + InquiryId);
        inquiryRepository.deleteById(InquiryId);

    }

    @Transactional
    public void updateInquiry(UpdateInquiryRequest request, Long InquiryId, Long userId) {

        log.info("userId = " + userId);
        log.info("InquiryId = " + InquiryId);
        if(!inquiryRepository.existsByUserIdAndInquiryId(userId, InquiryId)) {
            throw new CustomException(ErrorException.NOT_FOUND);
        }

        Inquiry inquiry = inquiryRepository.findById(InquiryId)
                .orElseThrow(() -> {
                    log.warn("Inquiry를 찾을 수 없습니다. inquiryId = {}", InquiryId);
                    return new IllegalArgumentException("존재하지 않는 문의입니다.");
                });
        log.info("inquiry = " + inquiry);

        inquiry.updateInquiryDetails(request);

    }

    public List<ViewInquiryResponse> findAllInquiry() {
        List<Inquiry> inquiryList = inquiryRepository.findAll();
        return inquiryList.stream()
                .map(ViewInquiryResponse::new)
                .collect(Collectors.toList());
    }

    public List<ViewInquiryResponse> findAllInquiryByItemId(Long itemId) {
        List<Inquiry> inquiryList = inquiryRepository.findInquiriesByItemId(itemId);
        return inquiryList.stream()
                .map(ViewInquiryResponse::new)
                .collect(Collectors.toList());
    }


}
