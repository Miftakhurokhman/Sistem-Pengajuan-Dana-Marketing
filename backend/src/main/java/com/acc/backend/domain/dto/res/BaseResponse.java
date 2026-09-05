package com.acc.backend.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    @Builder.Default
    private String message = "Success";

    @Builder.Default
    private Integer status = 200;

    @Builder.Default
    private Boolean success = true;

    private T data;

    // Helper method untuk response sukses dengan message kustom
    public static <T> BaseResponse<T> ok(String message, T data) {
        return BaseResponse.<T>builder()
                .message(message)
                .status(200)
                .success(true)
                .data(data)
                .build();
    }

    // Helper method untuk response sukses default
    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .message("Success")
                .status(200)
                .success(true)
                .data(data)
                .build();
    }

    // Helper method untuk response error
    public static <T> BaseResponse<T> error(Integer status, String message) {
        return BaseResponse.<T>builder()
                .message(message)
                .status(status)
                .success(false)
                .data(null)
                .build();
    }
}