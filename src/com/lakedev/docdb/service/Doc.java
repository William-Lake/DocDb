package com.lakedev.docdb.service;

import java.nio.file.Path;
import java.time.LocalDate;

public class Doc
{
	private int id;
	
	private String name;
	
	private String description;
	
	private LocalDate addDate;
	
	private LocalDate modDate;
	
	private Path docPath;

	public Doc(int id, String name, String description, LocalDate addDate, LocalDate modDate)
	{
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.addDate = addDate;
		this.modDate = modDate;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public LocalDate getAddDate()
	{
		return addDate;
	}

	public LocalDate getModDate()
	{
		return modDate;
	}
	
	public void setDocPath(Path docPath)
	{
		this.docPath = docPath;
	}
	
	public Path getDocPath()
	{
		return docPath;
	}
	
}
