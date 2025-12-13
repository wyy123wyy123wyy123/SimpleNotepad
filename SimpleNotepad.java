import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleNotepad extends JFrame {
    private JTextArea textArea;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, formatMenu, helpMenu;
    private JMenuItem newItem, openItem, saveItem, saveAsItem, exitItem;
    private JMenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, selectAllItem, findItem;
    private JMenuItem fontItem, fontSizeItem, themeItem;
    private JMenuItem aboutItem;
    private JLabel statusLabel;
    private JToolBar toolBar;
    private JFileChooser fileChooser;
    private File currentFile;
    private UndoManager undoManager;
    
    // 工具栏按钮
    private JButton newButton, openButton, saveButton, cutButton, copyButton, pasteButton, findButton;
    
    private Color lightThemeBg = Color.WHITE;
    private Color lightThemeFg = Color.BLACK;
    private Color darkThemeBg = new Color(30, 30, 30);
    private Color darkThemeFg = new Color(220, 220, 220);
    private boolean darkMode = false;
    
    public SimpleNotepad() {
        setTitle("增强记事本 - 未命名");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 初始化撤销管理器
        undoManager = new UndoManager();
        
        // 初始化组件
        initComponents();
        initListeners();
        
        // 添加到窗口
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        updateStatus();
    }
    
    private void initComponents() {
        // 文本区域
        textArea = new JTextArea();
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        // 将文本区域的Document添加到撤销管理器
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
                updateUndoRedoState();
            }
        });
        
        // 状态栏
        statusLabel = new JLabel(" 就绪 | 行: 1 列: 1 | UTF-8");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        
        // 工具栏
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // 创建工具栏按钮
        newButton = new JButton("新建");
        openButton = new JButton("打开");
        saveButton = new JButton("保存");
        cutButton = new JButton("剪切");
        copyButton = new JButton("复制");
        pasteButton = new JButton("粘贴");
        findButton = new JButton("查找");
        
        // 添加到工具栏
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(cutButton);
        toolBar.add(copyButton);
        toolBar.add(pasteButton);
        toolBar.addSeparator();
        toolBar.add(findButton);
        
        // 文件菜单
        fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic('F');
        
        newItem = new JMenuItem("新建");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        openItem = new JMenuItem("打开");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveItem = new JMenuItem("保存");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveAsItem = new JMenuItem("另存为");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        exitItem = new JMenuItem("退出");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 编辑菜单
        editMenu = new JMenu("编辑(E)");
        editMenu.setMnemonic('E');
        
        undoItem = new JMenuItem("撤销");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        redoItem = new JMenuItem("重做");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        cutItem = new JMenuItem("剪切");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        copyItem = new JMenuItem("复制");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pasteItem = new JMenuItem("粘贴");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        selectAllItem = new JMenuItem("全选");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        findItem = new JMenuItem("查找");
        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        editMenu.add(findItem);
        
        // 初始化撤销/重做按钮状态
        updateUndoRedoState();
        
        // 格式菜单
        formatMenu = new JMenu("格式(O)");
        formatMenu.setMnemonic('O');
        
        fontItem = new JMenuItem("字体...");
        fontSizeItem = new JMenuItem("字号...");
        themeItem = new JMenuItem("切换主题");
        themeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        
        formatMenu.add(fontItem);
        formatMenu.add(fontSizeItem);
        formatMenu.addSeparator();
        formatMenu.add(themeItem);
        
        // 帮助菜单
        helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic('H');
        aboutItem = new JMenuItem("关于");
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        helpMenu.add(aboutItem);
        
        // 菜单栏
        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        
        // 文件选择器
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "文本文件 (*.txt)", "txt"));
    }
    
    private void updateUndoRedoState() {
        undoItem.setEnabled(undoManager.canUndo());
        redoItem.setEnabled(undoManager.canRedo());
    }
    
    private void initListeners() {
        // 文本区域监听器
        textArea.addCaretListener(e -> updateStatus());
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { setTitleModified(true); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { setTitleModified(true); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { setTitleModified(true); }
        });
        
        // 文件菜单监听器
        ActionListener newAction = e -> newFile();
        newItem.addActionListener(newAction);
        newButton.addActionListener(newAction);
        
        ActionListener openAction = e -> openFile();
        openItem.addActionListener(openAction);
        openButton.addActionListener(openAction);
        
        ActionListener saveAction = e -> saveFile();
        saveItem.addActionListener(saveAction);
        saveButton.addActionListener(saveAction);
        
        saveAsItem.addActionListener(e -> saveFileAs());
        exitItem.addActionListener(e -> exitApp());
        
        // 编辑菜单监听器
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
                updateUndoRedoState();
            }
        });
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
                updateUndoRedoState();
            }
        });
        
        ActionListener cutAction = e -> textArea.cut();
        cutItem.addActionListener(cutAction);
        cutButton.addActionListener(cutAction);
        
        ActionListener copyAction = e -> textArea.copy();
        copyItem.addActionListener(copyAction);
        copyButton.addActionListener(copyAction);
        
        ActionListener pasteAction = e -> textArea.paste();
        pasteItem.addActionListener(pasteAction);
        pasteButton.addActionListener(pasteAction);
        
        selectAllItem.addActionListener(e -> textArea.selectAll());
        
        ActionListener findAction = e -> showFindDialog();
        findItem.addActionListener(findAction);
        findButton.addActionListener(findAction);
        
        // 格式菜单监听器
        fontItem.addActionListener(e -> changeFont());
        fontSizeItem.addActionListener(e -> changeFontSize());
        themeItem.addActionListener(e -> toggleTheme());
        
        // 帮助菜单监听器
        aboutItem.addActionListener(e -> showAboutDialog());
        
        // 窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });
    }
    
    private void newFile() {
        if (checkUnsavedChanges()) {
            textArea.setText("");
            currentFile = null;
            setTitle("增强记事本 - 未命名");
            textArea.setCaretPosition(0);
            undoManager.discardAllEdits();
            updateUndoRedoState();
        }
    }
    
    private void openFile() {
        if (checkUnsavedChanges()) {
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    currentFile = fileChooser.getSelectedFile();
                    BufferedReader reader = new BufferedReader(new FileReader(currentFile));
                    textArea.read(reader, null);
                    reader.close();
                    setTitle("增强记事本 - " + currentFile.getName());
                    undoManager.discardAllEdits();
                    updateUndoRedoState();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "打开文件失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            try {
                FileWriter writer = new FileWriter(currentFile);
                textArea.write(writer);
                writer.close();
                setTitle("增强记事本 - " + currentFile.getName());
                JOptionPane.showMessageDialog(this, "保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存文件失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveFileAs() {
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            // 确保文件扩展名
            if (!currentFile.getName().toLowerCase().endsWith(".txt")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".txt");
            }
            saveFile();
        }
    }
    
    private void exitApp() {
        if (checkUnsavedChanges()) {
            System.exit(0);
        }
    }
    
    private boolean checkUnsavedChanges() {
        if (textArea.getDocument().getLength() > 0 && getTitle().startsWith("*")) {
            int result = JOptionPane.showConfirmDialog(this,
                "文件已修改，是否保存更改？",
                "保存更改",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                saveFile();
                return true;
            } else if (result == JOptionPane.NO_OPTION) {
                return true;
            }
            return false;
        }
        return true;
    }
    
    private void showFindDialog() {
        JDialog findDialog = new JDialog(this, "查找", false);
        findDialog.setLayout(new FlowLayout());
        
        JTextField findField = new JTextField(20);
        JButton findNextButton = new JButton("查找下一个");
        JButton cancelButton = new JButton("取消");
        
        findNextButton.addActionListener(e -> {
            String text = findField.getText();
            if (!text.isEmpty()) {
                String content = textArea.getText();
                int fromIndex = textArea.getCaretPosition();
                int index = content.indexOf(text, fromIndex);
                
                if (index >= 0) {
                    textArea.setCaretPosition(index);
                    textArea.select(index, index + text.length());
                } else {
                    JOptionPane.showMessageDialog(findDialog, "未找到匹配项", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> findDialog.dispose());
        
        findDialog.add(new JLabel("查找内容:"));
        findDialog.add(findField);
        findDialog.add(findNextButton);
        findDialog.add(cancelButton);
        
        findDialog.pack();
        findDialog.setLocationRelativeTo(this);
        findDialog.setVisible(true);
    }
    
    private void changeFont() {
        Font currentFont = textArea.getFont();
        JFontChooser fontChooser = new JFontChooser();
        fontChooser.setSelectedFont(currentFont);
        
        int result = fontChooser.showDialog(this);
        if (result == JFontChooser.OK_OPTION) {
            textArea.setFont(fontChooser.getSelectedFont());
        }
    }
    
    private void changeFontSize() {
        String sizeStr = JOptionPane.showInputDialog(this,
            "输入字号 (8-72):",
            Integer.toString(textArea.getFont().getSize()));
        
        try {
            int size = Integer.parseInt(sizeStr);
            if (size >= 8 && size <= 72) {
                Font currentFont = textArea.getFont();
                textArea.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
            } else {
                JOptionPane.showMessageDialog(this, "请输入8-72之间的数字");
            }
        } catch (NumberFormatException ex) {
            // 用户取消或输入无效
        }
    }
    
    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            textArea.setBackground(darkThemeBg);
            textArea.setForeground(darkThemeFg);
            textArea.setCaretColor(Color.WHITE);
        } else {
            textArea.setBackground(lightThemeBg);
            textArea.setForeground(lightThemeFg);
            textArea.setCaretColor(Color.BLACK);
        }
    }
    
    private void showAboutDialog() {
        String aboutText = "增强记事本 v1.0\n\n" +
                          "一个功能丰富的文本编辑器\n" +
                          "支持多种文本编辑功能\n\n" +
                          "开发时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        JOptionPane.showMessageDialog(this,
            aboutText,
            "关于增强记事本",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus() {
        try {
            int caretPos = textArea.getCaretPosition();
            int line = 1;
            int column = 1;
            
            String text = textArea.getText(0, caretPos);
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
            }
            
            statusLabel.setText(String.format(" 就绪 | 行: %d 列: %d | 字符数: %d | UTF-8",
                line, column, textArea.getText().length()));
        } catch (Exception ex) {
            statusLabel.setText(" 状态错误");
        }
    }
    
    private void setTitleModified(boolean modified) {
        String title = getTitle();
        if (modified && !title.startsWith("*")) {
            setTitle("*" + title);
        } else if (!modified && title.startsWith("*")) {
            setTitle(title.substring(1));
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SimpleNotepad notepad = new SimpleNotepad();
            notepad.setVisible(true);
        });
    }
}

// 字体选择器类
class JFontChooser extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    
    private Font selectedFont;
    private int returnValue = CANCEL_OPTION;
    
    public JFontChooser() {
        setTitle("选择字体");
        setModal(true);
        setLayout(new BorderLayout());
        
        // 字体列表
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        JList<String> fontList = new JList<>(fontNames);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 字号选择
        String[] sizes = {"8", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "48", "72"};
        JComboBox<String> sizeCombo = new JComboBox<>(sizes);
        sizeCombo.setSelectedItem("14");
        
        // 样式选择
        JComboBox<String> styleCombo = new JComboBox<>(new String[]{"普通", "粗体", "斜体", "粗斜体"});
        
        // 预览区域
        JTextArea preview = new JTextArea("字体预览\nAaBbCcDdEeFfGg");
        preview.setEditable(false);
        preview.setBorder(BorderFactory.createTitledBorder("预览"));
        
        // 按钮
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        // 布局
        JPanel controlPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        controlPanel.add(new JLabel("字体:"));
        controlPanel.add(new JScrollPane(fontList));
        controlPanel.add(new JLabel("字号:"));
        controlPanel.add(sizeCombo);
        controlPanel.add(new JLabel("样式:"));
        controlPanel.add(styleCombo);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(preview), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 事件处理
        ActionListener updatePreview = e -> {
            String fontName = fontList.getSelectedValue();
            int fontSize = Integer.parseInt((String) sizeCombo.getSelectedItem());
            int fontStyle = getStyleFromString((String) styleCombo.getSelectedItem());
            
            if (fontName != null) {
                Font newFont = new Font(fontName, fontStyle, fontSize);
                preview.setFont(newFont);
            }
        };
        
        fontList.addListSelectionListener(e -> updatePreview.actionPerformed(null));
        sizeCombo.addActionListener(updatePreview);
        styleCombo.addActionListener(updatePreview);
        
        okButton.addActionListener(e -> {
            String fontName = fontList.getSelectedValue();
            int fontSize = Integer.parseInt((String) sizeCombo.getSelectedItem());
            int fontStyle = getStyleFromString((String) styleCombo.getSelectedItem());
            
            if (fontName != null) {
                selectedFont = new Font(fontName, fontStyle, fontSize);
                returnValue = OK_OPTION;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            returnValue = CANCEL_OPTION;
            dispose();
        });
        
        // 默认选择
        if (fontList.getModel().getSize() > 0) {
            fontList.setSelectedIndex(0);
        }
        setSize(400, 500);
        setLocationRelativeTo(null);
    }
    
    private int getStyleFromString(String style) {
        switch (style) {
            case "粗体": return Font.BOLD;
            case "斜体": return Font.ITALIC;
            case "粗斜体": return Font.BOLD | Font.ITALIC;
            default: return Font.PLAIN;
        }
    }
    
    public int showDialog(Component parent) {
        setLocationRelativeTo(parent);
        setVisible(true);
        return returnValue;
    }
    
    public Font getSelectedFont() {
        return selectedFont;
    }
    
    public void setSelectedFont(Font font) {
        selectedFont = font;
    }
}