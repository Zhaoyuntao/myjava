package test;

import test.pattern.PatternUtils;
import test.pattern.TPatternGroup;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

public class A {
    private static final String WEB_URL_PREFIX = "((?i)(https?|ftp|git|afp|telnet|smb)://)?";
    private static final String WEB_URL_SUFFIX = "(\\.(?i)(cn|com|ae|ar|ai|us|ch|ca|br|es|xyz|net|top|tech|org|gov|edu|ink|int|mil|pub|mob|tv|cc|biz|red|coop|aero|io))?";
    private static final String WEB_URL_DOMAIN_NAME = "[a-zA-Z0-9_\\-.]?";
    private static final String WEB_URL_PATH = "(/[a-zA-Z0-9_\\-])?";
    private static final String WEB_URL_PARAM = "[a-zA-Z0-9_\\-+=&?!@#$%^*():：'\";.]*";

    public static final String WEB_URL = "(?<![@A-Za-z0-9.])(" + WEB_URL_PREFIX + WEB_URL_DOMAIN_NAME + WEB_URL_SUFFIX + WEB_URL_PATH + WEB_URL_PARAM + ")(?![@A-Za-z0-9.])";
    Pattern p = Pattern.compile(WEB_URL);

    public static void main(String[] args) {
//        String link = "askldkasdklsandklsandk https://botim.me/mp/b/?app=me.botim.meet%2Findex.html%3Fseckey%3DdqMAdMih%26cardId%3D86e99c0400db487195e0abeb67f32351 skamdlkasdlkasmd";
//        S.s("link:" + link);
//        S.s("matched:" + PatternUtils.matched(BPattern.WEB_URL, link));
//        PatternUtils.log(PatternUtils.match(BPattern.WEB_URL, link));
        S.s(1<<10);
    }
}
