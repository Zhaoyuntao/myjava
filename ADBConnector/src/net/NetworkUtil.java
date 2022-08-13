package net;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * 网络信息
 *
 * @author yjzhao
 */
public class NetworkUtil {

    public static final String NET_UNKNOWN = "none";

    // wifi, cmwap, ctwap, uniwap, cmnet, uninet, ctnet,3gnet,3gwap
    // 其中3gwap映射为uniwap
    public static final String NET_WIFI = "wifi";
    public static final String NET_CMWAP = "cmwap";
    public static final String NET_UNIWAP = "uniwap";
    public static final String NET_CTWAP = "ctwap";
    public static final String NET_CTNET = "ctnet";


    /**
     * IP转整型
     *
     * @param ip
     * @return
     */
    public static long ip2int(String ip) {
        String[] items = ip.split("\\.");
        return Long.valueOf(items[0]) << 24 | Long.valueOf(items[1]) << 16 | Long.valueOf(items[2]) << 8 | Long.valueOf(items[3]);
    }

    public static String getIP() {
        try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

}
