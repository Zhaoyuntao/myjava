import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * created by zhaoyuntao
 * on 24/11/2021
 * description:
 */
public class MySQLite {
    public static final String BETA = "im.thebot.messenger.beta";
    public static final String PRODUCT = "im.thebot.messenger";
    private final JTextArea textViewTop;
    private String packageName = BETA;

    public MySQLite() {
        FlatLightLaf.setup();
        List<String> commands = new ArrayList<>();
        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        JFrame frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;//得到宽
        int height = screenSize.height;//得到高
        S.s("w:" + width + " h:" + height);
        int wFrame = Math.min(1400, (int) (width * 0.9f));
        int hFrame = Math.min(1000, (int) (height * 0.7f));
        int x = (int) ((width - wFrame) / 2f);
        int y = (int) ((height - hFrame) / 2f);
        frame.setBounds(x, y, wFrame, hFrame);
        frame.setMinimumSize(new Dimension(wFrame, hFrame));
        frame.setTitle("SQL Executor");
        frame.setLayout(new LinearLayout(LinearLayout.Orientation.VERTICAL));

        textViewTop = new JTextArea();
        textViewTop.setEditable(false);
        textViewTop.setLineWrap(true);
        textViewTop.setFont(new Font(null, 0, 10));
        textViewTop.setBackground(Color.black);
        textViewTop.setForeground(Color.white);
        JScrollPane resultScrollPane = new JScrollPane(textViewTop);
        resultScrollPane.setBackground(null);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final HintTextField textViewSql = new HintTextField("Input your sql here");
        final HintTextField textViewMaxLength = new HintTextField("Value length limit");
        textViewMaxLength.setBackground(new Color(220, 220, 240));
        textViewMaxLength.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                boolean isNumber = false;
                try {
                    Integer.parseInt(str);
                    isNumber = true;
                } catch (Exception ignore) {
                }
                S.s("is:" + isNumber);
                if (isNumber) {
                    super.insertString(offs, str, a);
                }
            }
        });
        textViewMaxLength.setText("100");

        Checkbox checkboxJson = new Checkbox();
        checkboxJson.setLabel("Format json");
        Checkbox checkboxBlob = new Checkbox();
        checkboxBlob.setLabel("Format blob");
        textViewSql.addKeyListener(new KeyListener() {
            int commandIndex;

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = textViewSql.getText();
                    processText(text);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                    if (commandIndex - 1 < 0) {
                        return;
                    }
                    commandIndex--;
                    textViewSql.setText(commands.get(commandIndex));
                    textViewSql.setCaretPosition(textViewSql.getDocument().getLength());
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.consume();
                    if (commandIndex + 1 > commands.size() - 1) {
                        commandIndex = commands.size();
                        textViewSql.setText(null);
                        return;
                    }
                    commandIndex++;
                    textViewSql.setText(commands.get(commandIndex));
                    textViewSql.setCaretPosition(textViewSql.getDocument().getLength());
                }
            }

            private void processText(String text) {
                if ("clear".equalsIgnoreCase(text) || "c".equals(text)) {
                    exec("adb logcat -c", true);
                    textViewTop.setText(null);
                } else {
                    commands.add(text);
                    commandIndex = commands.size();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("adb shell am start-foreground-service -n " + packageName + "/com.sdk.chat.test.ChatDebugService");
                    if (checkboxJson.getState()) {
                        stringBuilder.append(" --ez formatJson true");
                    }
                    if (checkboxBlob.getState()) {
                        stringBuilder.append(" --ez formatBlob true");
                    }
                    try {
                        int rowMaxLength = Integer.parseInt(textViewMaxLength.getText());
                        if (rowMaxLength > 0) {
                            stringBuilder.append(" --ei rowMaxLength ").append(rowMaxLength);
                        }
                    } catch (NumberFormatException ignore) {
                    }
                    stringBuilder.append(" --es sql ").append("\"").append(text).append("\"");
                    exec(stringBuilder.toString(), true);
                }
                textViewSql.setText(null);
                textViewSql.select(0, 0);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });


        JPanel paramPanel = new JPanel();
        paramPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        paramPanel.setLayout(new LinearLayout(LinearLayout.Orientation.HORIZONTAL));

        JRadioButton radioBeta = new JRadioButton("beta", true);
        radioBeta.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent changeEvent) {
                if (radioBeta.isSelected()) {
                    packageName = BETA;
                }
            }
        });
        JRadioButton radioProduct = new JRadioButton("product", false);
        radioProduct.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent changeEvent) {
                if (radioProduct.isSelected()) {
                    packageName = PRODUCT;
                }
            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(radioBeta);
        group.add(radioProduct);

        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(new Color(220, 220, 240));
        radioPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        radioPanel.setLayout(new LinearLayout(LinearLayout.Orientation.HORIZONTAL));

        radioBeta.setPreferredSize(new LinearLayout.LayoutParams(1, true, 1, true));
        radioPanel.add(radioBeta);
        radioProduct.setPreferredSize(new LinearLayout.LayoutParams(1, true, 1, true));
        radioPanel.add(radioProduct);

        checkboxJson.setPreferredSize(new LinearLayout.LayoutParams(100, false, 1, true));
        paramPanel.add(checkboxJson);
        checkboxBlob.setPreferredSize(new LinearLayout.LayoutParams(100, false, 1, true));
        paramPanel.add(checkboxBlob);
        textViewMaxLength.setPreferredSize(new LinearLayout.LayoutParams(150, false, 1, true));
        paramPanel.add(textViewMaxLength);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new LinearLayout.LayoutParams(10, false, 1, true));
        paramPanel.add(emptyPanel);
        radioPanel.setPreferredSize(new LinearLayout.LayoutParams(200, false, 1, true));
        paramPanel.add(radioPanel);

        resultScrollPane.setPreferredSize(new LinearLayout.LayoutParams(8, true));
        frame.getContentPane().add(resultScrollPane);
        paramPanel.setPreferredSize(new LinearLayout.LayoutParams(40, false));
        frame.getContentPane().add(paramPanel);
        textViewSql.setPreferredSize(new LinearLayout.LayoutParams(40, false));
        frame.getContentPane().add(textViewSql);

        // 显示窗口
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        exec("adb shell logcat -c", true);
                        Process process = exec("adb shell logcat -v raw -s abcdefg:I", false);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            textViewTop.append("\n");
                            textViewTop.append(line);
                            textViewTop.setCaretPosition(textViewTop.getDocument().getLength());
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } catch (IOException e) {
                        S.e(e);
                        textViewTop.setText(e.getMessage());
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new MySQLite();
    }

    public Process exec(String cmd, boolean print) {
        try {
            Process process = Runtime.getRuntime().exec("/usr/local/bin/" + cmd);
            if (print) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    textViewTop.append("\n");
                    textViewTop.append(line);
                    textViewTop.setCaretPosition(textViewTop.getDocument().getLength());
                }
            }
            return process;
        } catch (IOException e) {
            textViewTop.setText(e.getMessage());
            return null;
        }
    }
}
