package com.ducnh.form_dev.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class HVResponsePayload {
    private String message;
    private List<Map<String, Object>> data;
    private LocalDateTime createdAt;
    private String errors;
}
