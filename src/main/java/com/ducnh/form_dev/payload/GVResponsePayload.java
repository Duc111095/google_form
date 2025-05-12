package com.ducnh.form_dev.payload;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class GVResponsePayload {
    private String message;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
    private String errors;
}
