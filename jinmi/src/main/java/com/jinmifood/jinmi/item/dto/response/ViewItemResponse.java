package com.jinmifood.jinmi.item.dto.response;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.domain.ItemImage;
import com.jinmifood.jinmi.item.domain.ItemStatus;
import lombok.Getter;

import java.util.Optional; // Optional을 사용하기 위해 import

@Getter
public class ViewItemResponse {

    private Long itemId;
    private String itemName;
    private int itemPrice;
    private ItemStatus status;
    private int count;
    private String mainImageUrl;

    public ViewItemResponse(Item item) {
        this.itemId = item.getItemId();
        this.itemName = item.getItemName();
        this.itemPrice = item.getItemPrice();
        this.status = item.getStatus();
        this.count = item.getCount();


        Optional<String> firstMainImageUrl = item.getImages().stream()
                .filter(image -> image.getImageType() == ItemImage.ImageType.MAIN)
                .map(ItemImage::getImageUrl)
                .findFirst();

        this.mainImageUrl = firstMainImageUrl.orElse(null);
    }
}