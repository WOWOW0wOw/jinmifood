package com.jinmifood.jinmi.itemCart.controller;

//import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
//import com.jinmifood.jinmi.itemCart.dto.response.ViewItemCartResponse;
//import com.jinmifood.jinmi.itemCart.service.ItemCartService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/itemCart")
//public class ItemCartController {
//
//    private final ItemCartService itemCartService;
//
//    @GetMapping({""})
//    public StatusResponseDTO list(@PathVariable Long userId) {
//        List<ViewItemCartResponse> list =  itemCartService.list(userId);
//        return  StatusResponseDTO.ok(list);
//    }
//
//}
