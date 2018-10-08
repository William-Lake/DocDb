package com.lakedev.docdb.service.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
		// TODO It would be useful to allow the user to connect to different databases, or identify what database they want to connect to.
		
		dbConnection = new DbConnection();
		
		boolean dbExists = 
				
				Files
				.exists(
						Paths
						.get(DbConnection.DB_PATH));
		
		if (dbExists == false) dbExists = createDb();

		dbExists = tablesExist();
		
		if (dbExists == false) dbExists = createTables();
		
		if (dbExists)
		{
			connected = dbConnection.connect();
		} else
		{
			// TODO Log that you couldn't find/create the DB or tables.
		}
	}
	
	public static DataSource getInstance()
	{
		return Wrapper.INSTANCE;
	}
	
	private boolean createDb()
	{
		boolean databaseCreated = false;
		
		try
		{
			Files.createFile(Paths.get(DbConnection.DB_PATH));
			
			databaseCreated = true;
			
		} catch (IOException e)
		{
			// TODO Log
			e.printStackTrace();
		}
		
		return databaseCreated;
	}
	
	private boolean tablesExist()
	{
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
				
				if (0 < tableCount && tableCount < DbConnection.TABLE_NAMES.length)
				{
					// Partial database.
					// TODO Alert user that there's something seriously wrong with the DB and they need to check it out.
					// TODO Log this, exit.
					// TODO May want to give them a chance to decide whether or not to create the DB fresh from scratch.
					//			If so, that will have to be handled from the DB and not here.
				}
			}
			
		} catch (SQLException e)
		{
			// TODO Alert user, log, exit
			e.printStackTrace();
		}
		
		return tablesExist;
	}
	
	private boolean createTables()
	{
		boolean tablesCreated = false;
		
		try
		{
			StringBuilder query = 
					
					new StringBuilder()
					.append("CREATE TABLE doc ")
					.append("( ")
					.append("    id INTEGER NOT NULL PRIMARY KEY, ")
					.append("    name TEXT NOT NULL, ")
					.append("    description TEXT, ")
					.append("    data BLOB NOT NULL,  ")
					.append("    add_date INTEGER, ")
					.append("    mod_date INTEGER  ")
					.append(") ");
			
			dbConnection.executeStatement(query.toString());
			
			query = 
					
					new StringBuilder()
					.append("CREATE TRIGGER trg_add_date  ")
					.append("AFTER INSERT ")
					.append("ON doc ")
					.append("BEGIN ")
					.append("    UPDATE doc  ")
					.append("    SET add_date = DATETIME('NOW')  ")
					.append("    WHERE id = NEW.id; ")
					.append("END ");
			
			dbConnection.executeStatement(query.toString());
			
			query = 
					
					new StringBuilder()
					.append("CREATE TRIGGER trg_mod_date ")
					.append("AFTER UPDATE ")
					.append("ON doc ")
					.append("BEGIN ")
					.append("    UPDATE doc  ")
					.append("    SET mod_date = DATETIME('NOW') ")
					.append("    WHERE id = NEW.id; ")
					.append("END ");
			
			dbConnection.executeStatement(query.toString());
			
			tablesCreated = true;
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
		
		return tablesCreated;
	}
	
	public List<Doc> gatherAllDocs()
	{
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
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		} 
		
		return docs;
	}
	
	public List<Doc> gatherDocsByNameOrDescription(String searchValue)
	{
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
			// TODO Log
			e.printStackTrace();
		}
		
		return docs;
	}
	
	public void addDoc(Doc doc)
	{
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
			
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
	}
	
	public void updateDoc(Doc doc)
	{
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
			
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
	}
	
	public void deleteDoc(Doc doc)
	{
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
			
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
	}
	
	public byte[] gatherDataForDoc(Doc doc)
	{
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
			
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
		
		return docData;
		
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void closeConnection()
	{
		dbConnection.disconnect();
	}
}
