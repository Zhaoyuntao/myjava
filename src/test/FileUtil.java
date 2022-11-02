package test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
    public static final String divider = "#@#";
    public static final String KEY_ADB_PATH = "adbPath";
    public static final String KEY_LAST_SQL = "defaultSql";
    public static final String KEY_PACKAGE_NAME = "defaultPackageName";
    public static final String KEY_SELECT_JSON = "selectJSON";
    public static final String KEY_SELECT_BLOB = "selectBLOB";
    public static final String KEY_SELECT_COLOR = "selectColor";
    public static final String KEY_LAST_COLUMN_LENGTH = "columnLength";
    public static final String FILE_SETTINGS = "settings.zyt";

    public static void delete() {
        new File(FILE_SETTINGS).delete();
    }

    public static Map<String, String> read() {
        Map<String, String> map = new HashMap<>();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(new File(FILE_SETTINGS), "rw");
        } catch (FileNotFoundException e) {
            S.e(e);
            return map;
        }
        try {
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (!line.contains(divider)) {
                    continue;
                }
                String[] keyValue = line.split(divider);
                S.s(Arrays.toString(keyValue));
                if (keyValue.length == 2) {
                    map.put(keyValue[0], keyValue[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            S.e(e);
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
            }
        }
        return map;
    }

    public static boolean write(Map<String, String> list) {
        File settingsFile = new File(".", FileUtil.FILE_SETTINGS);
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        if (list == null || list.size() == 0) {
            return false;
        }
        FileOutputStream randomAccessFile = null;
        try {
            randomAccessFile = new FileOutputStream(FILE_SETTINGS);
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : list.entrySet()) {
                String key = entry.getKey();
                if (isEmpty(key)) {
                    continue;
                }
                String value = isEmpty(entry.getValue()) ? "" : entry.getValue();
                stringBuilder.append(key).append(divider).append(value).append("\n");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            randomAccessFile.write(stringBuilder.toString().getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
