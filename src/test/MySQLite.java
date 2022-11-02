package test;

import com.formdev.flatlaf.FlatLightLaf;
import test.pattern.PatternUtils;
import test.pattern.TPatternGroup;
import test.pattern.TPatternItem;
import test.xlog.XlogFileDecoder;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * created by zhaoyuntao
 * on 24/11/2021
 * description:
 */
public class MySQLite {
    public static final String BETA = "im.thebot.messenger.beta";
    public static final String PRODUCT = "im.thebot.messenger";
    public static final Pair<String, Color> RED = new Pair<>("RED", new Color(255, 50, 50));
    public static final Pair<String, Color> YELLOW = new Pair<>("YELLOW", new Color(255, 255, 150));
    public static final Pair<String, Color> GREEN = new Pair<>("GREEN", new Color(150, 255, 150));
    public static final Pair<String, Color> CYAN = new Pair<>("CYAN", Color.CYAN);
    public static final Pair<String, Color> BLUE = new Pair<>("BLUE", new Color(150, 150, 255));
    public static final Pair<String, Color> WHITE = new Pair<>("WHITE", Color.WHITE);
    public static final Pair<String, Color> GRAY = new Pair<>("GRAY", new Color(120, 120, 120));
    private static final String TAG = "abcdefghijk";
    private final JTextPane textViewTop;
    private final Checkbox checkboxJson;
    private final Checkbox checkboxBlob;
    private final Checkbox checkboxColor;
    private String packageName = BETA;

    private String adbPath = "/opt/homebrew/bin/";
    private String lastSql;
    private int columnLength = 100;
    private HintTextField textViewSql;
    private final List<String> commands = new ArrayList<>();
    private int commandIndex;
    private boolean selectJson, selectBlob, selectColor;

    public MySQLite() {
        readSettings();
        FlatLightLaf.setup();

        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        JFrame frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;//得到宽
        int height = screenSize.height;//得到高
        int wFrame = Math.min(1400, (int) (width * 0.9f));
        int hFrame = Math.min(1000, (int) (height * 0.7f));
        int x = (int) ((width - wFrame) / 2f);
        int y = (int) ((height - hFrame) / 2f);
        frame.setBounds(x, y, wFrame, hFrame);
        frame.setMinimumSize(new Dimension(wFrame, hFrame));
        frame.setTitle("SQL Executor");
        frame.setLayout(new LinearLayout(LinearLayout.Orientation.VERTICAL));

        textViewTop = new JTextPane();
        textViewTop.setEditable(false);
        textViewTop.setFont(new Font(null, 0, 10));
        textViewTop.setBackground(Color.black);
        textViewTop.setForeground(Color.white);
        textViewTop.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        parseXlog(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JScrollPane resultScrollPane = new JScrollPane(textViewTop);
        resultScrollPane.setBackground(null);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        textViewSql = new HintTextField("Input your sql here");
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
                if (isNumber) {
                    super.insertString(offs, str, a);
                    try {
                        columnLength = Integer.parseInt(textViewMaxLength.getText());
                    } catch (Exception ignore) {
                    }
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                try {
                    columnLength = Integer.parseInt(textViewMaxLength.getText());
                } catch (Exception ignore) {
                }
            }
        });
        textViewMaxLength.setText(String.valueOf(columnLength));

        final HintTextField textViewADBPath = new HintTextField("adb path");
        textViewADBPath.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offs, str, a);
                adbPath = textViewADBPath.getText();
                S.s("adb:" + adbPath);
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                adbPath = textViewADBPath.getText();
                S.s("adb:" + adbPath);
            }
        });
        textViewADBPath.setText(adbPath);

        checkboxJson = new Checkbox();
        checkboxJson.setLabel("Format json");
        checkboxJson.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                selectJson = checkboxJson.getState();
            }
        });
        checkboxJson.setState(selectJson);

        checkboxBlob = new Checkbox();
        checkboxBlob.setLabel("Format blob");
        checkboxBlob.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                selectBlob = checkboxBlob.getState();
            }
        });
        checkboxBlob.setState(selectBlob);

        checkboxColor = new Checkbox();
        checkboxColor.setLabel("Color");
        checkboxColor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                selectColor = checkboxColor.getState();
            }
        });
        checkboxColor.setState(selectColor);
        textViewSql.addKeyListener(new KeyListener() {


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
                    clearLog();
                } else if ("quit".equalsIgnoreCase(text) || "exit".equalsIgnoreCase(text)) {
                    System.exit(0);
                } else {
                    if (commands.size() == 0 || !commands.get(commands.size() - 1).equals(text)) {
                        lastSql = text;
                        commands.add(text);
                    }
                    commandIndex = commands.size();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("adb shell am start-foreground-service -n " + packageName + "/com.sdk.chat.test.ChatDebugService --ez format_ false");
                    if (selectJson) {
                        stringBuilder.append(" --ez formatJson true");
                    }
                    if (selectBlob) {
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

        final JRadioButton radioBeta = new JRadioButton("beta", true);
        radioBeta.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent changeEvent) {
                if (radioBeta.isSelected()) {
                    packageName = BETA;
                }
            }
        });
        final JRadioButton radioProduct = new JRadioButton("product", false);
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
        if (PRODUCT.equals(packageName)) {
            radioProduct.setSelected(true);
        } else if (BETA.equals(packageName)) {
            radioBeta.setSelected(true);
        }

        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(new Color(220, 220, 240));
        radioPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        radioPanel.setLayout(new LinearLayout(LinearLayout.Orientation.HORIZONTAL));

        radioBeta.setPreferredSize(new LinearLayout.LayoutParams(1, true, 1, true));
        radioPanel.add(radioBeta);
        radioProduct.setPreferredSize(new LinearLayout.LayoutParams(1, true, 1, true));
        radioPanel.add(radioProduct);

        //----------------------------------------------------------------------------------------------
        textViewADBPath.setPreferredSize(new LinearLayout.LayoutParams(300, false, 1, true, (byte) 12, (byte) 10));
        paramPanel.add(textViewADBPath);

        checkboxJson.setPreferredSize(new LinearLayout.LayoutParams(100, false, 1, true));
        paramPanel.add(checkboxJson);
        checkboxBlob.setPreferredSize(new LinearLayout.LayoutParams(100, false, 1, true));
        paramPanel.add(checkboxBlob);
        checkboxColor.setPreferredSize(new LinearLayout.LayoutParams(100, false, 1, true));
        paramPanel.add(checkboxColor);

        textViewMaxLength.setPreferredSize(new LinearLayout.LayoutParams(150, false, 1, true, (byte) 10, (byte) 10));
        paramPanel.add(textViewMaxLength);

        radioPanel.setPreferredSize(new LinearLayout.LayoutParams(200, false, 1, true, (byte) 10, (byte) 10));
        paramPanel.add(radioPanel);

        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new LinearLayout.LayoutParams(80, false, 1, true, (byte) 10, (byte) 10));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        paramPanel.add(clearButton);

        //---------------------------------------------------------------------------------------------------------
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
                        Process process = exec("adb shell logcat -v raw -s " + TAG + ":I", false);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            appendToPane(line, WHITE);
                            textViewTop.setCaretPosition(textViewTop.getDocument().getLength());
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } catch (IOException e) {
                        S.e(e);
                        appendToPane(e.getMessage(), RED);
                    }
                }
            }
        }).start();
        addStyle();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                writeSettings();
            }
        });
    }

    private void writeSettings() {
        Map<String, String> settings = new HashMap<>();
        settings.put(FileUtil.KEY_ADB_PATH, adbPath);
        settings.put(FileUtil.KEY_LAST_SQL, lastSql);
        settings.put(FileUtil.KEY_LAST_COLUMN_LENGTH, String.valueOf(columnLength));
        settings.put(FileUtil.KEY_PACKAGE_NAME, packageName);

        settings.put(FileUtil.KEY_SELECT_JSON, selectJson ? "true" : "false");
        settings.put(FileUtil.KEY_SELECT_BLOB, selectBlob ? "true" : "false");
        settings.put(FileUtil.KEY_SELECT_COLOR, selectColor ? "true" : "false");
        FileUtil.write(settings);
    }

    private void readSettings() {
        Map<String, String> tmp = FileUtil.read();
        String adbPathTmp = tmp.get(FileUtil.KEY_ADB_PATH);
        if (!FileUtil.isEmpty(adbPathTmp)) {
            adbPath = adbPathTmp;
            S.s("adbPath:" + adbPath);
        }
        String sqlTmp = tmp.get(FileUtil.KEY_LAST_SQL);
        if (!FileUtil.isEmpty(sqlTmp)) {
            lastSql = sqlTmp;
            commands.add(lastSql);
            commandIndex = commands.size();
            S.s("lastSql:" + lastSql);
        }
        String environmentTmp = tmp.get(FileUtil.KEY_PACKAGE_NAME);
        if (!FileUtil.isEmpty(environmentTmp)) {
            packageName = environmentTmp;
            S.s("packageName:" + packageName);
        }

        String selectJsonTmp = tmp.get(FileUtil.KEY_SELECT_JSON);
        selectJson = selectJsonTmp != null && selectJsonTmp.equals("true");

        String selectBlobTmp = tmp.get(FileUtil.KEY_SELECT_BLOB);
        selectBlob = selectBlobTmp != null && selectBlobTmp.equals("true");

        String selectColorTmp = tmp.get(FileUtil.KEY_SELECT_COLOR);
        selectColor = selectColorTmp == null || selectColorTmp.equals("true");

        try {
            String columnLengthString = tmp.get(FileUtil.KEY_LAST_COLUMN_LENGTH);
            int number = Integer.parseInt(columnLengthString);
            if (number > 0) {
                columnLength = number;
                S.s("columnLength:" + columnLength);
            }
        } catch (Throwable e) {
            columnLength = 100;
        }
    }

    private void clearLog() {
        FileUtil.delete();
        exec("adb logcat -c", true);
        textViewTop.setText(null);
    }

    private void addStyle() {
        StyledDocument document = textViewTop.getStyledDocument();
        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        style = document.addStyle(RED.getKey(), style);
        StyleConstants.setForeground(style, RED.getValue());
        style = document.addStyle(YELLOW.getKey(), style);
        StyleConstants.setForeground(style, YELLOW.getValue());
        style = document.addStyle(GREEN.getKey(), style);
        StyleConstants.setForeground(style, GREEN.getValue());
        style = document.addStyle(BLUE.getKey(), style);
        StyleConstants.setForeground(style, BLUE.getValue());
        style = document.addStyle(CYAN.getKey(), style);
        StyleConstants.setForeground(style, CYAN.getValue());
        style = document.addStyle(WHITE.getKey(), style);
        StyleConstants.setForeground(style, WHITE.getValue());
        style = document.addStyle(GRAY.getKey(), style);
        StyleConstants.setForeground(style, GRAY.getValue());
    }

    private void appendToPane(String msg, Pair<String, Color> pair) {
        StyledDocument document = textViewTop.getStyledDocument();
        try {
            document.insertString(document.getLength(), "\n", document.getStyle(WHITE.getKey()));
            boolean hasTag = msg.startsWith(TAG);
            if (hasTag) {
                msg = msg.substring(TAG.length());
            }
            if (selectColor && hasTag) {
                try {
                    List<TPatternGroup> patternGroups = PatternUtils.match("(\\(\\d+\\))(.*)", msg);
                    if (patternGroups != null && patternGroups.size() > 0) {
                        TPatternGroup group = patternGroups.get(0);
                        if (group != null && group.size() >= 3) {
                            TPatternItem head = group.getIndex(1);
                            document.insertString(document.getLength(), head.getContent(), document.getStyle(YELLOW.getKey()));
                            String body = group.getIndex(2).getContent();
                            List<TPatternGroup> columnsGroup = PatternUtils.match("\\[(.+?):(.+?)\\]", body);
                            int startPosition = 0;
                            if (columnsGroup != null && !columnsGroup.isEmpty()) {
                                for (TPatternGroup column : columnsGroup) {
                                    S.s("start position:" + startPosition + "  current start:" + column.getStart());
                                    if (startPosition != column.getStart()) {
                                        S.s("!=, insert origin:[" + body.substring(startPosition, column.getStart()) + "]");
                                        document.insertString(document.getLength(), body.substring(startPosition, column.getStart()), document.getStyle(BLUE.getKey()));
                                    }
                                    startPosition = column.getEnd();
                                    S.s("final start:" + startPosition);
                                    S.s("----------------------------------------------------------------------");
                                    if (column.size() >= 3) {
                                        String key = column.getIndex(1).getContent().trim();
                                        String value = column.getIndex(2).getContent().trim();
                                        document.insertString(document.getLength(), " [", document.getStyle(BLUE.getKey()));
                                        document.insertString(document.getLength(), key, document.getStyle(BLUE.getKey()));
                                        document.insertString(document.getLength(), ":", document.getStyle(pair.getKey()));
                                        document.insertString(document.getLength(), value, document.getStyle(pair.getKey()));
                                        document.insertString(document.getLength(), "]", document.getStyle(BLUE.getKey()));
                                    } else {
                                        document.insertString(document.getLength(), column.getContent(), document.getStyle(BLUE.getKey()));
                                    }
                                }
                                if (startPosition != body.length()) {
                                    S.s("!=, insert origin:[" + body.substring(startPosition) + "]");
                                    document.insertString(document.getLength(), body.substring(startPosition), document.getStyle(pair.getKey()));
                                }
                                return;
                            }
                        }
                    }
                    List<TPatternGroup> table = PatternUtils.match("Table name:\\s*\\[(.*)\\]\\s*columns:\\s*(.*)", msg);
                    if (table != null && table.size() > 0) {
                        TPatternGroup group = table.get(0);
                        if (group != null && group.size() >= 3) {
                            TPatternItem tableName = group.getIndex(1);
                            document.insertString(document.getLength(), "Table:[", document.getStyle(YELLOW.getKey()));
                            document.insertString(document.getLength(), tableName.getContent(), document.getStyle(BLUE.getKey()));
                            document.insertString(document.getLength(), "]  Columns:", document.getStyle(YELLOW.getKey()));
                            List<TPatternGroup> columns = PatternUtils.match("\\[(.+?)\\]", group.getIndex(2).getContent());
                            if (columns != null && !columns.isEmpty()) {
                                for (TPatternGroup column : columns) {
                                    if (column.size() >= 2) {
                                        String columnInfo = column.getIndex(1).getContent().trim();
                                        document.insertString(document.getLength(), "  [", document.getStyle(YELLOW.getKey()));
                                        document.insertString(document.getLength(), columnInfo, document.getStyle(BLUE.getKey()));
                                        document.insertString(document.getLength(), "]", document.getStyle(YELLOW.getKey()));
                                    }
                                }
                            }
                            return;
                        }
                    }
                    if (PatternUtils.matched("[-]{10,}", msg)) {
                        document.insertString(document.getLength(), msg, document.getStyle(GRAY.getKey()));
                        return;
                    }
                    if (PatternUtils.matched(".*Exception.*", msg)) {
                        document.insertString(document.getLength(), msg, document.getStyle(RED.getKey()));
                        return;
                    }
                    if (PatternUtils.matched("Starting\\s+service.*", msg)) {
                        document.insertString(document.getLength(), msg, document.getStyle(GREEN.getKey()));
                        return;
                    }
                } catch (Throwable ignore) {
                }
            }
            document.insertString(document.getLength(), msg, document.getStyle(pair.getKey()));
        } catch (Throwable ignore) {
        }
    }

    public Process exec(String cmd, boolean print) {
        try {
            Process process = Runtime.getRuntime().exec(adbPath + cmd);
            if (print) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    appendToPane(line, WHITE);
                    textViewTop.setCaretPosition(textViewTop.getDocument().getLength());
                }
            }
            return process;
        } catch (IOException e) {
            appendToPane(e.getMessage(), RED);
            return null;
        }
    }

    private void parseXlog(File sourceFile) {
        appendToPane("start parse xlog: " + sourceFile.getAbsolutePath(), BLUE);
        String fileName = sourceFile.getName().replaceAll("\\.xlog", "");
        final File destFile = new File(sourceFile.getParent(), fileName + "_decode.log");
        XlogFileDecoder.ParseFile(sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), new XlogFileDecoder.Callback() {
            @Override
            public void onSuccess() {
                appendToPane("finish, save to :" + destFile.getAbsolutePath(), GREEN);
            }

            @Override
            public void onFailed(String errorMessage) {
                appendToPane("failed: " + errorMessage, RED);
            }
        });
    }

    public static void main(String[] args) {
        new MySQLite();
    }
}
