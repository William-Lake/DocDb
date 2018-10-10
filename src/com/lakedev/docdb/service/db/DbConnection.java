package com.lakedev.docdb.service.db;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.pmw.tinylog.Logger;

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
		Logger.info("Connecting to " + DB_PATH);
		
		boolean connectionSuccessful = false;
		
		try
		{
			connection = DriverManager.getConnection(CONNECTION_STRING);
			
			connectionSuccessful = true;
			
			Logger.debug("Successfully connected to " + DB_PATH);
		}
		catch (SQLException e)
		{
			
			Logger.error("Error while trying to connect to DB: \n" + e);
			
			Logger.trace(e);
		}
		
		return connectionSuccessful;
	}
	
	public ResultSet executeSelect(String statement) throws SQLException
	{
		Logger.debug("Executing Select: \n" + formatStatementForLogs(statement));
		
		return connection.createStatement().executeQuery(statement);
	}
	
	public ResultSet executeSelect(PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.executeQuery();
	}
	
	public boolean executeStatement(String statement) throws SQLException
	{
		Logger.debug("Executing Statement: \n" + formatStatementForLogs(statement));
		
		return connection.createStatement().execute(statement);
	}
	
	public boolean executeStatement(PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.execute();
	}
	
	public PreparedStatement createPreparedStatement(String statement) throws SQLException
	{
		Logger.debug("Creating PreparedStatement for String statement: \n" + formatStatementForLogs(statement));
		
		return connection.prepareStatement(statement, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	/**
	 * Formats the given String SQL statement so it can be 
	 * easy to read when placed in the logs.
	 * 
	 * @param statement
	 * 			The String statement to format for the logs.
	 * @return The formatted String statement.
	 */
	private String formatStatementForLogs(String statement)
	{
		return 
				
				statement
				.replaceAll(
						"((?:SELECT|INSERT|UPDATE|FROM|WHERE|AND|OR|INNER|LEFT))", 
						"\n\t$1") 
				+ "\n";
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
