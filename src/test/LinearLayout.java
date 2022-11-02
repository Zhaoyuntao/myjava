package test;

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
    public static int MASK_MARGIN = 0xff;
    public static int MASK_SIZE = 0xfff;
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
                int marginStart = (dimension.width >> 12) & MASK_MARGIN;
                int marginEnd = (dimension.width) >> 20 & MASK_MARGIN;
                int childWidth = dimension.width & MASK_SIZE;
                int childHeight = dimension.height & MASK_SIZE;
                boolean isWeightWidth = isWeight(dimension.width);
                boolean isWeightHeight = isWeight(dimension.height);
                int childWidthWithMargin = (isWeightWidth ? 0 : childWidth) + marginStart + marginEnd;
                if (orientation == Orientation.VERTICAL) {
                    w = Math.max(w, childWidthWithMargin);
                    h += (isWeightHeight ? 0 : childHeight);
                } else {
                    h = Math.max(h, isWeightHeight ? 0 : childHeight);
                    w += childWidthWithMargin;
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
            for (int i = 0; i < childCount; i++) {
                Component child = parent.getComponent(i);
                Dimension dimension = child.getPreferredSize();
                int childWidth = dimension.width & MASK_SIZE;
                int childHeight = dimension.height & MASK_SIZE;
                boolean isWeightWidth = isWeight(dimension.width);
                boolean isWeightHeight = isWeight(dimension.height);

                if (horizontal) {
                    if (isWeightWidth) {
                        weightSum += childWidth;
                    } else {
                        widthMax -= childWidth;
                    }
                } else {
                    if (isWeightHeight) {
                        weightSum += childHeight;
                    } else {
                        heightMax -= childHeight;
                    }
                }
            }

            int x = insets.left;
            int y = insets.top;
            for (int i = 0; i < childCount; i++) {
                Component child = parent.getComponent(i);
                Dimension dimension = child.getPreferredSize();
                int marginStart = (dimension.width >> 12) & MASK_MARGIN;
                int marginEnd = (dimension.width) >> 20 & MASK_MARGIN;
                int childWidth = dimension.width & MASK_SIZE;
                int childHeight = dimension.height & MASK_SIZE;
                boolean isWeightWidth = isWeight(dimension.width);
                boolean isWeightHeight = isWeight(dimension.height);
//                S.s("w[" + isWeightWidth + "]: " + childWidth + "   h[" + isWeightHeight + "]: " + childHeight + "  marginStart:" + marginStart + " marginEnd:" + marginEnd);
                if (horizontal) {
                    if (isWeightWidth) {
                        int childWidthWeight = childWidth;
                        childWidth = (int) (childWidthWeight / weightSum * widthMax);
                    }
                    if (isWeightHeight) {
                        childHeight = heightMax;
                    }
                    child.setBounds(x + marginStart, y, childWidth, childHeight);
                    x += childWidth + marginStart + marginEnd;
                } else {
                    if (isWeightWidth) {
                        childWidth = widthMax;
                    }
                    if (isWeightHeight) {
                        childHeight = (int) (childHeight / weightSum * heightMax);
                    }
                    child.setBounds(x + marginStart, y, childWidth, childHeight);
                    y += childHeight;
                }
            }
        }
    }

    private boolean isWeight(int size) {
        return (MASK_WEIGHT & size) == MASK_WEIGHT;
    }

    public @interface Orientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    public static class LayoutParams extends Dimension {

        public int marginStart;
        public int marginEnd;

        public LayoutParams(int height, boolean weight) {
            this((short) 1, true, height, weight);
        }

        public LayoutParams(int width, boolean weightWidth, int height, boolean weightHeight) {
            this(width, weightWidth, height, weightHeight, (byte) 0, (byte) 0);
        }

        public LayoutParams(int width, boolean weightWidth, int height, boolean weightHeight, byte marginStart, byte marginEnd) {
            super((weightWidth ? width | MASK_WEIGHT : width) | marginStart << 12 | marginEnd << 20, (weightHeight ? height | MASK_WEIGHT : height));
            this.marginStart = marginStart;
            this.marginEnd = marginEnd;
        }

        @Override
        public double getWidth() {
            return super.getWidth();
        }

        @Override
        public double getHeight() {
            return super.getHeight();
        }
    }
}