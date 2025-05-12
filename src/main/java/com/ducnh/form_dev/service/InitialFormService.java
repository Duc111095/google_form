package com.ducnh.form_dev.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.google.api.services.forms.v1.model.Request;

@Service
@ConditionalOnBean(name = "googleFormService")
public class InitialFormService {
    
    @Autowired
    private GoogleFormService googleFormService;

    public String initialGVForm(String title, String tenGV, String startDate, String formId) throws IOException, GeneralSecurityException {
        // Buider batch Request
        String formTitle = "[MN] Thư mời phản hồi lớp \"" + title +"\" - Giảng viên: " + tenGV + " - " + startDate;
        String formDecription = "Trân trọng cảm ơn Anh/Chị đã tham dự lớp đào tạo do công ty tổ chức.\r\n" + //
                        "Để hoàn thành đào tạo, Anh/Chị vui lòng dành thời gian 2-5 phút để phản hồi, đóng góp nâng cao chất lượng đào tạo. \r\n" + //
                        "Mọi thông tin Anh/Chị cung cấp sẽ được BẢO MẬT. Phòng Đào tạo chỉ sử dụng thông tin cho mục đích nâng cao chất lượng đào tạo.\r\n" + //
                        "Rất mong nhận được sự đóng góp ý kiến của Anh/Chị!";
        String endTitle = "Mỗi phản hồi của Anh/Chị sẽ góp phần nâng cao chất lượng các chương trình đào tạo của Mitalab! " + 
                        "Trân trọng cảm ơn!";
        String pathToImage = "https://drive.google.com/uc?export=view&id=1oEVqtFddg-spFi_YHKJnhNYp7Lp5paWx";
        String token = googleFormService.getAccessToken();
        // String formId = "12_M7gf2MBV4mSSxYAIBcEws_RiBzXAANihCxUyUQUVE";
        
        if (formId == "") {
            formId = googleFormService.createNewForm(token, formTitle);
        }

        System.out.println(googleFormService.publishForm(formId, token));

        List<Request> requests = new ArrayList<>();
        
        requests.add(googleFormService.updateDescriptionForm(formDecription, formId, token));

        requests.add(googleFormService.addRadioItem("1. Anh/Chị đã từng tham dự nội dung đào tạo này trước đây chưa?", List.of(
            "Chưa. Tôi chưa bao giờ tham dự",
            "Có. Tôi đã từng tham dự (Ghi rõ khoảng thời gian đã từng tham dự)"
        ), "", true, true, 0, "RADIO", formId, token));

        requests.add(googleFormService.addRadioItem("2. Lý do khiến Anh/Chị tham dự lớp đào tạo này? (Có thể chọn nhiều hơn một đáp án)", List.of(
            "Yêu cầu của Phòng Đào tạo",
            "Quản lý của tôi đề xuất",
            "Chủ đề mà tôi quan tâm"
        ), "", true, true, 1, "CHECKBOX", formId, token));

        requests.add(googleFormService.addMultichoiceGrid("3. Đánh giá về Khâu tổ chức lớp đào tạo (Theo thang điểm từ 1 đến 10)", List.of(
            "Thông tin hướng dẫn về lớp đào tạo rõ ràng, cụ thể",
            "Tài liệu tham khảo đầy đủ, nội dung hữu ích",
            "Môi trường đào tạo phù hợp, có đầy đủ các trang thiết bị "
        ), 2, true, 10, 1, "RADIO", formId, token));
        
        requests.add(googleFormService.addMultichoiceGrid("4. Đánh giá về Kỹ năng giảng dạy của giảng viên (Theo thang điểm từ 1 đến 10)", List.of(
            "Kiến thức chuyên môn tốt đối với chủ đề đang chia sẻ",
            "Slide trình chiếu đầy đủ thông tin, thiết kế chuyên nghiệp",
            "Kỹ năng thuyết trình lôi cuốn",
            "Áp dụng các phương pháp giảng dạy tích cực"
        ), 3, true, 10, 1, "RADIO", formId, token));

        requests.add(googleFormService.addMultichoiceGrid("5. Đánh giá về tính hữu ích của nội dung đào tạo? (Theo thang điểm từ 1 đến 10)", List.of(
            "Cung cấp kiến thức cần thiết, hữu ích cho công việc",
            "Khả năng áp dụng cao trong công việc thực tế"
        ), 4, true, 10, 1, "RADIO", formId, token));

        requests.add(googleFormService.addTextQuestion("6. Anh/Chị THÍCH điều gì trong lớp đào tạo này?", 5, true, false, formId, token));
        
        requests.add(googleFormService.addTextQuestion("7. Anh/Chị KHÔNG THÍCH điều gì trong lớp đào tạo này?", 6, true, false, formId, token));
        
        requests.add(googleFormService.addTextQuestion("8. Anh/Chị có ĐỀ XUẤT gì để nâng cao chất lượng lớp đào tạo không?", 7, true, false, formId, token));
        
        requests.add(googleFormService.addImage(endTitle, pathToImage, 8, formId, token));

        googleFormService.batchRequestUpdate(requests, formId, token);
        return formId;
    }
}
