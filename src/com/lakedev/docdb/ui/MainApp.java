package com.lakedev.docdb.ui;

import org.pmw.tinylog.Logger;

import com.lakedev.docdb.service.db.DataSource;
import com.lakedev.docdb.service.dmz.DMZManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class MainApp extends Application
{
	
	@Override
	public void init() throws Exception
	{
		Logger.info("Initializing DocDb");
		
		if (
				DMZManager
				.getInstance() 
				.dmzExists() == false)
		{
			// Would log this but DMZManager logs its own failures.
			
			new Alert(
					AlertType.ERROR,
					"There was an issue creating a Doc DMZ. Shutting Down.",
					ButtonType.OK)
			.showAndWait();
			
			stop();
		} else if (
				
				DataSource
				.getInstance()
				.isConnected() == false)
		{
			// Would log this but DMZManager logs its own failures.
			
			new Alert(
					AlertType.ERROR,
					"There was an issue connecting to the Doc DB. Shutting Down.",
					ButtonType.OK)
			.showAndWait();
			
			stop();
		}
		
		super.init();
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Logger.debug("Creating UI");
		
		primaryStage.setTitle("DocDb");
		
		primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("PrimaryUI.fxml"))));
		
		primaryStage.show();
	}
	
	@Override
	public void stop() throws Exception
	{
		Logger.info("Shutting down DocDb.");
		
		if (
				DMZManager
				.getInstance()
				.dmzExists())
		{
			// TODO Determine what you want to do with the DMZ and documents in it.
		}
		
		if (
				DataSource
				.getInstance()
				.isConnected())
		{
			DataSource
			.getInstance()
			.closeConnection();
		}
		
		super.stop();
	}

}
