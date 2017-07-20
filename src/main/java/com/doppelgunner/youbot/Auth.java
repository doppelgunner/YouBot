package com.doppelgunner.youbot;

import com.doppelgunner.youbot.controller.LoginWebController;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

/**
 * Shared class used by every sample. Contains methods for authorizing a user and caching credentials.
 */
public class Auth {

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

    private DataStore<StoredCredential> datastore;
    private List<String> scopes;
    private String credentialDataStore;
    private GoogleAuthorizationCodeFlow flow;
    private LocalServerReceiver localReceiver;

    public Auth(List<String> scopes, String credentialDataStore) throws IOException {
        this.scopes = scopes;
        this.credentialDataStore = credentialDataStore;

        // Load client secrets.
        Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream("/client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
        datastore = fileDataStoreFactory.getDataStore(credentialDataStore);

        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
                .build();
    }

    public Credential authorize() throws IOException {

        Credential credential = null;
        try {
            // Build the local server and bind it to port 8080
            localReceiver = new LocalServerReceiver.Builder().setPort(54321).build();
            String redirectUri = localReceiver.getRedirectUri();
            AuthorizationCodeRequestUrl authorizationURL = flow.newAuthorizationUrl();
            authorizationURL.setRedirectUri(redirectUri);
            String url = authorizationURL.build();
            //Desktop.getDesktop().browse(new URI(url));

            //create stage for log in then open url
            Platform.runLater(() -> {
                Stage loginStage = YouBot.getLoginStage();
                LoginWebController loginWebController = (LoginWebController)YouBot.getLoginWebController();
                loginWebController.setLocalReceiver(localReceiver);
                loginWebController.openURL(url);
                loginStage.show();
            });
            //wait for code after

            String code = localReceiver.waitForCode();
            Platform.runLater(() -> {
                YouBot.getLoginStage().close();
            });
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            credential = flow.createAndStoreCredential(response,"user");
        } finally {
            localReceiver.stop();
            return credential;
        }
    }

    //https://stackoverflow.com/questions/22763934/google-oauth-fails-fetching-redirect-url
    public DataStore getDataStore() {
        return datastore;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getCredentialDataStore() {
        return credentialDataStore;
    }
}