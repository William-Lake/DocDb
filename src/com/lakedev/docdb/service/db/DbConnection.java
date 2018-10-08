package com.lakedev.docdb.service.db;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DbConnection
{
	private Connection connection;
	
	// TODO Figure out how to password protect and/or encrypt a sqlite db.
	// TODO Encrypt these and put them in a .properties file. 
	private static final String USER = ""; // TODO Pick out
	
	private static final String PASSWORD = ""; // TODO Pick out
	
	public static final String DB_PATH = Paths.get(System.getProperty("user.dir"), "Docs.db").toAbsolutePath().toString();
	
	public static final String[] TABLE_NAMES = {"doc"};
	
	private static final String CONNECTION_STRING = "jdbc:sqlite:" + DB_PATH;
	
	public boolean connect()
	{
		boolean connectionSuccessful = false;
		
		try
		{
			connection = DriverManager.getConnection(CONNECTION_STRING);
			
			connectionSuccessful = true;
		}
		catch (SQLException e)
		{
			// TODO Log
			e.printStackTrace();
		}
		
		return connectionSuccessful;
	}
	
	public ResultSet executeSelect(String query) throws SQLException
	{
		return connection.createStatement().executeQuery(query);
	}
	
	public ResultSet executeSelect(PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.executeQuery();
	}
	
	public boolean executeStatement(String sql) throws SQLException
	{
		return connection.createStatement().execute(sql);
	}
	
	public boolean executeStatement(PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.execute();
	}
	
	public PreparedStatement createPreparedStatement(String statement) throws SQLException
	{
		return connection.prepareStatement(statement, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	public void disconnect()
	{
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
