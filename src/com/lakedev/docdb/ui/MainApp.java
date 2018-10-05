package com.lakedev.docdb.ui;

import com.lakedev.docdb.service.db.DataSource;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application
{

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
		DataSource
		.getInstance()
		.closeConnection();
		
		super.stop();
	}

}
