/**
 * Created by zhaoyuntao on 2019-4-29.
 */

import java.awt.*;

/**
 * VerticalFlowLayout is similar to FlowLayout except it lays out components
 * vertically. Extends FlowLayout because it mimics much of the behavior of the
 * FlowLayout class, except vertically. An additional feature is that you can
 * specify a fill to edge flag, which causes the VerticalFlowLayout manager to
 * resize all components to expand to the column width Warning: This causes
 * problems when the main panel has less space that it needs and it seems to
 * prohibit multi-column output. Additionally there is a vertical fill flag,
 * which fills the last component to the remaining height of the container.
 */
public class LinearLayout extends FlowLayout {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -7411804673224730901L;
    public static int MASK_WEIGHT = 1 << 30;
    @Orientation
    private final int orientation;

    public LinearLayout(@Orientation int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int childrenCount = parent.getComponentCount();

            int w = 0;
            int h = 0;
            for (int i = 0; i < childrenCount; i++) {
                Component child = parent.getComponent(i);
                Dimension dimension = child.getPreferredSize();
                if (orientation == Orientation.VERTICAL) {
                    S.s("isWeight:" + isWeight(dimension.width));
                    w = Math.max(w, isWeight(dimension.width) ? 0 : dimension.width);
                    h += (isWeight(dimension.height) ? 0 : dimension.height);
                } else {
                    h = Math.max(h, isWeight(dimension.height) ? 0 : dimension.height);
                    w += (isWeight(dimension.width) ? 0 : dimension.width);
                }
            }
            return new Dimension(insets.left + insets.right + w, insets.top + insets.bottom + h);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(100, 100);
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int childCount = parent.getComponentCount();
            if (childCount == 0) {
                return;
            }
            boolean horizontal = orientation == Orientation.HORIZONTAL;
            float weightSum = 0;
            int widthMax = parent.getWidth() - insets.left - insets.right;
            int heightMax = parent.getHeight() - insets.top - insets.bottom;
            S.s("w:" + widthMax + " " + heightMax);
            for (int i = 0; i < childCount; i++) {
                Component child = parent.getComponent(i);
                Dimension dimension = child.getPreferredSize();
                if (horizontal) {
                    if (isWeight(dimension.width)) {
                        weightSum += getWeight(dimension.width);
                    } else {
                        widthMax -= dimension.getWidth();
                    }
                } else {
                    if (isWeight(dimension.height)) {
                        weightSum += getWeight(dimension.height);
                    } else {
                        heightMax -= dimension.getHeight();
                    }
                }
            }

            int x = insets.left;
            int y = insets.top;
            for (int i = 0; i < childCount; i++) {
                Component child = parent.getComponent(i);
                Dimension dimension = child.getPreferredSize();
                if (horizontal) {
                    int widthChild = (isWeight(dimension.width) && weightSum > 0) ? (int) (getWeight(dimension.width) / weightSum * widthMax) : dimension.width;
                    int heightChild = isWeight(dimension.height) ? heightMax : dimension.height;
                    child.setBounds(x, y, widthChild, heightChild);
                    x += child.getWidth();
                } else {
                    int widthChild = isWeight(dimension.width) ? widthMax : dimension.width;
                    int heightChild = (isWeight(dimension.height) && weightSum > 0) ? (int) (getWeight(dimension.height) / weightSum * heightMax) : dimension.height;
                    child.setBounds(x, y, widthChild, heightChild);
                    y += child.getHeight();
                }
            }
        }
    }

    private boolean isWeight(int size) {
        return (MASK_WEIGHT & size) == MASK_WEIGHT;
    }

    private int getWeight(int size) {
        return MASK_WEIGHT ^ size;
    }

    public @interface Orientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    public static class LayoutParams extends Dimension {

        public LayoutParams(int height, boolean weight) {
            this(0, true, height, weight);
        }

        public LayoutParams(int width, boolean weightWidth, int height, boolean weightHeight) {
            super(weightWidth ? width | MASK_WEIGHT : width, weightHeight ? height | MASK_WEIGHT : height);
        }
    }
}