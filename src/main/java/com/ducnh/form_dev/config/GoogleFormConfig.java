package com.ducnh.form_dev.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ducnh.form_dev.FormProjectApplication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.forms.v1.Forms;

@Configuration
public class GoogleFormConfig {

    private static final String APPLICATION_NAME = "google-form-api-project";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static HttpTransport HTTP_TRANSPORT;

    private static FileDataStoreFactory DATA_STORE_FACTORY;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Bean   
    public Drive getDriveService() throws IOException, GeneralSecurityException{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
        Drive driveService = new Drive.Builder(httpTransport, gsonFactory, null).setApplicationName(APPLICATION_NAME).build();
        return driveService;
    }

    @Bean
    public Forms getFormService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
        Forms formService = new Forms.Builder(httpTransport, gsonFactory, null).setApplicationName(APPLICATION_NAME).build();
        return formService;
    }  
    
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =  FormProjectApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        System.out.println(clientSecrets.getDetails());

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                                                .setDataStoreFactory(DATA_STORE_FACTORY)
                                                .setAccessType("offline")
                                                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        System.out.println("Credentials saved to " + TOKENS_DIRECTORY_PATH);
        return credential;
    }
}
