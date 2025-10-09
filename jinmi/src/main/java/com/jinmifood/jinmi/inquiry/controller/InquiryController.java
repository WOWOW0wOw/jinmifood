package com.jinmifood.jinmi.inquiry.controller;

import com.jinmifood.jinmi.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

}
