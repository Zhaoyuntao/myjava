package emojitool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class B {
	final static String picSrc = "/Users/a1234/Desktop/workspace/totok/totok/external/Emoji/res/drawable-xhdpi/";
	private static void doSome() {
		HashSet<String> set = new HashSet<String>();
		HashMap<String, String> map = new HashMap<>();
		File fileAll = new File(picSrc);
		if(fileAll.exists()&&fileAll.isDirectory()) {
			File[]files=fileAll.listFiles();
			for(File file:files) {
				String filename=file.getName();
				S.s(filename);
				S.s(file.getAbsolutePath());
				if(filename.contains("emoji_u")) {
					file.renameTo(new File(file.getAbsolutePath().replaceAll("emoji_u", "emoji_")));
				}
			}
		}
	}

	public static void main(String[] args) {
		doSome();
	}

}
