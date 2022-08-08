package test.pattern;

import test.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by zhaoyuntao
 * on 2020/8/25
 * description:
 * https://meetings.a.us/#/j/989048337/2622d0ab46deddb327df7291391afe503300d8d13326ba49
 * old:
 * https://intl.meeting.huaweicloud.com/#/j/289229034/6401c55f8d3c90b5b727fd58c9e19e571f4bae832fc60e0c
 */
public class PatternUtils {
    /**
     * Email.
     */
    public static final String PATTERN_EMAIL = "([a-z0-9A-Z-_\\.]+@[a-zA-Z0-9]+\\.[a-zA-Z]+)";
    /**
     * Phone number.
     */
    public static final String PATTERN_PHONE_NUMBER = "((?<!(http:|\\d))([0-9]{1,3}[ \\-])?[0-9]{5,15}(?!( ?\\d)))";
    public static final String PATTERN_MENTION = "(@\\u2068\\d+?\\u2068)";
    private static final String WEB_URL_PREFIX = "((?i)(https?|ftp|git|afp|telnet|smb)://)";
    private static final String WEB_URL_SUFFIX = "(\\.(?i)(cn|com|ae|ar|ai|us|ch|ca|br|es|xyz|net|top|tech|org|gov|edu|ink|int|mil|pub|mob|tv|cc|biz|red|coop|aero|io))";
    private static final String WEB_URL_DOMAIN_NAME = "[a-zA-Z0-9_\\-]";
    private static final String WEB_URL_PARAM = "[a-zA-Z0-9_\\-+=&?!@#$%^*():：'\";.]";

    public static final String WEB_URL = "(?<![@A-Za-z0-9.])(" +
            "(" + WEB_URL_PREFIX + "?" + WEB_URL_DOMAIN_NAME + "+(\\." + WEB_URL_DOMAIN_NAME + "{2,})+" + WEB_URL_SUFFIX + "*(/" + WEB_URL_PARAM + "*)*(\\?" + WEB_URL_PARAM + "*)?)" +
            "|(" + WEB_URL_DOMAIN_NAME + "+(\\." + WEB_URL_DOMAIN_NAME + "{2,})*\\." + WEB_URL_SUFFIX + ")" +
            "|(" + WEB_URL_PREFIX + WEB_URL_DOMAIN_NAME + "+(\\." + WEB_URL_DOMAIN_NAME + "{2,})+)" +
            ")(?![@A-Za-z0-9.])";
//    Pattern p = Pattern.compile(WEB_URL);


    /**
     * Parent group at 0,child group after parent group.
     */
    public static List<TPatternGroup> match(String patternString, CharSequence text) {
        return match(patternString, text, 1000);
    }

    public static List<TPatternGroup> match(String patternString, CharSequence text, int maxLength) {
        if (TextUtils.isEmpty(patternString) || TextUtils.isEmpty(text) || text.length() < 3 || text.length() > Math.max(maxLength, 1000)) {
            return null;
        }
        List<TPatternGroup> patternBeans = new ArrayList<>();
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        int index = 0;
        while (matcher.find()) {
            int groupCount = matcher.groupCount() + 1;
            TPatternGroup TPatternGroup = new TPatternGroup(groupCount);
            TPatternGroup.setPatternString(patternString);
            //Parent group.
            TPatternGroup.setContent(matcher.group(0));
            TPatternGroup.setEnd(matcher.end());
            TPatternGroup.setIndex(index++);
            TPatternGroup.setStart(matcher.start());
            //Child group.
            for (int i = 0; i < groupCount; i++) {
                String group = matcher.group(i);
                int start = matcher.start(i);
                int end = matcher.end(i);
                if (start < 0) {
                    continue;
                }
                TPatternItem childPatternBean = new TPatternItem();
                childPatternBean.setContent(group);
                childPatternBean.setEnd(end);
                childPatternBean.setStart(start);
                childPatternBean.setIndex(i);
                TPatternGroup.addItem(childPatternBean);
            }
            patternBeans.add(TPatternGroup);
        }
        return patternBeans;
    }

    public static List<TPatternGroup> matcher(String patternString, String text) {
        if (matched(patternString, text)) {
            return match(patternString, text);
        } else {
            return null;
        }
    }

    public static boolean matched(String patternString, String text) {
        if (patternString == null || text == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(patternString);
        return pattern.matcher(text).matches();
    }

    public static void log(List<TPatternGroup> groups) {
        if (groups != null && groups.size() > 0) {
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                TPatternGroup groupBean = groups.get(groupIndex);
                S.s("------[" + groupIndex + "] group[" + groupBean.getIndex() + "][" + groupBean.getContent() + "][child count:" + groups.size() + "][pattern:" + groupBean.getPatternString() + "]---------------------------");
                log(groupBean);
            }
        }
    }

    public static void log(TPatternGroup groupBean) {
        if (!groupBean.isEmpty()) {
            groupBean.iterate(new ValueSafeTransfer.ElementIterator<TPatternItem>() {
                @Override
                public TPatternItem element(int position, TPatternItem itemBean) {
                    if (itemBean != null) {
                        S.s("[" + position + "] item[" + itemBean.getIndex() + "]:[" + itemBean.getContent() + "]");
                    }
                    return null;
                }
            });
        }
    }
}
