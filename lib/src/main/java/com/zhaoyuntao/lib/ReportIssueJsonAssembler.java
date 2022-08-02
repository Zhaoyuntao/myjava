package com.zhaoyuntao.lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * created by zhaoyuntao
 * on 11/05/2020
 * description:
 */
public class ReportIssueJsonAssembler {

    private static String filePath = "/Users/a1234/Desktop/report&welcome translation 14lans";
    private static String fileNameCommon = "totok_wording_0511_";

    /**
     * [
     * {
     * "name":"Calls",
     * "subName":[
     * "Doesn't ring",
     * "Poor quality",
     * "Can't hang up",
     * "Unexpected drop off",
     * "Camera doesn’t work",
     * "Unable to call",
     * "Bluetooth doesn’t work",
     * "Other"
     * ]
     * },
     * {
     * "name":"Chats",
     * "subName":[
     * "Not get notification",
     * "Download media",
     * "Share media",
     * "Other"
     * ]
     * },
     * {
     * "name":"Other",
     * "subName":[
     * "Find contacts",
     * "Network connection",
     * "Verification code",
     * "Login",
     * "Other"
     * ]
     * }
     * ]
     * <p>
     * 需要翻译的内容为“name”的值和“subName”里面的元素。
     *
     * @param filename
     * @return
     */
    public static String assembler(String filePath,String filename) {
        File file = new File(filePath, filename);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(fileInputStream));
        JSONArray jsonArray=new JSONArray();
        try {
            JSONObject oneObject=null;
            while (true) {
                String line = inputStreamReader.readLine();
//                System.out.println("line:"+line);
                if (line != null && !"".equals(line)) {
                    String[] arr = line.split("\\|");
                    if (arr.length >= 2) {
                        String key = arr[0];
                        String value = arr[1];
//                        System.out.println("key:"+key+" value:"+value);
                        if ("calls".equals(key)||"chats".equals(key)||"other".equals(key)) {
                            oneObject=new JSONObject();
                            jsonArray.put(oneObject);
                            oneObject.put("name",value);
                        }else if(key.contains("calls")||key.contains("chats")||key.contains("other")){
                            JSONArray tags=null;
                            try {
                                tags=oneObject.getJSONArray("subName");
                            }catch (Exception e){

                            }
                            if(tags==null){
                                tags=new JSONArray();
                                oneObject.put("subName",tags);
                            }
                            tags.put(value);
                        }
                    }
                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    private static final String names[] = {"am", "de", "es", "fa", "fr", "he", "hi", "id", "it", "ml", "pt", "ru", "tr", "ur"};

    public static void main(String[] args) {
        for (String name : names) {
            String filename = fileNameCommon + name + ".txt";
            System.out.println("["+name+ "]:");
            System.out.println(assembler(filePath,filename));
        }

    }
}
