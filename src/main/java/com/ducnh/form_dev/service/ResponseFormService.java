package com.ducnh.form_dev.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.ducnh.form_dev.entity.Staff;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.ListFormResponsesResponse;
import com.google.api.services.forms.v1.model.Question;
import com.google.auth.oauth2.GoogleCredentials;

@Service
@ConditionalOnBean(name = "googleFormConfig")
public class ResponseFormService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Forms formService;

    public ResponseFormService(Forms formService) {
        this.formService = formService;
       
    }

    private String getAccessToken() throws IOException{
        GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(new ClassPathResource("original-mesh-428409-p7-a54614f92073.json").getFile())).createScoped(FormsScopes.all());
        return credential.getAccessToken() != null ? credential.getAccessToken().getTokenValue() : credential.refreshAccessToken().getTokenValue();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> rGVResponses(String formId) throws IOException, GeneralSecurityException {
        String accessToken = this.getAccessToken();
        ListFormResponsesResponse response = formService.forms().responses().list(formId).setOauthToken(accessToken).execute();
        Form form = formService.forms().get(formId).setOauthToken(accessToken).execute();
        Map<String, Object> responsesMap = new HashMap<>(); // map[id] -> kết quả
        Map<String, String> questionsMap = new HashMap<>(); // map[câu hỏi] -> id 
        form.getItems().forEach(item -> {
            String title = item.getTitle();
            if (item.getQuestionGroupItem() != null) {
                for (Question q : item.getQuestionGroupItem().getQuestions()) {
                    questionsMap.put(q.getRowQuestion().getTitle(), q.getQuestionId());
                }
            } else if (item.getQuestionItem() != null ){
                Question q = item.getQuestionItem().getQuestion();
                questionsMap.put(title, q.getQuestionId());
            } else {
                questionsMap.put(title, item.getItemId());
            }
        });

        response.getResponses().forEach(res -> {
            res.getAnswers().values().forEach(ans -> {
                String questionId = ans.getQuestionId();
                List<Object> answers = new ArrayList<>();
                ans.getTextAnswers().getAnswers().forEach(textAns -> {
                    answers.add(textAns.getValue());
                });
                if (responsesMap.containsKey(questionId)) {
                    ((List<Object>)responsesMap.get(questionId)).addAll(answers);
                }  else {
                    List<Object> ansList = new ArrayList<>();
                    ansList.addAll(answers);
                    responsesMap.put(questionId, ansList);
                } 
            });
        });
        Map<String, Object> result = new HashMap<>();

        questionsMap.forEach((k, v) -> {
            result.put(k, responsesMap.get(v));
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> rHVResponses(String formId) throws IOException, GeneralSecurityException, SQLException {
        // List Kết quả từng học viên - theo câu hỏi 
        String accessToken = this.getAccessToken();
        ListFormResponsesResponse response = formService.forms().responses().list(formId).setOauthToken(accessToken).execute();
        Form form = formService.forms().get(formId).setOauthToken(accessToken).execute();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> questionsMap = new HashMap<>(); // map[câu hỏi] -> id 
        form.getItems().forEach(item -> {
            String title = item.getTitle();
            if (item.getQuestionGroupItem() != null) {
                for (Question q : item.getQuestionGroupItem().getQuestions()) {
                    questionsMap.put( q.getQuestionId(),q.getRowQuestion().getTitle());
                }
            } else if (item.getQuestionItem() != null ){
                Question q = item.getQuestionItem().getQuestion();
                questionsMap.put(q.getQuestionId(),title);
            } else {
                questionsMap.put(item.getItemId(), title);
            }
        });
        
        response.getResponses().forEach(res -> {
            res.getAnswers().values().forEach(ans -> {
                Map<String, Object> responsesMap = new HashMap<>(); // map[id] -> kết quả
                String questionId = ans.getQuestionId();
                String questionText = questionsMap.get(questionId);
                List<Object> answers = new ArrayList<>();
                ans.getTextAnswers().getAnswers().forEach(textAns -> {
                    answers.add(textAns.getValue());
                });
                responsesMap.put(questionText, answers);
                responsesMap.put("totalScore", (double) res.getOrDefault("totalScore", 0.0));
                result.add(responsesMap);
            });
        });

        result.forEach(hv -> {
            for (String k : questionsMap.values()) {
                if (k.indexOf("Mã nhân viên") >= 0) {
                    if (hv.get(k) != null) {
                        String maNv = ((List<String>) hv.get(k)).get(0);
                        Staff staff = queryInformationHV(maNv).get(0);
                        hv.put("ma_nv", maNv);
                        hv.put("stt_rec_nv", staff.getSttRec() == null ? "" : staff.getSttRec());
                        hv.put("ten", staff.getTen() == null ? "" : staff.getTen());
                        hv.put("ten_bp", staff.getTenBP() == null ? "" : staff.getTenBP());
                        hv.put("ten_bac", staff.getTenBac() == null ? "" : staff.getTenBac());
                    }
                }
            }                
        });
         
        return result;
    }

    private List<Staff> queryInformationHV(String maNv) {
        String sql ="if exists(select 1 from hrnv where ma_nv = '" + maNv + "') begin\r\n" + //
                    "  select stt_rec , ten, ten_bp, ten_bac\r\n" + //
                    "    from vhrnv where ma_nv = '" + maNv + "'\r\n" + //
                    " end ";
        
        try {
            return jdbcTemplate.query(sql, new RowMapper<Staff>() {
                public Staff mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Staff staff = new Staff();
                    staff.setMaNv(maNv);
                    staff.setSttRec(rs.getString("stt_rec"));
                    staff.setTen(rs.getString("ten"));
                    staff.setTenBP(rs.getString("ten_bp"));
                    staff.setTenBac(rs.getString("ten_bac"));
                    return staff;
                }
            });
        } catch (Exception e) {
            Staff staff = new Staff();
            staff.setMaNv(maNv);
            return Arrays.asList(staff);
        }
    }
}
