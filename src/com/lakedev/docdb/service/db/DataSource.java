package com.lakedev.docdb.service.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.lakedev.docdb.service.dmz.DMZManager;

public class DataSource
{
	private DbConnection dbConnection;
	
	private boolean connected;
	
	private static final DateTimeFormatter DOC_DATE_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss");
	
	private static final class Wrapper
	{
		private static DataSource INSTANCE = new DataSource();
	}
	
	private DataSource()
	{
		Logger.info("Initializing DataSource");
		
		// TODO It would be useful to allow the user to connect to different databases, or identify what database they want to connect to.
		
		dbConnection = new DbConnection();
		
		boolean doContinue = true;
		
		if (
				Files
				.exists(
						Paths
						.get(DbConnection.DB_PATH)) == false)
		{
			doContinue = createDb();
		}
		
		if (doContinue)
		{
			if (tablesExist() == false) doContinue = createTables();
			
			if (doContinue)
			{
				connected = dbConnection.connect();
			}
		}
	}
	
	public static DataSource getInstance()
	{
		return Wrapper.INSTANCE;
	}
	
	private boolean createDb()
	{
		Logger.debug("Creating Database");
		
		boolean databaseCreated = false;
		
		try
		{
			Files.createFile(Paths.get(DbConnection.DB_PATH));
			
			databaseCreated = true;
			
			Logger.debug("Database successfully created.");
			
		} catch (IOException e)
		{
			Logger.error("Error while creating Database: \n" + e);
			
			Logger.trace(e);
		}
		
		return databaseCreated;
	}
	
	private boolean tablesExist()
	{
		Logger.debug("Checking if tables exist");
		
		boolean tablesExist = false;
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("SELECT COUNT(*) \"TABLE_COUNT\" ")
				.append("FROM sqlite_master ")
				.append("WHERE type = 'table' ")
				.append("AND name IN (?) ");
		
		PreparedStatement preparedStatement;
		
		try
		{
			preparedStatement = dbConnection.createPreparedStatement(query.toString());
			
			for (int i = 0;i<DbConnection.TABLE_NAMES.length;i++)
			{
				preparedStatement.setString(i + 1, DbConnection.TABLE_NAMES[i]);
			}
			
			ResultSet resultSet = dbConnection.executeSelect(preparedStatement);
			
			if (resultSet.next())
			{
				int tableCount = resultSet.getInt("TABLE_COUNT");
				
				tablesExist = tableCount == DbConnection.TABLE_NAMES.length;
				
				Logger.debug(String.format("%d of the required %d tables exist", tableCount, DbConnection.TABLE_NAMES.length));
				
				if (0 < tableCount && tableCount < DbConnection.TABLE_NAMES.length)
				{
					Logger.error("There are some tables, but not the right amount.");
					
					// Partial database.
					// TODO Alert user that there's something seriously wrong with the DB and they need to check it out.
					// TODO Log this, exit.
					// TODO May want to give them a chance to decide whether or not to create the DB fresh from scratch.
					//			If so, that will have to be handled from the DB and not here.
				}
			}
			
		} catch (SQLException e)
		{
			Logger.error("Error while checking if Tables Exist: \n" + e);
			
			Logger.trace(e);
		}
		
		return tablesExist;
	}
	
	private boolean createTables()
	{
		Logger.debug("Creating tables");
		
		boolean tablesCreated = false;
		
		try
		{
			String createTableScript = 
					
					String
					.join(" \n", 
							
							Files
							.readAllLines(
									
									Paths
									.get(
											DataSource
											.class
											.getResource("Create_Tables.sql")
											.toURI()), 
									Charset.defaultCharset()));
			
			dbConnection.executeStatement(createTableScript);
			
			Logger.debug("Tables successfully created.");
			
		} catch (URISyntaxException | IOException | SQLException e)
		{
			
			Logger.error("Error while trying to create tables: \n" + e);
			
			Logger.trace(e);
		}
		
		return tablesCreated;
	}
	
	public List<Doc> gatherAllDocs()
	{
		Logger.debug("Gathering all Docs");
		
		List<Doc> docs = new ArrayList<>();
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("SELECT * ")
				.append("FROM doc ");
		
		try(ResultSet resultSet = dbConnection.executeSelect(query.toString()))
		{
			while (resultSet.next())
			{
				int id = resultSet.getInt("id");
				
				String name = resultSet.getString("name");
				
				String description = resultSet.getString("description");
				
				LocalDate addDate = LocalDate.parse(resultSet.getString("add_date"), DOC_DATE_FORMATTER);
				
				LocalDate modDate = LocalDate.parse(resultSet.getString("mod date"), DOC_DATE_FORMATTER);
				
				docs.add(new Doc(id,name,description,addDate,modDate));
			}
			
			Logger.debug(String.format("%d docs gathered.", docs.size()));
		} catch (SQLException e)
		{
			
			Logger.error("Error while trying to gather docs: \n" + e);
			
			Logger.trace(e);
		} 
		
		return docs;
	}
	
	public List<Doc> searchForDocs(String searchValue)
	{
		Logger.debug("Searching for docs with param: " + searchValue);
		
		List<Doc> docs = new ArrayList<>();
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("SELECT * ")
				.append("FROM doc ")
				.append("WHERE title LIKE '%?%' ")
				.append("OR description LIKE '%?%' ");
		
		ResultSet results = null;
		
		try(PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString()))
		{
			preparedStatement.setString(1, searchValue);
			
			preparedStatement.setString(2, searchValue);
			
			results = dbConnection.executeSelect(preparedStatement);
			
			while (results.next())
			{
				int id = results.getInt("id");
				
				String name = results.getString("name");
				
				String description = results.getString("description");
				
				LocalDate addDate = LocalDate.parse(results.getString("add_date"), DOC_DATE_FORMATTER);
				
				LocalDate modDate = LocalDate.parse(results.getString("mod date"), DOC_DATE_FORMATTER);
				
				docs.add(new Doc(id,name,description,addDate,modDate));
			}
		} catch (SQLException e)
		{
			
			Logger.error("Error while trying to search for Doc: \n" + e);
			
			Logger.trace(e);
		}
		
		return docs;
	}
	
	public void addDoc(Doc doc)
	{
		Logger.debug("Adding " + doc.getName());
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("INSERT INTO doc ")
				.append("(name,description,data) ")
				.append("VALUES ")
				.append("(?,?,?) ");
							
		try
		{
			PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString());
			
			preparedStatement.setString(1, doc.getName());
			
			preparedStatement.setString(2, doc.getDescription());
			
			byte[] docData = 
					
					DMZManager
					.getInstance()
					.getDocData(doc);
			
			preparedStatement.setBytes(3, docData);
			
			dbConnection.executeStatement(preparedStatement);
			
			Logger.debug(doc.getName() + " successfully added");
			
		} catch (SQLException e)
		{
			Logger.error("Error while trying to add Doc: \n" + e);
			
			Logger.trace(e);
		}
	}
	
	public void updateDoc(Doc doc)
	{
		Logger.debug("Updating " + doc.getName());
		
		StringBuilder query =
				
				new StringBuilder()
				.append("UPDATE doc ")
				.append("SET name = ?, description = ?, data = ? ")
				.append("WHERE id = ? ");
							
		try
		{
			PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString());
			
			preparedStatement.setString(1, doc.getName());
			
			preparedStatement.setString(2, doc.getDescription());
			
			byte[] docData = 
					
					DMZManager
					.getInstance()
					.getDocData(doc);
			
			preparedStatement.setBytes(3, docData);
			
			dbConnection.executeStatement(preparedStatement);
			
			Logger.debug(doc.getName() + " successfully updated");
			
		} catch (SQLException e)
		{
			
			Logger.error("Error while trying to update Doc: \n" + e);
			
			Logger.trace(e);
		}
	}
	
	public void deleteDoc(Doc doc)
	{
		Logger.debug("Deleting " + doc.getName());
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("DELETE ")
				.append("FROM doc ")
				.append("WHERE id = ? ");
		
		try
		{
			PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString());
			
			preparedStatement.setInt(1, doc.getId());
			
			dbConnection.executeStatement(preparedStatement);
			
			Logger.debug(doc.getName() + " successfully deleted");
			
		} catch (SQLException e)
		{
			
			Logger.error("Error while trying to delete Doc: \n" + e);
			
			Logger.trace(e);
		}
	}
	
	public byte[] gatherDataForDoc(Doc doc)
	{
		Logger.debug("Gathering data for " + doc.getName());
		
		byte[] docData = null;
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("SELECT data ")
				.append("FROM doc d ")
				.append("WHERE d.id = ? ");
		
		try(PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString()))
		{
			preparedStatement.setLong(1, doc.getId());
			
			ResultSet resultSet = dbConnection.executeSelect(preparedStatement);
			
			if (resultSet.next())
			{
				docData = resultSet.getBytes("data");
			}
			
			Logger.debug(doc.getName() + "'s successfully gathered");
			
		} catch (SQLException e)
		{
			
			Logger.error("Error while trying to gather Doc data: \n" + e);
			
			Logger.trace(e);
		}
		
		return docData;
		
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void closeConnection()
	{
		Logger.info("Closing DataSource connection.");
		
		dbConnection.disconnect();
	}
}
