package com.zhaoyuntao.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;


/**
 * 用DOM方式读取xml文件
 *
 * @author lune
 */
public class CompareDiff {
    private static DocumentBuilder db;

    static {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void s(Object o) {
        System.out.println(o);
    }

    public static Map<String, String> getBooks(String... fileNames) throws Exception {
        Map<String, String> mapAll = new HashMap<>();
        for (String filename : fileNames) {
            Map<String, String> map = getBook(filename);
            mapAll.putAll(map);
        }
        return mapAll;
    }

    public static Map<String, String> getBook(String fileName) throws Exception {
        // 将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
        Document document = db.parse(fileName);
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

    public static void diff(String file_diff, String... file_all_en) {
        try {
            Map<String, String> stringItemsDiff = CompareDiff.getBooks(file_diff);
            Map<String, String> stringItemsEn = CompareDiff.getBooks(file_all_en);
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            int count = 0;
            for (String key : stringItemsEn.keySet()) {
                if (!stringItemsDiff.containsKey(key)) {
                    String value = stringItemsEn.get(key);
                    sb.append("<string name=\"").append(key).append("\">").append(stringItemsEn.get(key)).append("</string>\n");
                    sb2.append(key).append("|").append(value).append("\n");
                    count++;
                }
            }
            System.out.println(sb.toString());
            System.out.println("missed:" + count);
            while (file_output.endsWith(File.separator)) {
                file_output = file_output.substring(0, file_output.length() - 1);
            }
            File dir = new File(file_output + File.separator);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(file_output + File.separator + "diff" + new SimpleDateFormat("_yyyyMMdd_hhmmss", Locale.ENGLISH).format(new Date(System.currentTimeMillis())) + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(sb.toString());
            outputStreamWriter.flush();
            s("file is saved in:" + file.getAbsolutePath());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //所有英语资源
    private static final String ftx_values = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values/fts_values.xml";
    private static final String strings = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values/strings.xml";
    private static final String strings_totok = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values/strings_totok.xml";
    private static final String strings_zxing = "/Users/a1234/Desktop/workspace/totok/totok/external/zxing3.3.2/src/main/res/values/strings.xml";
    private static final String strings_baseUI = "/Users/a1234/Desktop/workspace/totok/totok/BaseUI/library/res/values/strings.xml";
    private static final String strings_baseLib = "/Users/a1234/Desktop/workspace/totok/totok/BaseLib/library/res/values/strings.xml";
    //俄语
    private static final String strings_russian = "/Users/a1234/Desktop/workspace/totok/totok/BaseLib/library/res/values-ru/strings.xml";
    private static final String strings_russian_strings_totok = "/Users/a1234/Desktop/workspace/totok/totok/ZayhuApp/res/values-ru/strings_totok.xml";

    private static String file_output = "/Users/a1234/Desktop/diff_Russian/";

    public static void main(String args[]) {
        //后面的比前面的多哪些
        diff(strings_russian,ftx_values, strings, strings_totok, strings_zxing, strings_baseUI, strings_baseLib);
//        diff(strings_russian, strings_russian_strings_totok);
    }

}
