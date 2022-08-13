package com.lune.readxmlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Node;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.lune.bean.Book;

/**
 * 用DOM方式读取xml文件
 * 
 * @author lune
 */
public class ReadxmlByDom {
	static String file_Arabic = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values-ar/strings_totok.xml";
	static String file_Chinese = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values-zh/strings_totok.xml";
	static String file_English = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values/strings_totok.xml";
	static String file_Russian = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values-ru/strings_totok.xml";
	static String output_diff_Arabic = "/Users/a1234/Desktop/diff_Arabic/";
	static String output_diff_Chinese = "/Users/a1234/Desktop/diff_Chinese/";
	static String output_diff_Russian = "/Users/a1234/Desktop/diff_Russian/";
	private static DocumentBuilderFactory dbFactory = null;
	private static DocumentBuilder db = null;
	private static Document document = null;
	static {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			db = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	public static void s(Object o) {
		System.out.print(o);
	}
	public static Map<String, String> getBooks(String fileName) throws Exception {
		// 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
		document = db.parse(fileName);
		// 按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList
		NodeList bookList = document.getElementsByTagName("string");
		Map<String, String> books = new HashMap<String, String>();
		// 遍历books
		for (int i = 0; i < bookList.getLength(); i++) {
			org.w3c.dom.Node node = bookList.item(i);
			// 获取第i个book的所有属性
			NamedNodeMap namedNodeMap = node.getAttributes();
			// 获取已知名为name的属性值
			org.w3c.dom.Node node2 = namedNodeMap.getNamedItem("name");
			String name = node2.getTextContent();// System.out.println(id);
			String value = node.getTextContent();
			books.put(name, value);

		}

		return books;

	}

	public static void diff(String file_tmp,String file_output) {
		try {
			Map<String, String> list = ReadxmlByDom.getBooks(file_tmp);
			Map<String, String> list2 = ReadxmlByDom.getBooks(file_English);
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			int count = 0;
			for (String key : list2.keySet()) {
				if (!list.containsKey(key)) {
					String value = list2.get(key);
					sb.append("<string name=\"" + key + "\">" + list2.get(key) + "</string>\n");
					sb2.append(key + "|" + value + "\n");
					count++;
				}
			}
			System.out.println(sb.toString());
			System.out.println("missed:" + count);
			while(file_output.endsWith(File.separator)) {
				file_output=file_output.substring(0,file_output.length()-1);
			}
			File dir=new File(file_output+File.separator);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			File file=new File(file_output+File.separator+"diff"+new SimpleDateFormat("_yyyyMMdd_hhmmss").format(new Date(System.currentTimeMillis()))+".txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
			outputStreamWriter.write(sb.toString());
			outputStreamWriter.flush();
			s("file is saved in:"+file.getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void diff_Chinese() {
		diff(file_Chinese,output_diff_Chinese);
	}
	public static void diff_Arabic() {
		diff(file_Arabic,output_diff_Arabic);
	}
	public static void diff_Russian() {
		diff(file_Russian,output_diff_Russian);
	}
	public static void main(String args[]) {
//		diff_Russian();
		s(":"+"a|b|sssc|d".split("\\|").length);
	}
	
}
