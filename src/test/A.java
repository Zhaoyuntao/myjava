package test;

import java.util.regex.Pattern;

public class A {
    private static final String WEB_URL_PREFIX = "((?i)(https?|ftp|git|afp|telnet|smb)://)?";
    private static final String WEB_URL_SUFFIX = "(\\.(?i)(cn|com|ae|ar|ai|us|ch|ca|br|es|xyz|net|top|tech|org|gov|edu|ink|int|mil|pub|mob|tv|cc|biz|red|coop|aero|io))?";
    private static final String WEB_URL_DOMAIN_NAME = "[a-zA-Z0-9_\\-.]?";
    private static final String WEB_URL_PATH = "(/[a-zA-Z0-9_\\-])?";
    private static final String WEB_URL_PARAM = "[a-zA-Z0-9_\\-+=&?!@#$%^*():：'\";.]*";

    public static final String WEB_URL = "(?<![@A-Za-z0-9.])(" + WEB_URL_PREFIX + WEB_URL_DOMAIN_NAME + WEB_URL_SUFFIX + WEB_URL_PATH + WEB_URL_PARAM + ")(?![@A-Za-z0-9.])";
    Pattern p = Pattern.compile(WEB_URL);

    public static final String WEB_TELEGRAME = "(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s|\\()@[a-zA-Z\\d_]{1,32}|(^|\\s|\\()#[^0-9][\\w.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)";

    public static void main(String[] args) {
//        String link = "ksajdnandkjsa https://botim.me/mp/b/?app=me.botim.me95e0abeb67f32351 salmdklsadn";
////        S.s("link:" + link);
////        S.s("matched:" + PatternUtils.matched(BPattern.WEB_URL, link));
//        PatternUtils.log(PatternUtils.match(LinkifyPort.WEB_URL_STRING, link));

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                b();
//            }
//        }).start();
//        a();
//        S.s("number:"+0xf);
//        S.s("number:"+0xff);
//        S.s("number:"+0xfff);
//        S.s("number:"+0x0000_0fff);
//        S.s("number:"+Integer.MAX_VALUE);
//        S.s("number:"+0x0fff_0000);
//        S.s("number:"+(1<<30));
//        S.s("number:"+(1<<1));
//        S.s("p:"+Math.pow(2,8));
        int w = 1000;
        byte ms = 30;
        byte me = 30;
        int mask_ms = 0xff << 16;
        int mask_me = 0xff << 24;
        S.s("0xff: " + 0xff);
        S.s("0xff<<16: " + (0xff << 16));
        S.s("ms<<16: " + (ms << 16));
        S.s("mask_ms: " + mask_ms);
        int all = w | ms << 16 | me << 24;
        S.s("" + ((all & mask_ms) >> 16));
        S.s("" + (mask_me & all >> 24));
    }

    public static synchronized void a() {
        S.s("a");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void b() {
        synchronized (A.class) {

            S.s("b");
        }
    }
}
