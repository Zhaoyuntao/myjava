package test;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class A {
    public static void main(String[] args) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(new File("abc.txt"), "rw");
             RandomAccessFile reader = new RandomAccessFile(new File("abc.txt"),"r");
        ) {
            String abc = "abcde";
            randomAccessFile.seek(1);
            randomAccessFile.write(abc.getBytes(),0,3);
            String a;
            reader.seek(2);
            while ((a = reader.readLine()) != null) {
                S.s(a);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
