package com.lakedev.docdb.service.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.lakedev.docdb.service.Doc;
import com.lakedev.docdb.service.DocCoverter;

import javafx.application.Platform;

public class DataSource
{
	private DbConnection dbConnection;
	
	private static final DateTimeFormatter DOC_DATE_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss");
	
	private static final class Wrapper
	{
		private static DataSource INSTANCE = new DataSource();
	}
	
	private DataSource()
	{
		dbConnection = new DbConnection();
		
		if (dbConnection.connect() == false)
		{
			// There was an issue with the db connection. TODO Log this and let the user know.
			
			Platform.exit();
		}
	}
	
	public static DataSource getInstance()
	{
		return Wrapper.INSTANCE;
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
		
		try
		{
			PreparedStatement preparedStatement = 
					
					dbConnection
					.createPreparedStatement(query.toString());
			
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
		} finally
		{
			if (results != null)
				try
				{
					closeResultSetAndStatement(results);
				} catch (SQLException e)
				{
					// TODO Log
					e.printStackTrace();
				}
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
			
			byte[] fileData = 
					
					DocCoverter
					.getInstance()
					.get
			
//			preparedStatement.setBytes(3, doc.getData()); // TODO get this from the DocConverter
			
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
			
//			preparedStatement.setBytes(3, doc.getData()); // TODO get this from the DocConverter
			
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
		byte[] fileData = null;
		
		StringBuilder query = 
				
				new StringBuilder()
				.append("SELECT data ")
				.append("FROM doc d ")
				.append("WHERE d.id = ? ");
		
		try
		{
			PreparedStatement preparedStatement = dbConnection.createPreparedStatement(query.toString());
			
			preparedStatement.setLong(1, doc.getId());
			
			ResultSet resultSet = dbConnection.executeSelect(preparedStatement);
			
			if (resultSet.next())
			{
				fileData = resultSet.getBytes("data");
			}
			
			closeResultSetAndStatement(resultSet);
		} catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
		
		return fileData;
		
	}
	
	/**
	 * Closes the provided ResultSet and associated Statement that produced it.
	 * 
	 * @param resultSet
	 * 			The ResultSet to close.
	 * 
	 * @throws SQLException
	 */
	public void closeResultSetAndStatement(ResultSet resultSet) throws SQLException
	{
		Statement stmt = resultSet.getStatement();
		
		resultSet.close();
		
		if(stmt != null) stmt.close();
	}

	public void closeConnection()
	{
		dbConnection.disconnect();
	}
}
