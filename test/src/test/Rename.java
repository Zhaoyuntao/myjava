package test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rename {
	public static void s(Object o) {
		System.out.println(o);
	}
	
	public synchronized void show2(String name) {
		s("name:"+name);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void show3(String name) {
		s("name:"+name);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void show() {
		File dir=new File("/Users/a1234/Desktop/未命名文件夹/Gif图层133张/");
		if(!dir.isDirectory()||!dir.exists()) {
			return;
		}
		s("exists");
		File[]files=dir.listFiles();
		Pattern pa=Pattern.compile("-[0-9]*");
		for(int i=0;i<files.length;i++) {
			File file=files[i];
			String filename =file.getAbsolutePath();
			Matcher ma =pa.matcher(filename);
			if(!ma.find()) {
				continue;
			}
			
			File des=new File("/Users/a1234/Desktop/未命名文件夹/Gif图层133张/b"+ma.group()+".png");
			file.renameTo(des);
			
			s(file.getAbsolutePath()+"   -->   "+des.getAbsolutePath());
		}
	}
	
	public static void main(String[]args) {
		Rename rename=new Rename();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				rename.show2("1");
			}
		}).start();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				new Rename().show2("2");
			}
		}).start();
	}
}
