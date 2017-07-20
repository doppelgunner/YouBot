package com.doppelgunner.youbot.controller;

import javafx.fxml.FXML;
import javafx.stage.*;

public abstract class Controller {
	
	protected Stage stage;
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	//sending data from outside to the controller
	public void send(Object data) {
		//do nothing - override to do something
	}

	@FXML
	protected void initialize() {
		System.out.println("INIT: " + getClass().getSimpleName());
	}

	public void dispose() {
	}
}