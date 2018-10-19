package com.lakedev.docdb;

import java.nio.file.StandardWatchEventKinds;

public class Test
{
	public Test()
	{
		new Thread(new Runnable()
		{
			// Example showing that the hashcode of the StandardWatchEventKinds remains the same regardless of Thread.
			
			@Override
			public void run()
			{
				System.out.printf("Inner thread, Create, %d%n",StandardWatchEventKinds.ENTRY_CREATE.hashCode()); 
				System.out.printf("Inner thread, Delete, %d%n",StandardWatchEventKinds.ENTRY_DELETE.hashCode()); 
				System.out.printf("Inner thread, Modify, %d%n",StandardWatchEventKinds.ENTRY_MODIFY.hashCode());
			}
		}).start();
		
		System.out.printf("Outer thread, Create, %d%n",StandardWatchEventKinds.ENTRY_CREATE.hashCode()); 
		System.out.printf("Outer thread, Delete, %d%n",StandardWatchEventKinds.ENTRY_DELETE.hashCode()); 
		System.out.printf("Outer thread, Modify, %d%n",StandardWatchEventKinds.ENTRY_MODIFY.hashCode());
	}
	public static void main(String[] args)
	{
		new Test();
	}

}
