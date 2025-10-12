package com.jinmifood.jinmi.inquiry.repository;

import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import com.jinmifood.jinmi.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    boolean existsByUserIdAndInquiryId(Long userId, Long inquiryId);

    List<Inquiry> findInquiriesByItemId(Long itemId);

}
