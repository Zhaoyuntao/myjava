package net;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileRandomReader extends ZThread {
    private List<Reader> taskQueue;
    private RandomAccessFile randomAccessFile;
    private String filename;
    private File file;
    private long timeRead;
    private CallBack callBack;
    private String id;
    private int pieceSize;
    private int count;
    private String path;

    public FileRandomReader(String id, String path, String filename, CallBack callBack) {
        this.id = id;
        this.path = path;
        this.filename = filename;
        this.callBack = callBack;
        taskQueue = new ArrayList<>();
    }

    public void getFilePiece(Reader reader) {
        taskQueue.add(reader);
    }

    public String getTaskId() {
        return id;
    }

    @Override
    protected void init() {
        if (pieceSize <= 0) {
            S.e("文件块大小未赋值,无法读取文件并计算文件块大小");
            close();
            return;
        }
        file = new File(path, filename);
        //初始化读取时间
        timeRead = S.currentTimeMillis();
        if (file.exists()) {
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            callBack.whenFileFind(filename, this);

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                int filesize = fileInputStream.available();
                count = filesize / pieceSize;
                if (filesize % pieceSize > 0) {
                    count += 1;//尾部的余数也算一个片段
                }
                callBack.whenGotFileInfo(count, pieceSize, filesize);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            S.e("FileRandomReader:请求的文件不存在:" + filename);
            callBack.whenFileNotFind(filename);
            close();
        }
    }

    @Override
    protected void todo() {

        if (taskQueue.size() == 0) {
            long timeNow = S.currentTimeMillis();
            long during = timeNow - timeRead;
            //5秒未读取,就销毁该线程
            if (during > 5 * 1000) {
                close();
                //当超时时,调用回调
                callBack.whenTimeOut(filename);
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //刷新读取时间
            timeRead = S.currentTimeMillis();
            Reader reader = taskQueue.remove(0);
            try {
                //计算文件读取位置
                int[] indexs = reader.getIndexs();
                for (int index : indexs) {
                    int position = index * pieceSize;
                    //移动到计算好的位置开始读取数据
                    randomAccessFile.seek(position);
                    byte[] piece = new byte[pieceSize];
                    int num = randomAccessFile.read(piece);
                    if (num > 0) {
                        piece = Arrays.copyOf(piece, num);
                    }
                    reader.whenGotFilePiece(filename, count, index, piece);
                }
                reader.whenTaskEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void close() {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.close();
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public interface CallBack {
        void whenTimeOut(String filename);

        void whenFileNotFind(String filename);

        void whenFileFind(String filename, FileRandomReader fileRandomReader);

        void whenGotFileInfo(int count, int piecesize, int filesize);
    }


    public interface Reader {

        int[] getIndexs();

        void whenGotFilePiece(String filename, int count, int index, byte[] piece);

        void whenTaskEnd();
    }
}
