package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 执行文件下载的线程
 */
public class FileDownloadTask extends ZThread {
    private RandomAccessFile randomAccessFile;
    private List<TaskMsg> blockingDeque;
    private String id;
    private String filename;
    private File file;
    private CallBack callBack;
    private int indexNow;
    private int count;
    private int countNow;
    private int pieceSize;
    private long fileSize;
    private String ip;
    private byte[] check;
    private long time_recv;

    private String fileCacheDir ;

    public FileDownloadTask(String id, String fileCacheDir,String filename, CallBack callBack) {
        this.fileCacheDir=fileCacheDir;
        this.id = id;
        this.filename = filename;
        this.callBack = callBack;
        blockingDeque = new ArrayList<>();
    }

    public synchronized void addPiece(byte[] data, int index) {
        if (check[index] != 0) {
            return;
        }
        check[index]=1;
        TaskMsg taskMsg = new TaskMsg();
        taskMsg.data = data;
        taskMsg.index = index;
        blockingDeque.add(taskMsg);
    }

    public String getTaskId() {
        return id;
    }


    @Override
    protected void init() {
//        S.s("FileDownloadTask 已启动...");
        //文件不存在
        if (count <= 0 || pieceSize <= 0) {
            S.e("FileDownloadTask:文件不存在,无法下载:" + filename);
            callBack.whenFileNotFind(filename);
            close();
            return;
        }
        File path = new File(fileCacheDir);
        if (!path.exists()) {
            path.mkdirs();
        }
        this.file = new File(fileCacheDir, filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            S.e(e);
        }
        if (callBack != null) {
            callBack.whenStartDownloading(file.getAbsolutePath(), fileSize);
        }
        //初始要先发送一次文件请求
        check();
    }

    @Override
    protected void todo() {
        long time_now = S.currentTimeMillis();

        if (blockingDeque.size() == 0) {
            long during = time_now - time_recv;
            if (during > 500) {
                time_recv = time_now;
//                check();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            time_recv = time_now;
            //开始写入下标
            TaskMsg taskMsg = blockingDeque.remove(0);
            countNow++;
            try {
                //文件块下标
                indexNow = taskMsg.index;
                //文件二进制数据
                byte[] data = taskMsg.data;

                //计算写入位置
                int position = indexNow * pieceSize;
//                S.s("正在接收文件[" + countNow + "/" + count + "] 数据长度:" + data.length + " 写入位置:" + position);
//                S.s("------------index<" + indexNow + ">count<"+countNow+">----------");
                //写入文件
                randomAccessFile.seek(position);
                randomAccessFile.write(data);

                //进度
                if (callBack != null) {
                    callBack.whenDownloading(file.getAbsolutePath(), (countNow) / (float) count);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查遗漏的文件块
     */
    public void check() {
        int[] lose = new int[0];
        for (int i = 0; i < check.length; i++) {
            if (check[i] == 0) {
                lose = Arrays.copyOf(lose, lose.length + 1);
                lose[lose.length - 1] = i;
            }
        }
        if (lose.length > 0) {
            callBack.checkLost(filename,lose, ip, this);
        } else {
            close();
            if (callBack != null) {
                callBack.whenDownloadCompleted(this, file.getAbsolutePath());
            }
        }
    }

    @Override
    public void close() {
        try {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.close();
    }

    public void setCount(int count) {
        this.count = count;
        this.check = new byte[count];
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public static class TaskMsg {
        public int index;
        byte[] data;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public interface CallBack {
        void whenFileNotFind(String filename);

        void whenDownloadCompleted(FileDownloadTask fileDownloadTask, String filename);

        void whenDownloading(String filename, float percent);

        void whenStartDownloading(String filename, long filesize);

        void checkLost(String filename,int[] lose, String ip, FileDownloadTask fileDownloadTask);
    }
}
