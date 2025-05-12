package com.ducnh.form_dev.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducnh.form_dev.payload.HVResponsePayload;
import com.ducnh.form_dev.service.ResponseFormService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/")
@CrossOrigin(origins = "https://erp.lienketdachieu.com")
public class HVFormController {
    @Autowired
    private ResponseFormService responseFormService;

    @GetMapping("/form-hv")
    public ResponseEntity<HVResponsePayload> getHVResponse(@RequestParam String formId) {
        HVResponsePayload resPayload = new HVResponsePayload();
        resPayload.setCreatedAt(LocalDateTime.now());
        try {
            List<Map<String, Object>> data = responseFormService.rHVResponses(formId);
            resPayload.setData(data);
            resPayload.setMessage("success");
            return ResponseEntity.ok(resPayload);
        } catch (IOException | GeneralSecurityException | SQLException ex) {
            resPayload.setMessage("error");
            resPayload.setErrors(ex.getMessage());
            return ResponseEntity.internalServerError().body(resPayload);
        }
    }   
}
