package com.ducnh.form_dev.payload;

import lombok.Data;

@Data
public class GVRequestPayload {
    private String tenLop;
    private String giangVien;
    private String startDate;
    private String formId;
}
