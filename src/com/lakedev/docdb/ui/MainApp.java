package com.lakedev.docdb.ui;

import com.lakedev.docdb.service.db.DataSource;
import com.lakedev.docdb.service.dmz.DMZManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application
{
	
	@Override
	public void init() throws Exception
	{
		if (
				DMZManager
				.getInstance() 
				.dmzExists() == false)
		{
			// TODO Alert the user, Log
			
			stop();
		} else if (
				
				DataSource
				.getInstance()
				.isConnected() == false)
		{
			// TODO Alert the user, Log
			
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
		primaryStage.setTitle("DataSaver");
		
		primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("PrimaryUI.fxml"))));
		
		primaryStage.show();
	}
	
	@Override
	public void stop() throws Exception
	{
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
