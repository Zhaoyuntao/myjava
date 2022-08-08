package test.pattern;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * created by zhaoyuntao
 * on 2020/8/25
 * description:
 */
public class TPatternGroup {
    private final Map<Integer, TPatternItem> groupItemBeans;
    private int start;
    private int end;
    private int index;
    private String content;
    private String patternString;

    public TPatternGroup(int groupCount) {
        groupItemBeans = new LinkedHashMap<>(groupCount);
    }

    public void addItem(TPatternItem item) {
        this.groupItemBeans.put(item.getIndex(), item);
    }

    public void clearItems() {
        this.groupItemBeans.clear();
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPatternString() {
        return patternString;
    }

    public void setPatternString(String patternString) {
        this.patternString = patternString;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int length() {
        if (content == null) {
            return 0;
        }
        return content.length();
    }

    public boolean isEmpty() {
        return groupItemBeans.size() <= 0;
    }

    public void iterate(ValueSafeTransfer.ElementIterator<TPatternItem> iterator) {
        ValueSafeTransfer.iterate(groupItemBeans.values(), iterator);
    }

    public TPatternItem getIndex(int index) {
        return groupItemBeans.get(index);
    }

    public int size() {
        return groupItemBeans.size();
    }
}
