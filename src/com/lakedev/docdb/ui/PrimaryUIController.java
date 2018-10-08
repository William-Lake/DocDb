package com.lakedev.docdb.ui;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.lakedev.docdb.service.db.DataSource;
import com.lakedev.docdb.service.db.Doc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;

public class PrimaryUIController implements Initializable
{

	@FXML
	private TextField txtSearch;
	
	@FXML
	private TableView<Doc> tvDb;
	
	@FXML
	private TableColumn<Doc,String> tcDbDocName;
	
	@FXML
	private TableColumn<Doc,String> tcDbDocDesc;
	
	@FXML
	private TableColumn<Doc,LocalDate> tcDbDocAddDt;
	
	@FXML
	private TableColumn<Doc,LocalDate> tcDbDocModDt;
	
	@FXML
	private TableView<Doc> tvDmz;
	
	@FXML
	private TableColumn<Doc,String> tcDmzDocName;
	
	@FXML
	private TableColumn<Doc,String> tcDmzDocDesc;
	
	@FXML 
	private TableColumn<Doc,LocalDate> tcDmzDocAddDt;
	
	@FXML
	private TableColumn<Doc,LocalDate> tcDmzDocModDt;
	
	private ContextMenu cmDb;
	
	private ContextMenu cmDmz;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		buildTables();
		
		buildContextMenus();
	}
	
	private void buildTables()
	{
		PropertyValueFactory<Doc, String> docNamePVF = new PropertyValueFactory<>("name");
		
		PropertyValueFactory<Doc, String> docDescPVF = new PropertyValueFactory<>("description");
		
		PropertyValueFactory<Doc, LocalDate> docAddDtPVF = new PropertyValueFactory<>("addDate");
		
		PropertyValueFactory<Doc, LocalDate> docModDtPVF = new PropertyValueFactory<>("modDate");
		
		tcDbDocName.setCellValueFactory(docNamePVF);
		
		tcDmzDocName.setCellValueFactory(docNamePVF);
		
		tcDbDocDesc.setCellValueFactory(docDescPVF);
		
		tcDmzDocDesc.setCellValueFactory(docDescPVF);
		
		tcDbDocAddDt.setCellValueFactory(docAddDtPVF);
		
		tcDmzDocAddDt.setCellValueFactory(docAddDtPVF);
		
		tcDbDocModDt.setCellValueFactory(docModDtPVF);
		
		tcDmzDocModDt.setCellValueFactory(docModDtPVF);
	}
	
	private void buildContextMenus()
	{
		cmDb = new ContextMenu();
		
		cmDmz = new ContextMenu();
		
		MenuItem dbToDmz = new MenuItem("Move to DMZ");
		
		MenuItem dbDelete = new MenuItem("Delete from DB");
		
		MenuItem dmzImport = new MenuItem("Import to DB");
		
		MenuItem dmzDelete = new MenuItem("Delete from DMZ");
		
		dbToDmz.setOnAction((selected) -> 
		{
			Doc selectedDoc = tvDb.getSelectionModel().getSelectedItem();
			
			
		});
		
		dbDelete.setOnAction((selected) -> 
		{
			Doc selectedDoc = tvDb.getSelectionModel().getSelectedItem();
		});
		
		dmzImport.setOnAction((selected) -> 
		{
			Doc selectedDoc = tvDmz.getSelectionModel().getSelectedItem();
		});
		
		dmzDelete.setOnAction((selected) -> 
		{
			Doc selectedDoc = tvDmz.getSelectionModel().getSelectedItem();
		});
	}
	
	@FXML
	public void updateDbTable()
	{
		String searchText = txtSearch.getText().trim();
		
		List<Doc> matchingDocs = 
				
				searchText.isEmpty() ?
						
						DataSource
						.getInstance()
						.gatherAllDocs() :
							
							DataSource
							.getInstance()
							.gatherDocsByNameOrDescription(searchText);
		
		tvDb.getItems().clear();
		
		tvDb.getItems().addAll(matchingDocs);
	}
	
	@FXML
	public void displayDmzContextMenu(ContextMenuEvent event)
	{
		
	}

	@FXML
	public void displayDbContextMenu(ContextMenuEvent event)
	{
		
	}

}
