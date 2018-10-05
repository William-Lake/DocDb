package com.lakedev.docdb.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.lakedev.docdb.service.db.DataSource;

public class DocCoverter
{
	private static final class Wrapper
	{
		static final DocCoverter INSTANCE = new DocCoverter();
	}
	
	private DocCoverter()
	{
		
	}
	
	public static DocCoverter getInstance()
	{
		return Wrapper.INSTANCE;
	}
	
	// http://www.sqlitetutorial.net/sqlite-java/jdbc-read-write-blob/
	public byte[] getDocData(Doc doc)
	{
		byte[] docData = null;
		
		if (doc.getDocPath() == null)
		{
			// TODO Alert user, do not continue.
		} else
		{
			ByteArrayOutputStream byteArrayOutputStream = null;
			
			try (FileInputStream fileInputStream = new FileInputStream(doc.getDocPath().toFile()))
			{
				byte[] buffer = new byte[1024];
				
				byteArrayOutputStream = new ByteArrayOutputStream();
				
				for (int len; (len = fileInputStream.read(buffer)) != -1;)
				{
					byteArrayOutputStream.write(buffer, 0, len);
				}
			} catch (FileNotFoundException e)
			{
				// TODO Log
				System.err.println(e.getMessage());
			} catch (IOException e2)
			{
				// TODO Log
				System.err.println(e2.getMessage());
			}
			
			docData = 
					
					byteArrayOutputStream != null ? 
							
							byteArrayOutputStream.toByteArray() : 
								
								null;
		}
		
		return docData;
		
	}
	
	private void writeDocumentToFile(Doc doc, Path filePath)
	{
		FileOutputStream fileOutputStream = null;
		
		byte[] fileData = 
				
				DataSource
				.getInstance()
				.gatherDataForDoc(doc);
		
		if (fileData == null)
		{
			// TODO Alert user, do not continue.
		} else
		{
			try
			{
				fileOutputStream = new FileOutputStream(filePath.toFile());
				
				InputStream inputStream = new ByteArrayInputStream(fileData);
				
				byte[] buffer = new byte[1024];
				
				while (inputStream.read(buffer) > 0) fileOutputStream.write(buffer);
				
			} catch (FileNotFoundException e)
			{
				// TODO Log
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Log
				e.printStackTrace();
			} finally
			{
				if (fileOutputStream != null)
					try
					{
						fileOutputStream.close();
					} catch (IOException e)
					{
						// TODO Log
						e.printStackTrace();
					}
			}
		}
	}
}
