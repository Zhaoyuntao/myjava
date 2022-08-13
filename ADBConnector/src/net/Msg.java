package net;


import java.util.Arrays;

public class Msg {
    private long time;
    public String id;
    public String ip;
    public byte[] msg;
    //文件大小
    public long filesize;
    //分块消息包个数
    public int count;
    //当前消息包的位置
    public int index;
    public byte type;
    public String title;
    //title占200字节
    public static final int titleByteArrLen = 600;
    public String filename;
    public static final byte HEARTBIT = 0;
    public static final byte LOGOUT = 1;
    public static final byte RESPONSE = 2;


    public static final byte ASK = 10;
    public static final byte ANSWER = 11;
    //文件块消息
    public static final byte FILE_PIECE = 13;
    //客户端请求文件块
    public static final byte ASK_FILE_PIECE = 14;
    //文件信息
    public static final byte FILE_INFO = 15;
    //客户端请求文件信息
    public static final byte ASK_FILE_INFO = 16;
    public static final byte FILE_CHECK = 17;

    //id占100字节
    private static int idByteArrLen = 100;
    //文件名占200字节
    public static final int filenameByteArrLen = 200;

    public ZThread zThread;
    public long timeSend;

    public Msg() {
        this(getRandomId());
    }

    public Msg(String id) {
        this.id = id;
        this.time=S.currentTimeMillis();
    }

    public static String getRandomId() {
        return "socketid" + S.currentTimeMillis();
    }

    //组包:类型:1字节,id:100字节,文件块长度:4,文件块下标:4,文件名:4+200,正文:不定,总长度=213+正文长度
    public static byte[] getPackage(Msg msg) {
        if (msg == null) {
            return null;
        }
        byte[] data = new byte[0];
        //类型,1字节---------------------------------------------------------------------------
        data = Arrays.copyOf(data, data.length + 1);
        data[0] = msg.type;

        //id数组长度,4字节---------------------------------------------------------------------------
        byte[] id = msg.id.getBytes();
        byte[] len_id = S.intToByteArr(id.length);
        data = Arrays.copyOf(data, data.length + len_id.length);
        System.arraycopy(len_id, 0, data, data.length - len_id.length, len_id.length);

        //id,100字节---------------------------------------------------------------------------
        data = Arrays.copyOf(data, data.length + idByteArrLen);
        System.arraycopy(id, 0, data, data.length - idByteArrLen, id.length);
        //文件块相关---------------------------------------------------------------------------
        //文件总大小
        byte[] filesize = S.longToByteArr(msg.filesize);
        data = Arrays.copyOf(data, data.length + filesize.length);
        System.arraycopy(filesize, 0, data, data.length - filesize.length, filesize.length);

        //文件块总个数,4字节
        byte[] count = S.intToByteArr(msg.count);
        data = Arrays.copyOf(data, data.length + count.length);
        System.arraycopy(count, 0, data, data.length - count.length, count.length);
        //当前文件块下标,4字节
        byte[] index = S.intToByteArr(msg.index);
        data = Arrays.copyOf(data, data.length + index.length);
        System.arraycopy(index, 0, data, data.length - index.length, index.length);
        //文件块写入位置,8字节
//        byte[] position = S.longToByteArr(msg.position);
//        data = Arrays.copyOf(data, data.length + position.length);
//        System.arraycopy(position, 0, data, data.length - position.length, position.length);

        //文件名相关---------------------------------------------------------------------------
        byte[] filenameArr = null;
        if (S.isNotEmpty(msg.filename)) {
            filenameArr = msg.filename.getBytes();
            if (filenameArr.length > filenameByteArrLen / 2) {
                S.e("文件名长度不能大于" + filenameByteArrLen / 2 + "!");
                return null;
            }
        }
        //文件名数组长度,4字节
        data = Arrays.copyOf(data, data.length + 4);
        if (filenameArr != null) {
            byte[] strlen = S.intToByteArr(filenameArr.length);
//            S.s("文件名长度1:" + filenameArr.length + " [" + msg.filename + "]");
            System.arraycopy(strlen, 0, data, data.length - 4, strlen.length);
        }
        //文件名内容,200字节
        data = Arrays.copyOf(data, data.length + filenameByteArrLen);
        if (filenameArr != null) {
            byte[] tmp = Arrays.copyOf(filenameArr, filenameByteArrLen);
            System.arraycopy(tmp, 0, data, data.length - filenameByteArrLen, filenameByteArrLen);
        }
        //title相关---------------------------------------------------------------------------
        byte[] titleArr = null;
        if (S.isNotEmpty(msg.title)) {
            titleArr = msg.title.getBytes();
            if (titleArr.length > titleByteArrLen / 2) {
                S.e("title长度不能大于" + titleByteArrLen / 2 + "!");
                return null;
            }
        }
        //title数组长度
        data = Arrays.copyOf(data, data.length + 4);
        if (titleArr != null) {
            byte[] strlen = S.intToByteArr(titleArr.length);
            System.arraycopy(strlen, 0, data, data.length - 4, strlen.length);
        }
        //title内容
        data = Arrays.copyOf(data, data.length + titleByteArrLen);
        if (titleArr != null) {
            byte[] tmp = Arrays.copyOf(titleArr, titleByteArrLen);
            System.arraycopy(tmp, 0, data, data.length - titleByteArrLen, titleByteArrLen);
        }

        //消息正文,任意字节数---------------------------------------------------------------------------
        if (msg.msg != null && msg.msg.length > 0) {
            data = Arrays.copyOf(data, data.length + msg.msg.length);
            System.arraycopy(msg.msg, 0, data, data.length - msg.msg.length, msg.msg.length);
        }
        return data;
    }

    //解包
    public static Msg releasePackage(byte[] tmp) {
        byte[] data = Arrays.copyOf(tmp, tmp.length);
        Msg msg = new Msg();
        //类型
        if (data.length >= 1) {
            msg.type = data[0];
            data = Arrays.copyOfRange(data, 1, data.length);
        }
        //id
        if (data.length >= 4 + 100) {
            //id数组长度
            byte[] idlen = Arrays.copyOfRange(data, 0, 4);
            int len = S.byteArrToInt(idlen);
            data = Arrays.copyOfRange(data, 4, data.length);
            //id内容
            byte[] id = Arrays.copyOfRange(data, 0, idByteArrLen);
            msg.id = new String(id, 0, len);
            data = Arrays.copyOfRange(data, idByteArrLen, data.length);
        }
        //文件块
        if (data.length >= (8 + 4 + 4 + 8)) {
            //文件总大小
            msg.filesize = S.byteArrToLong(Arrays.copyOfRange(data, 0, 8));
            data = Arrays.copyOfRange(data, 8, data.length);
            //文件块总长度
            msg.count = S.byteArrToInt(Arrays.copyOfRange(data, 0, 4));
            data = Arrays.copyOfRange(data, 4, data.length);
            //文件块当前下标
            msg.index = S.byteArrToInt(Arrays.copyOfRange(data, 0, 4));
            data = Arrays.copyOfRange(data, 4, data.length);
        }
        //文件名
        if (data.length >= filenameByteArrLen + 4) {
            //文件名长度
            byte[] filenameLength = Arrays.copyOfRange(data, 0, 4);
            int len = S.byteArrToInt(filenameLength);
            data = Arrays.copyOfRange(data, 4, data.length);
            //文件名
            byte[] filenameByteArr = Arrays.copyOfRange(data, 0, filenameByteArrLen);
            msg.filename = new String(filenameByteArr, 0, len);
//            S.s("filename:[" + msg.filename + "]");
            data = Arrays.copyOfRange(data, filenameByteArrLen, data.length);
        }
        //title
        if (data.length >= titleByteArrLen + 4) {
            //title长度
            byte[] titleLength = Arrays.copyOfRange(data, 0, 4);
            int len = S.byteArrToInt(titleLength);
            //去掉记录长度的4个字节,留下title的数组内容
            data = Arrays.copyOfRange(data, 4, data.length);
            //title内容
            byte[] titleByteArr = Arrays.copyOfRange(data, 0, titleByteArrLen);
            msg.title = new String(titleByteArr, 0, len);
            data = Arrays.copyOfRange(data, titleByteArrLen, data.length);
        }
        //正文
        msg.msg = Arrays.copyOfRange(data, 0, data.length);
        return msg;
    }

    public TimeOut timeOut;

    public long getTime() {
        return time;
    }

    public interface TimeOut{
        void whenTimeOut();
    }
}
