package com.lakedev.docdb.service.db;

import java.nio.file.Files;
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
	private static final String USER = ""; // TODO Pick out
	
	private static final String PASSWORD = ""; // TODO Pick out
	
	private static final String DB_PATH = Paths.get(System.getProperty("user.dir"), "Docs.db").toAbsolutePath().toString();
	
	private static final String CONNECTION_STRING = "jdbc:sqlite:" + DB_PATH;
	
	public boolean connect()
	{
		try
		{
			boolean createTables = Files.exists(Paths.get(DB_PATH)) == false;
			
			connection = DriverManager.getConnection(CONNECTION_STRING);
			
			if (createTables) createTables();
			
			return true;
		}
		catch (SQLException e)
		{
			// TODO Log this
			e.printStackTrace();
			
			return false;
		}
	}
	
	private void createTables()
	{
		// TODO Pretty sure the
		
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
			
			executeStatement(query.toString());
			
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
			
			executeStatement(query.toString());
			
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
			
			executeStatement(query.toString());
			
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

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
