package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class Sender extends ZThread {

    public static final int maxPackagSize = 63 * 1024;//UDP每个包最大不能超过64kb
    DatagramSocket datagramSocket;
    private Map<String, Client> clients;
    private BlockingDeque<Msg> queue;
    private Map<String, Msg> cache_timeout;
    private int port;
    private CallBack callBack;

    public Sender(int port, Map<String, Client> clients, CallBack callBack) {
        this.port = port;
        this.clients = clients;
        this.callBack = callBack;
        queue = new LinkedBlockingDeque<>();
        cache_timeout = new ConcurrentHashMap<>();
    }

    public void send(Msg msg) {
        queue.add(msg);
        cache_timeout.put(msg.id, msg);
        resumeThread();
    }

    @Override
    protected void init() {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e2) {
            e2.printStackTrace();
            S.e(e2);
        }
    }

    @Override
    protected void todo() {

        if (clients.size() <= 0) {
            this.pauseThread();
            return;
        }


        Msg msg = null;

        try {
            msg = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (msg == null) {
            return;
        }

        //组包
        byte[] data = Msg.getPackage(msg);
        if (data == null) {
            return;
        }
        if (data.length > maxPackagSize + 1024) {
            S.e("send err:message is too long(max:" + (maxPackagSize + 1024) + "):" + data.length);
            return;
        }

        //注册一个发送时间,用来判断下一次发送的时机
        msg.timeSend = S.currentTimeMillis();
//        S.s("正在发送msg:" + msg.id);

        try {
            if (S.isIp(msg.ip)) {
//                        S.s("正在向[" + msg.ip + "]发送-------------------------->");
                Client client = clients.get(msg.ip);
                if (client != null) {
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(client.ip), port);
                    datagramSocket.send(datagramPacket);
                }
//                        S.s("发送成功");
            } else {
                for (Client client : clients.values()) {
//                            S.s("正在向[" + client.ip + "]发送:" + data.length + "============================>");
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(client.ip), port);
                    datagramSocket.send(datagramPacket);
                }
//                        S.s("发送成功");
            }
            if (callBack != null) {
                callBack.whenSend();
            }
            if (msg.zThread != null) {
                msg.zThread.resumeThread();
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            S.e(e1);
        } catch (IOException e) {
            e.printStackTrace();
            S.e(e);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CallBack {
        void whenSend();
    }
}
