package com.jinmifood.jinmi.common.statusResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponseDTO {

    private final Integer status = 200;
    private final String message = "OK";
    private Object data;

    public static StatusResponseDTO ok() {
        return new StatusResponseDTO();
    }

    public static StatusResponseDTO ok(Object data) {
        return new StatusResponseDTO(data);
    }

    public static StatusResponseDTO ok(List<Object> data) {
        return new StatusResponseDTO(data);
    }

    public static StatusResponseDTO ok(Map<Object,Object> data) {
        return new StatusResponseDTO(data);
    }

    public static StatusResponseDTO ok(Set<Object> data) {
        return new StatusResponseDTO(data);
    }

}
