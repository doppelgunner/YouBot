package com.doppelgunner.youbot.controller;

import com.doppelgunner.youbot.YouBot;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

import java.io.IOException;

/**
 * Created by robertoguazon on 19/07/2017.
 */
public class LoginWebController extends Controller {

    @FXML private WebView loginWebView;
    @FXML private Label statusLabel;

    private LocalServerReceiver lsr;
    private Button loginButton;

    @Override
    @FXML protected void initialize() {
        loginWebView.getEngine().getLoadWorker().stateProperty().addListener((o,ov,nv) -> {
            switch (nv) {
                case RUNNING:
                    statusLabel.setText("LOADING...");
                    break;
                case SUCCEEDED:
                    statusLabel.setText("SUCCESS");
                    break;
                case CANCELLED:
                case FAILED:
                    statusLabel.setText("FAILED");
                    break;
            }
        });

        //this is important! needed to login to google
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    public void openURL(String url) {
        loginWebView.getEngine().load(url);
    }

    public void setLocalReceiver(LocalServerReceiver lsr) {
        this.lsr = lsr;
    }

    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    @Override
    public void dispose() {
        try {
            lsr.stop();

            MainController main = (MainController)YouBot.getMainController();
            HeaderController header = (HeaderController) main.getHeaderController();
            header.getLogButton().setDisable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
