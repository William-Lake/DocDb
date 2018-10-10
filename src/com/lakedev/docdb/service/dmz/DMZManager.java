package com.lakedev.docdb.service.dmz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.lakedev.docdb.service.db.DataSource;
import com.lakedev.docdb.service.db.Doc;

public class DMZManager
{
	private boolean dmzExists;
	
	private WatchService watchService;
	
	private WatchKey watchKey;
	
	private static final class Wrapper
	{
		static final DMZManager INSTANCE = new DMZManager();
	}
	
	private static final String DMZ_DIR = "DMZ";
	
	public static DMZManager getInstance()
	{
		return Wrapper.INSTANCE;
	}
	
	private DMZManager()
	{
		Path dmzDirPath = Paths.get(DMZ_DIR);
		
		dmzExists = Files.exists(dmzDirPath);
		
		if (dmzExists == false)
		{
			try
			{
				Files.createDirectory(dmzDirPath);
				
				dmzExists = true;
			} catch (IOException e)
			{
				// TODO Log
				e.printStackTrace();
			}
		}
		
		if (dmzExists)
		{
			try
			{
				watchService = 
						
						FileSystems
						.getDefault()
						.newWatchService();
				
				watchKey = 
						
						Paths
						.get(DMZ_DIR)
						.register(
								watchService, 
								StandardWatchEventKinds.ENTRY_CREATE, 
								StandardWatchEventKinds.ENTRY_DELETE, 
								StandardWatchEventKinds.ENTRY_MODIFY);
				
				/*
				 * This is how you continually check a directory for a change.
				 * This process will be central to the UI's DMZ status table,
				 * which means it will need to be:
				 * 		1. On it's own thread
				 * 		2. Able to update UI
				 * 		3. Able to update resources on the primary thread.
				 * 
				 * TODO Research how this is best completed.
				 * 
				 * ProcessBuilder: https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html
				 * Java Tutorials - Concurrency: https://docs.oracle.com/javase/tutorial/essential/concurrency/
				 */
//				try
//				{
//					while ((watchKey = watchService.take()) != null)
//					{
//						for (WatchEvent<?> event : watchKey.pollEvents())
//						{
//							// Map the event with it's associated document.
//						}
//						
//						watchKey.reset();
//					}
//				} catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
			} catch (IOException e)
			{
				// TODO Log
				e.printStackTrace();
				
				dmzExists = false;
			}
		}
		
	}
	
	public boolean removeFromDmz(Doc doc)
	{
		Path docPath = Paths.get(DMZ_DIR, doc.getName());
		
		boolean docRemoved = false;
		
		if (Files.exists(docPath))
		{
			try
			{
				Files.delete(docPath);
				
				docRemoved = true;
			} catch (IOException e)
			{
				// TODO Log
				e.printStackTrace();
			}
		}
		
		return docRemoved;
	}
	
	// http://www.sqlitetutorial.net/sqlite-java/jdbc-read-write-blob/
	public byte[] getDocData(Doc doc)
	{
		byte[] docData = null;
		
		if (doc.getName() == null)
		{
			// TODO Alert user, do not continue.
		} else
		{
			ByteArrayOutputStream byteArrayOutputStream = null;
			
			try (FileInputStream fileInputStream = new FileInputStream(Paths.get(DMZ_DIR, doc.getName()).toFile()))
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
				e.printStackTrace();
			} catch (IOException e2)
			{
				// TODO Log
				e2.printStackTrace();
			}
			
			docData = 
					
					byteArrayOutputStream != null ? 
							
							byteArrayOutputStream.toByteArray() : 
								
								null;
		}
		
		return docData;
		
	}
	
	private void addToDmz(Doc doc)
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
				fileOutputStream = new FileOutputStream(Paths.get(DMZ_DIR, doc.getName()).toFile());
				
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
	
	public boolean dmzExists()
	{
		return dmzExists;
	}
}
