package com.ducnh.form_dev.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducnh.form_dev.payload.GVResponsePayload;
import com.ducnh.form_dev.service.DatabaseService;
import com.ducnh.form_dev.service.InitialFormService;
import com.ducnh.form_dev.service.ResponseFormService;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "https://mrc.mitalab.com")
public class GVFormController {

    @Autowired
    private InitialFormService initialFormService;

    @Autowired
    private ResponseFormService responseFormService;

    @Autowired
    private DatabaseService databaseService;

    @PostMapping(path="/form-gv", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GVResponsePayload> createFormGV(@RequestBody Map<String, String> formData) {
        GVResponsePayload sResponsePayload = new GVResponsePayload();
        sResponsePayload.setCreatedAt(LocalDateTime.now());
        Map<String, Object> data = new HashMap<>();

        try {
            String title = formData.get("ten_lop");
            String tenGV = formData.get("giang_vien");
            String startDate = formData.get("start_date");
            String formId = formData.get("form_id");
            String listTenGV = "";
            List<String> listGV = Arrays.stream(tenGV.split(", ")).map(s -> databaseService.getTenGVFromMaGV(s.trim())).collect(Collectors.toList());
            for (String gv : listGV) {
                listTenGV += (listTenGV == "" ? gv : ", " + gv);
            } 
            formId = initialFormService.initialGVForm(title, listTenGV, startDate, formId);
            sResponsePayload.setMessage("success");
            data.put("form_id", formId);
            sResponsePayload.setData(data);
            return ResponseEntity.ok(sResponsePayload);
        } catch (IOException | GeneralSecurityException ex) {
            String errors = ex.getMessage();
            sResponsePayload.setMessage("error");
            sResponsePayload.setErrors(errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sResponsePayload);
        }   
    }
    
    @GetMapping("/form-gv")
    public ResponseEntity<GVResponsePayload> getGVResults(@RequestParam String formId) {
        GVResponsePayload sResponsePayload = new GVResponsePayload();
        sResponsePayload.setCreatedAt(LocalDateTime.now());
        Map<String, Object> data = new HashMap<>();
        try {
            data = responseFormService.rGVResponses(formId);
            sResponsePayload.setMessage("success");
            data.put("form_id", formId);
            sResponsePayload.setData(data);
            return ResponseEntity.ok(sResponsePayload);
        } catch (IOException | GeneralSecurityException ex) {
            String errors = ex.getMessage();
            sResponsePayload.setMessage("error");
            sResponsePayload.setErrors(errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sResponsePayload);
        }
    }
}
