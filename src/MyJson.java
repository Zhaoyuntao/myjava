import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;


/**
 * created by zhaoyuntao
 * on 24/11/2021
 * description:
 */
public class MyJson {
    static DocumentListener listener;

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setBackground(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;//得到宽
        int height = screenSize.height;//得到高
        S.s("w:" + width + " h:" + height);
        int wFrame = (int) (width * 0.7f);
        int hFrame = (int) (height * 0.7f);
        int x = (int) ((width - wFrame) / 2f);
        int y = (int) ((height - hFrame) / 2f);
        frame.setBounds(x, y, wFrame, hFrame);
        frame.setMinimumSize(new Dimension(wFrame, hFrame));
        frame.setTitle("Json Formatter");
        frame.setLayout(new GridLayout(1, 2));

        final JTextArea label = new JTextArea();
        label.setLineWrap(true);
        label.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane jScrollPane = new JScrollPane(label);
        jScrollPane.getVerticalScrollBar().setBackground(null);
        jScrollPane.setBackground(null);
        final JTextArea label2 = new JTextArea();
        label2.setLineWrap(true);
        label2.setEditable(false);
        label2.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane jScrollPane2 = new JScrollPane(label2);
        jScrollPane2.setBackground(null);
        listener = new DocumentListener() {
            private void formatJson(final JTextArea label) {
                final String text = label.getText();
//                label2.setText("Hello"+new Random().nextInt(100));
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    label2.setForeground(Color.BLACK);
                    label2.setText(jsonObject.toString(6));
                } catch (Exception e) {
                    label2.setForeground(Color.RED);
                    label2.setText(e.toString());
                }
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                formatJson(label);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                formatJson(label);
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        };
        label.getDocument().addDocumentListener(listener);
//        jScrollPane.setSize(wFrame, hFrame);
//        jScrollPane2.setSize(wFrame, hFrame);
        frame.getContentPane().add(jScrollPane);
        frame.getContentPane().add(jScrollPane2);

        // 显示窗口
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
