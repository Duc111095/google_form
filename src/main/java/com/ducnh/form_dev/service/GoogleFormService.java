package com.ducnh.form_dev.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.*;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpHeaders;

@Service
@ConditionalOnBean(name = "googleFormConfig")
public class GoogleFormService {

    @Autowired
    private Forms formService;

    @Autowired
    private Drive driveService;


    public String getAccessToken() throws IOException {
        GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(new ClassPathResource("original-mesh-428409-p7-a54614f92073.json").getFile())).createScoped(FormsScopes.all());
        return credential.getAccessToken() != null ? credential.getAccessToken().getTokenValue() : credential.refreshAccessToken().getTokenValue();
    }

    public Form getForm(String formId, String token) throws IOException, GeneralSecurityException {        
        return formService.forms().get(formId).setAccessToken(token).execute();
    }

    public String createNewForm(String token, String title) throws IOException, GeneralSecurityException {
        Form form = new Form();
        form.setInfo(new Info());
        form.getInfo().setTitle(title);
        form = formService.forms().create(form)
                .setAccessToken(token)
                .execute();
        return form.getFormId();
    }

    public static void setPermissionsToFile(Drive service, String fileId) throws IOException {
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                System.err.println(e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                System.out.println("Permission ID: " + permission.getId());
            }
        };
        BatchRequest batch = service.batch();
        Permission userPermission = new Permission()
                .setType("anyone")
                .setRole("reader")
                .setAllowFileDiscovery(false);
        service.permissions().create(fileId, userPermission)
                .setFields("id")
                .queue(batch, callback);

        batch.execute();
    }


    public void batchRequestUpdate(List<Request> requests, String formId, String token) throws IOException, GeneralSecurityException {
        Forms formService = this.formService;
        BatchUpdateFormRequest batchRequest = new BatchUpdateFormRequest();
        batchRequest.setRequests(requests);
        formService.forms().batchUpdate(formId, batchRequest)
                .setAccessToken(token).execute();
    }

    public Request updateDescriptionForm(String description, String formId, String token) throws IOException, GeneralSecurityException {
        Request request = new Request();

        UpdateFormInfoRequest formInfo = new UpdateFormInfoRequest();
        Info info = new Info();
        info.setDescription(description);
        formInfo.setInfo(info);
        formInfo.setUpdateMask("description");
        request.setUpdateFormInfo(formInfo);

        return request;
    }

    public Request addTextQuestion(String questionText, int questionPos, boolean required, boolean isParagraph, String formId, String token) throws IOException, GeneralSecurityException {
        Request request = new Request();

        Item item = new Item();
        item.setTitle(questionText);
        
        // Set up the question item
        item.setQuestionItem(new QuestionItem());
        Question question = new Question();
        question.setRequired(required);
        question.setTextQuestion(new TextQuestion());
        question.getTextQuestion().setParagraph(isParagraph);

        // Assign the question to the question item
        item.getQuestionItem().setQuestion(question);

        // Create the create item request
        request.setCreateItem(new CreateItemRequest());
        request.getCreateItem().setItem(item);
        request.getCreateItem().setLocation(new Location());
        request.getCreateItem().getLocation().setIndex(questionPos);

        return request;
    }

    public Request addImage(String title, String pathToImage, int position, String formId, String token) throws IOException, GeneralSecurityException {
        Request request = new Request();

        Item item = new Item();
        item.setTitle(title);

        // Create image item
        ImageItem imageItem = new ImageItem();
        Image image = new Image();
        image.setSourceUri(pathToImage);
        imageItem.setImage(image);
        item.setImageItem(imageItem);

        request.setCreateItem(new CreateItemRequest());
        request.getCreateItem().setItem(item);
        request.getCreateItem().setLocation(new Location());
        request.getCreateItem().getLocation().setIndex(position);
        
        return request;
    }

    public Request addRadioItem(
        String questionText,
        List<String> answers,
        String correctAnswer,
        Boolean isTextAnother,
        boolean isRequired,
        int questionPos,
        String questionType,
        String formId, String token) throws IOException, GeneralSecurityException {
        Request request = new Request();

        Item item = new Item();
        item.setTitle(questionText);

        item.setQuestionItem(new QuestionItem());
        Question question = new Question();
        question.setRequired(isRequired);
        question.setChoiceQuestion(new ChoiceQuestion());
        question.getChoiceQuestion().setType(questionType);
        List<Option> options = new ArrayList<>();
        
        for (String answer : answers) {
            Option option = new Option();
            option.setValue(answer);
            options.add(option);
        }

        if (isTextAnother) {
            Option option = new Option();
            option.setIsOther(isTextAnother);
            options.add(option);
        }

        question.getChoiceQuestion().setOptions(options);

        item.getQuestionItem().setQuestion(question);
        request.setCreateItem(new CreateItemRequest());
        request.getCreateItem().setItem(item);
        request.getCreateItem().setLocation(new Location());
        request.getCreateItem().getLocation().setIndex(questionPos);

        return request;
    }

    public Request addMultichoiceGrid(String questionText, List<String> rowLabels, int questionPos, boolean isRequired, int maxGrade, int minGrade, String questionType, String formId, String token) throws IOException, GeneralSecurityException  {
        Request request = new Request();
        
        Item item = new Item();
        item.setTitle(questionText);

        QuestionGroupItem questionGrid = new QuestionGroupItem();
        
        // Set Columns of Grid
        Grid grid = new Grid();
        ChoiceQuestion choiceQuestion = new ChoiceQuestion();
        choiceQuestion.setType(questionType);
        List<Option> options = new ArrayList<>();

        for (int i = maxGrade; i >= minGrade; i--) {
            Option option = new Option();
            option.setValue(String.valueOf(i));
            options.add(option);
        }
        choiceQuestion.setOptions(options);
        grid.setColumns(choiceQuestion);
        questionGrid.setGrid(grid);

        // Set row labels
        List<Question> questions = new ArrayList<>();
        for (String r : rowLabels) {
            Question question = new Question();
            question.setRequired(isRequired);
            RowQuestion rq = new RowQuestion();
            rq.setTitle(r);
            question.setRowQuestion(rq);
            questions.add(question);
        }
        questionGrid.setQuestions(questions);

        item.setQuestionGroupItem(questionGrid);
        request.setCreateItem(new CreateItemRequest());
        request.getCreateItem().setItem(item);
        request.getCreateItem().setLocation(new Location());
        request.getCreateItem().getLocation().setIndex(questionPos);

        return request;
    }

    public Request addScaleQuestion(String questionText, List<String> rowLabels, boolean isRequired, int questionPos, int minScale, int maxScale, String token, String formId) throws IOException, GeneralSecurityException {
        Request request = new Request();

        Item item = new Item();
        item.setTitle(questionText);

        item.setQuestionItem(new QuestionItem());

        // Create scale question item 
        Question question = new Question();
        question.setRequired(isRequired);
        ScaleQuestion scaleQuestion = new ScaleQuestion();
        scaleQuestion.setLow(minScale);
        scaleQuestion.setHigh(maxScale);
        scaleQuestion.setLowLabel(rowLabels.get(0));
        question.setScaleQuestion(scaleQuestion);

        // Assign the question to the question form
        item.getQuestionItem().setQuestion(question);

        // Create the create item request
        request.setCreateItem(new CreateItemRequest());
        request.getCreateItem().setItem(item);
        request.getCreateItem().setLocation(new Location());
        request.getCreateItem().getLocation().setIndex(questionPos);

        return request;
    }

    public void transformInQuiz(String formId, String token) throws IOException, GeneralSecurityException {
        BatchUpdateFormRequest batchRequest = new BatchUpdateFormRequest();
        Request request = new Request();
        FormSettings formSetting = new FormSettings();
        QuizSettings quizSettings = new QuizSettings();
        quizSettings.setIsQuiz(true);
        formSetting.setQuizSettings(quizSettings);
        UpdateSettingsRequest updateSettings = new UpdateSettingsRequest();
        updateSettings.setSettings(formSetting);
        updateSettings.setUpdateMask("quizSettings.isQuiz");
        request.setUpdateSettings(updateSettings);
        batchRequest.setRequests(Collections.singletonList(request));
        formService.forms().batchUpdate(formId, batchRequest)
            .setAccessToken(token).execute();
    }

    public boolean publishForm(String formId, String token) throws GeneralSecurityException, IOException {
        PermissionList list = driveService.permissions().list(formId).setOauthToken(token).execute();
        System.out.println(list);
        System.out.println(formId);
        if (!list.getPermissions().stream().filter((it) -> it.getRole().equals("writer")).findAny().isPresent()) {
            Permission body = new Permission();
            body.setRole("writer");
            body.setType("user");
            body.setEmailAddress("phongdaotaomitalabvn3@gmail.com");
            driveService.permissions().create(formId, body).setOauthToken(token).execute();
            return true;
        }
        return false;
    }
}
