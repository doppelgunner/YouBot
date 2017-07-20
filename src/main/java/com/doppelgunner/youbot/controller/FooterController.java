package com.doppelgunner.youbot.controller;

import com.doppelgunner.youbot.Util;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;

/**
 * Created by robertoguazon on 19/07/2017.
 */
public class FooterController extends Controller {

    @FXML private Button paypalButton;
    @FXML private Button patreonButton;
    @FXML private Hyperlink glyphiconsLink;

    @FXML
    protected void initialize() {
        ImageView paypalView = Util.makeImageView("/images/paypal.png",24,24);
        ImageView patreonView = Util.makeImageView("/images/patreon.png",24,24);

        paypalButton.setGraphic(paypalView);
        patreonButton.setGraphic(patreonView);
    }

    @FXML
    private void goPaypal() {
        Util.goLink("https://www.paypal.me/doppelgunner");
    }

    @FXML
    private void goPatreon() {
        Util.goLink("https://www.patreon.com/doppelgunner");
    }

    @FXML void goGlyphicons() {
        Util.goLink("http://glyphicons.com/");
    }
}
