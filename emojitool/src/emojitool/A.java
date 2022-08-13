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

public class A {

	final static String allEmojiFile = "/Users/a1234/Desktop/emoji_test/all_emojis.txt";
	final static String selectEmojiFile = "/Users/a1234/Desktop/emoji_test/select_emojis.txt";
	final static String picSrc = "/Users/a1234/Desktop/workspace/totok/totok/external/Emoji/res/drawable-xhdpi/";
	final static String picDes = "/Users/a1234/Desktop/emoji_test/";

	private static void doSome() {
		HashSet<String> set = new HashSet<String>();
		HashMap<String, String> map = new HashMap<>();
		File fileAll = new File(allEmojiFile);
		File fileSelect = new File(selectEmojiFile);

		BufferedReader fileReaderAll = null;
		try {
			fileReaderAll = new BufferedReader(new FileReader(allEmojiFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line;
		try {
			String[] splitedItem = new String[2];
			while ((line = fileReaderAll.readLine()) != null) {

				int pos = line.indexOf('|');
				if (pos < 0 || pos >= line.length()) {
					continue;
				}

				int pos2 = line.indexOf('|', pos + 1);
				if (pos2 >= 0) {
					continue; // multiple
				}

				splitedItem[0] = line.substring(0, pos);
				splitedItem[1] = line.substring(pos + 1);

				// Log.i("YC", "src=[" + line + "] - [" + splitedItem[0] + "] - [" +
				// splitedItem[1] + "]");

				if (splitedItem != null && splitedItem.length == 2) {
					String code = splitedItem[0];
					String name = splitedItem[1];
					map.put(name, code);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fileReaderAll.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedReader fileReaderSelect = null;
		try {
			fileReaderSelect = new BufferedReader(new FileReader(fileSelect));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while ((line = fileReaderSelect.readLine()) != null) {
				String code=map.get(line);
				if(code!=null&&!code.equals("")) {
					String fileNameSrc=picSrc+"emoji_"+code+".png";
					String fileNameDes=picDes+"emoji_"+code+".png";
					File fileSrc=new File(fileNameSrc);
					File fileDes=new File(fileNameDes);
					if(fileSrc.exists()) {
						S.s("exist:"+fileSrc.getAbsolutePath());
						FileInputStream fileInputStream =new FileInputStream(fileSrc);
						FileOutputStream fileOutputStream =new FileOutputStream(fileDes);
						byte[]arr=new byte[1024];
						int i=-1;
						while((i=fileInputStream.read(arr))!=-1) {
							fileOutputStream.write(arr);
						}
						fileOutputStream.flush();
						fileOutputStream.close();
						fileInputStream.close();
					}else {
						S.e("not exist:"+fileSrc.getAbsolutePath());
					}
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fileReaderAll.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		doSome();
	}

}
