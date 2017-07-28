package com.doppelgunner.youbot;

import com.doppelgunner.youbot.controller.Controller;
import com.doppelgunner.youbot.controller.LoginWebController;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.youtube.YouTube;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by robertoguazon on 15/07/2017.
 */
public class YouBot extends Application {

    public static int WIDTH = 640;
    public static int HEIGHT = 600;

    private static Stage primaryStage;
    private static Stage loginStage;

    private static YouTube youtubeDefault;
    private static YouTube youtubeAuth;
    private static Credential credential;
    private static Auth auth;
    private static Oauth2 oauth2;
    private static Userinfoplus userProfile;
    private static Image image;
    private static String username;
    private static Image defaultImage;

    private static Scene mainScene;
    private static Controller mainController;
    private static Controller loginWebController;
    private static BooleanProperty loggedIn = new SimpleBooleanProperty(false);

    public static Image getDefaultImage() {
        return defaultImage;
    }

    public static boolean isLoggedIn() {
        return loggedIn.get();
    }

    public static String getUsername() {
        return username;
    }

    public static Image getImage() {
        return image;
    }

    public static Oauth2 getOauth2() {
        return oauth2;
    }

    public static Userinfoplus getUserProfile() {
        return userProfile;
    }

    public static Credential getCredential() {
        return credential;
    }

    public static Auth getAuth() {
        return auth;
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static Controller getMainController() {
        return mainController;
    }

    public static YouTube getYoutubeDefault() {
        return youtubeDefault;
    }

    public static BooleanProperty getLoggedInProperty() {
        return loggedIn;
    }

    public static YouTube getYoutubeAuth() {
        return youtubeAuth;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Stage getLoginStage() {
        return loginStage;
    }

    public static Controller getLoginWebController() {
        return loginWebController;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        beforeStart();
        this.primaryStage = primaryStage;
        primaryStage.setTitle("YouBot - YouTube Commenter");

        FXMLLoader loader = createLoader("/fxml/Main.fxml");
        Parent mainRoot = loader.load();
        mainController = loader.getController();

        loader = createLoader("/fxml/LoginWeb.fxml");
        Parent loginWebRoot = loader.load();
        loginWebController = loader.getController();
        loginStage = new Stage(StageStyle.DECORATED);
        loginStage.initOwner(primaryStage);
        loginStage.setTitle("Login: (Google)");
        loginStage.setScene(new Scene(loginWebRoot));
        loginStage.setOnHidden(e -> loginWebController.dispose());
        loginStage.setResizable(false);
        loginStage.getIcons().add(new Image("/images/icon.png"));



        mainScene = new Scene(mainRoot);

        Util.setDefaultStylesheet(mainScene);
        Util.setDefaultStylesheet(loginStage.getScene());

        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.getIcons().add(new Image("/images/icon.png"));
        setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private void beforeStart() {
        youtubeDefault = Util.getYouTubeDefault();
        defaultImage = new Image("/images/unknown.gif");
    }

    public void setScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    public static FXMLLoader createLoader(String filepath) {
        URL url = YouBot.class.getResource(filepath);
        FXMLLoader loader = new FXMLLoader(url);
        return loader;
    }

    public static boolean login() {
        auth = Util.getAuth();
        credential = Util.getCredential(auth);
        if (credential == null) return false;

        youtubeAuth = Util.getYouTubeAuthentication(credential);
        oauth2 = Util.getOauth2Authentication(credential);

        userProfile = Util.getUserProfile(oauth2);
        image = new Image(userProfile.getPicture());
        username = userProfile.getName();

        loggedIn.set(true);
        return isLoggedIn();
    }

    public static boolean logout() {
        try {
            Util.revokeAccess(credential.getAccessToken());
            auth.getDataStore().clear();
            loggedIn.set(false);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void stop() {
        if (credential == null || !isLoggedIn()) return;
        Util.runBackground(
                () -> logout(),
                null, false);
    }
}
