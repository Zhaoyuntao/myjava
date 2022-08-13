package net;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class Receiver extends ZThread {
    private DatagramSocket datagramSocket;
    private CallBack callBack;
    private int port;

    public Receiver(int port,CallBack callBack) {
        this.callBack = callBack;
        this.port = port;
    }

    @Override
    protected void init() {
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            S.e(e);
        }
    }

    @Override
    protected void todo() {
        byte[] buffer = new byte[65507];
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (packet == null) {
            return;
        }
        if (packet.getLength() < 1) {
            return;
        }
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        InetAddress inetAddress = packet.getAddress();
        if (inetAddress == null) {
            return;
        }
        final String ip = inetAddress.getHostAddress();
        if(callBack!=null){
            String ip_self = callBack.getIp();
            if (ip.equals(ip_self)) {
//                    S.e("自身的消息,略过");
                return;
            }
        }
        //解包
        final Msg msg = Msg.releasePackage(data);
        msg.ip = ip;
        //response不需要回复
        if (msg.type == Msg.RESPONSE) {
            if (callBack != null) {
                callBack.whenGotResponse(msg.id);
            }
        }else{
            //除了心跳和文件块以外, 其他消息需要回复
            if(msg.type!=Msg.HEARTBIT&&msg.type!=Msg.FILE_PIECE){
                //回复
                Msg response = new Msg(msg.id);
                response.type = Msg.RESPONSE;
                if (callBack != null) {
                    Sender sender = callBack.getSender();
                    if(sender!=null){
                        sender.send(response);
                    }
                }
            }

            if (callBack != null) {
                callBack.whenGotMsg(msg);
            }
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
        void whenGotMsg(Msg msg);

        void whenGotResponse(String id);

        Sender getSender();

        String getIp();

    }
}
