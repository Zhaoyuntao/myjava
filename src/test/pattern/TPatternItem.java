package test.pattern;

/**
 * created by zhaoyuntao
 * on 2020/8/25
 * description:
 */
public class TPatternItem {
    private int start;
    private int end;
    private int index;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return "\n{" +
                "start=" + start +
                ", end=" + end +
                ", index=" + index +
                ", content='" + content + '\'' +
                '}';
    }
}
