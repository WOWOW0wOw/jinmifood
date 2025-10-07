package com.jinmifood.jinmi.inquiry.repository;

import com.jinmifood.jinmi.inquiry.domain.Inquiry;
import com.jinmifood.jinmi.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
