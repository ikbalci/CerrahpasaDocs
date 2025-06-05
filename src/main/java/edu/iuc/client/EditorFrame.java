package edu.iuc.client;

import edu.iuc.shared.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class EditorFrame extends JFrame {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String username;
    private String clientName;
    private MainMenuFrame parentFrame;
    private boolean isConnected = false;

    private JTabbedPane tabbedPane;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JTextArea statusArea;
    private JLabel statusLabel;

    private Map<String, JTextArea> openFiles = new HashMap<>();
    private Map<String, Boolean> fileChanged = new HashMap<>();

    public EditorFrame() {
        this("EditorFrame", null);
    }

    public EditorFrame(String clientName, MainMenuFrame parentFrame) {
        this.clientName = clientName;
        this.parentFrame = parentFrame;
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        setTitle(clientName != null ? clientName : "Cerrahpaşa Docs");
        setSize(1000, 700);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        setupKeyboardShortcuts();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeEditor();
            }
        });
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane pane = new JTabbedPane();

        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    int tabIndex = pane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex >= 0) {
                        closeTab(tabIndex);
                    }
                }
            }
        });

        return pane;
    }

    private void setupKeyboardShortcuts() {
        KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKey, "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentFile();
            }
        });

        KeyStroke closeTabKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeTabKey, "closeTab");
        getRootPane().getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeCurrentTab();
            }
        });

        KeyStroke newFileKey = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newFileKey, "newFile");
        getRootPane().getActionMap().put("newFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewFile();
            }
        });

        KeyStroke refreshKey = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(refreshKey, "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestFileList();
            }
        });
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Dosyalar"));

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedFile = fileList.getSelectedValue();
                    if (selectedFile != null) {
                        openFile(selectedFile);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JButton openButton = new JButton("Aç");
        openButton.addActionListener(e -> {
            String selectedFile = fileList.getSelectedValue();
            if (selectedFile != null) {
                openFile(selectedFile);
            }
        });

        JButton newButton = new JButton("Yeni");
        newButton.addActionListener(e -> createNewFile());

        JButton refreshButton = new JButton("Yenile");
        refreshButton.addActionListener(e -> requestFileList());

        JButton saveButton = new JButton("Kaydet");
        saveButton.addActionListener(e -> saveCurrentFile());

        buttonPanel.add(openButton);
        buttonPanel.add(newButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("Bağlantı bekleniyor...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.WEST);

        JLabel shortcutLabel = new JLabel("Kısayollar: Ctrl+S=Kaydet, Ctrl+W=Kapat, Ctrl+N=Yeni, F5=Yenile");
        shortcutLabel.setFont(shortcutLabel.getFont().deriveFont(Font.PLAIN, 10f));
        shortcutLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(shortcutLabel, BorderLayout.CENTER);

        statusArea = new JTextArea(4, 0);
        statusArea.setEditable(false);
        statusArea.setBackground(getBackground());
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(0, 100));
        panel.add(statusScroll, BorderLayout.SOUTH);

        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Dosya");

        JMenuItem newFileItem = new JMenuItem("Yeni Dosya (Ctrl+N)");
        newFileItem.addActionListener(e -> createNewFile());

        JMenuItem saveItem = new JMenuItem("Kaydet (Ctrl+S)");
        saveItem.addActionListener(e -> saveCurrentFile());

        JMenuItem closeTabItem = new JMenuItem("Tab'ı Kapat (Ctrl+W)");
        closeTabItem.addActionListener(e -> closeCurrentTab());

        JMenuItem refreshItem = new JMenuItem("Dosya Listesi Yenile (F5)");
        refreshItem.addActionListener(e -> requestFileList());

        fileMenu.add(newFileItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(closeTabItem);
        fileMenu.addSeparator();
        fileMenu.add(refreshItem);

        JMenu connectionMenu = new JMenu("Bağlantı");

        JMenuItem reconnectItem = new JMenuItem("Yeniden Bağlan");
        reconnectItem.addActionListener(e -> reconnect());

        JMenuItem disconnectItem = new JMenuItem("Bağlantıyı Kes");
        disconnectItem.addActionListener(e -> disconnectFromServer());

        connectionMenu.add(reconnectItem);
        connectionMenu.add(disconnectItem);

        JMenu windowMenu = new JMenu("Pencere");

        JMenuItem closeItem = new JMenuItem("Pencereyi Kapat");
        closeItem.addActionListener(e -> closeEditor());

        if (parentFrame != null) {
            JMenuItem mainMenuitem = new JMenuItem("Ana Menüye Dön");
            mainMenuitem.addActionListener(e -> {
                parentFrame.toFront();
                parentFrame.requestFocus();
            });
            windowMenu.add(mainMenuitem);
            windowMenu.addSeparator();
        }

        windowMenu.add(closeItem);

        menuBar.add(fileMenu);
        menuBar.add(connectionMenu);
        menuBar.add(windowMenu);

        return menuBar;
    }

    private void closeTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            String fileName = tabbedPane.getTitleAt(tabIndex);
            if (fileName.endsWith("*")) {
                fileName = fileName.substring(0, fileName.length() - 1);
            }

            if (fileChanged.getOrDefault(fileName, false)) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "'" + fileName + "' dosyasında kaydedilmemiş değişiklikler var.\nKapatmadan önce kaydetmek ister misiniz?",
                    "Dosyayı Kapat",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    saveFile(fileName);
                } else if (result == JOptionPane.CANCEL_OPTION) {
                    return; // İptal
                }
            }

            openFiles.remove(fileName);
            fileChanged.remove(fileName);
            tabbedPane.removeTabAt(tabIndex);
            addStatus("Dosya kapatıldı: " + fileName);
        }
    }

    private void closeCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            closeTab(selectedIndex);
        }
    }

    private void saveCurrentFile() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            String fileName = tabbedPane.getTitleAt(selectedIndex);
            if (fileName.endsWith("*")) {
                fileName = fileName.substring(0, fileName.length() - 1);
            }
            saveFile(fileName);
        }
    }

    private void saveFile(String fileName) {
        JTextArea textArea = openFiles.get(fileName);
        if (textArea != null) {
            sendMessage(Message.saveFile(fileName, textArea.getText()));
            addStatus("Dosya kaydediliyor: " + fileName);
        }
    }

    private void reconnect() {
        disconnectFromServer();
        Timer timer = new Timer(1000, e -> connectToServer());
        timer.setRepeats(false);
        timer.start();
    }

    private void closeEditor() {
        boolean hasUnsavedChanges = fileChanged.values().stream().anyMatch(changed -> changed);

        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Kaydedilmemiş değişiklikler var. Yine de kapatmak istiyor musunuz?",
                "Editörü Kapat",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        disconnectFromServer();

        if (parentFrame != null) {
            parentFrame.closeClient(this);
        } else {
            System.exit(0);
        }
    }

    private void connectToServer() {
        if (isConnected) {
            addStatus("Zaten bağlısınız!");
            return;
        }
    
        username = JOptionPane.showInputDialog(this,
            "Kullanıcı adınızı girin:",
            "Giriş",
            JOptionPane.PLAIN_MESSAGE);

        if (username == null || username.trim().isEmpty()) {
            addStatus("Geçersiz kullanıcı adı!");
            if (parentFrame != null) {
                parentFrame.closeClient(this);
            }
            return;
        }

        try {
            socket = new Socket("localhost", 9999);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::listenToServer).start();

            sendMessage(Message.login(username));

            addStatus("Sunucuya bağlanıldı: " + username);
            updateParentStatus("Bağlanıyor...");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı: " + e.getMessage());
            addStatus("Bağlantı hatası: " + e.getMessage());
            updateParentStatus("Bağlantı Hatası");
        }
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                final String message = line;
                SwingUtilities.invokeLater(() -> processServerMessage(message));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                if (isConnected) {
                    addStatus("Sunucu bağlantısı kesildi: " + e.getMessage());
                    isConnected = false;
                    statusLabel.setText("❌ Bağlantı kesildi");
                    statusLabel.setForeground(Color.RED);
                    updateParentStatus("Bağlantı Kesildi");
                }
            });
        }
    }

    private void processServerMessage(String rawMessage) {
        try {
            Message message = Message.fromRaw(rawMessage);

            switch (message.getType()) {
                case SUCCESS:
                    handleSuccess(message.getSuccessMessage());
                    break;

                case ERROR:
                    handleError(message.getErrorType(), message.getErrorMessage());
                    break;

                case LIST_FILES_RESPONSE:
                    handleFileListResponse(message.getFileList());
                    break;

                case OPEN_FILE_RESPONSE:
                    handleOpenFileResponse(message.getFileName(), message.getFileContent());
                    break;

                case EDIT:
                    handleEditUpdate(message.getFileName(), message.getFileContent());
                    break;

                case USER_JOINED:
                    addStatus("👤 Kullanıcı katıldı: " + message.getUsername());
                    break;

                case USER_LEFT:
                    addStatus("👋 Kullanıcı ayrıldı: " + message.getUsername());
                    break;

                default:
                    addStatus("❓ Bilinmeyen mesaj: " + message.getType());
            }
        } catch (Exception e) {
            addStatus("❌ Mesaj işlenirken hata: " + e.getMessage());
        }
    }

    private void handleSuccess(String message) {
        if (message.equals("Giriş başarılı")) {
            isConnected = true;
            statusLabel.setText("✅ Bağlı: " + username);
            statusLabel.setForeground(Color.GREEN);
            addStatus("✅ Başarıyla giriş yapıldı!");
            updateParentStatus("Bağlı: " + username);
            updateTitle();
        } else {
            addStatus("✅ " + message);
        }
    }

    private void handleError(String errorType, String errorMessage) {
        addStatus("❌ Hata [" + errorType + "]: " + errorMessage);

        if (errorType.equals("USERNAME_TAKEN")) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Bu kullanıcı adı zaten kullanılıyor. Lütfen farklı bir ad deneyin.",
                    "Kullanıcı Adı Hatası",
                    JOptionPane.WARNING_MESSAGE);
                disconnectFromServer();
                connectToServer();
            });
        }
    }

    private void handleFileListResponse(String fileList) {
        fileListModel.clear();
        if (!fileList.isEmpty()) {
            String[] files = fileList.split(",");
            for (String file : files) {
                if (!file.trim().isEmpty()) {
                    fileListModel.addElement(file.trim());
                }
            }
        }
        addStatus("📁 Dosya listesi güncellendi (" + fileListModel.size() + " dosya)");
    }

    private void handleOpenFileResponse(String fileName, String content) {
        openFileInEditor(fileName, content.replace("\\n", "\n"));
    }

    private void handleEditUpdate(String fileName, String content) {
        JTextArea textArea = openFiles.get(fileName);
        if (textArea != null) {

            int caretPosition = textArea.getCaretPosition();
            textArea.setText(content.replace("\\n", "\n"));

            try {
                textArea.setCaretPosition(Math.min(caretPosition, textArea.getText().length()));
            } catch (IllegalArgumentException e) {
                textArea.setCaretPosition(textArea.getText().length());
            }

            fileChanged.put(fileName, false);

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                String tabTitle = tabbedPane.getTitleAt(i);
                if (tabTitle.equals(fileName + "*")) {
                    tabbedPane.setTitleAt(i, fileName);
                    break;
                }
            }

            addStatus("🔄 Dosya güncellendi: " + fileName);
        }
    }

    private void openFile(String fileName) {
        if (openFiles.containsKey(fileName)) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                String tabTitle = tabbedPane.getTitleAt(i);
                if (tabTitle.equals(fileName) || tabTitle.equals(fileName + "*")) {
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        sendMessage(Message.openFileRequest(fileName));
    }

    private void openFileInEditor(String fileName, String content) {
        if (openFiles.containsKey(fileName)) {
            return;
        }

        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(4);
    
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onTextChanged(fileName);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onTextChanged(fileName);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onTextChanged(fileName);
            }
        });

        openFiles.put(fileName, textArea);
        fileChanged.put(fileName, false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(fileName);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, 10f));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> {
            int tabIndex = tabbedPane.indexOfComponent(scrollPane);
            if (tabIndex >= 0) {
                closeTab(tabIndex);
            }
        });

        tabPanel.add(titleLabel, BorderLayout.CENTER);
        tabPanel.add(closeButton, BorderLayout.EAST);

        tabbedPane.addTab(fileName, scrollPane);
        int tabIndex = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(tabIndex, tabPanel);
        tabbedPane.setSelectedComponent(scrollPane);

        addStatus("📄 Dosya açıldı: " + fileName);
    }

    private void onTextChanged(String fileName) {
        if (!fileChanged.getOrDefault(fileName, false)) {
            fileChanged.put(fileName, true);

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component tabComponent = tabbedPane.getTabComponentAt(i);
                if (tabComponent instanceof JPanel) {
                    JLabel titleLabel = (JLabel) ((JPanel) tabComponent).getComponent(0);
                    if (titleLabel.getText().equals(fileName)) {
                        titleLabel.setText(fileName + "*");
                        break;
                    }
                }
            }
        }

        Timer timer = new Timer(800, e -> {
            JTextArea textArea = openFiles.get(fileName);
            if (textArea != null && fileChanged.getOrDefault(fileName, false)) {
                sendMessage(Message.edit(fileName, textArea.getText()));
                fileChanged.put(fileName, false);

                // Tab başlığını normal hale getir
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Component tabComponent = tabbedPane.getTabComponentAt(i);
                    if (tabComponent instanceof JPanel) {
                        JLabel titleLabel = (JLabel) ((JPanel) tabComponent).getComponent(0);
                        if (titleLabel.getText().equals(fileName + "*")) {
                            titleLabel.setText(fileName);
                            break;
                        }
                    }
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog(this,
            "Yeni dosya adını girin:",
            "Yeni Dosya",
            JOptionPane.PLAIN_MESSAGE);

        if (fileName != null && !fileName.trim().isEmpty()) {
            sendMessage(Message.createFile(fileName.trim()));
        }
    }

    private void requestFileList() {
        if (isConnected) {
            sendMessage(Message.listFilesRequest());
        }
    }

    private void sendMessage(String message) {
        if (writer != null) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                addStatus("❌ Mesaj gönderilirken hata: " + e.getMessage());
            }
        }
    }

    private void sendMessage(Message message) {
        sendMessage(message.toProtocolString());
    }

    private void addStatus(String message) {
        statusArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] " + message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void updateParentStatus(String status) {
        if (parentFrame != null) {
            parentFrame.updateClientStatus(this, status);
        }
    }

    private void updateTitle() {
        if (username != null && !username.trim().isEmpty()) {
            setTitle("Cerrahpaşa Docs - " + username);
        }
    }

    private void disconnectFromServer() {
        try {
            isConnected = false;
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();

            statusLabel.setText("❌ Bağlantı kesildi");
            statusLabel.setForeground(Color.RED);
            addStatus("🔌 Sunucudan bağlantı kesildi");
            updateParentStatus("Bağlantı Kesildi");

        } catch (IOException e) {
            addStatus("❌ Bağlantı kesilirken hata: " + e.getMessage());
        }
    }

    public String getClientName() {
        return clientName != null ? clientName : "EditorFrame";
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return isConnected;
    }
}

