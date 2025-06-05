package edu.iuc.client;

import edu.iuc.server.ServerMain;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainMenuFrame extends JFrame {
    private static final AtomicInteger clientCounter = new AtomicInteger(1);
    private final List<EditorFrame> openClients = new ArrayList<>();
    
    private JLabel serverStatusLabel;
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JButton newClientButton;
    private JButton stopServerButton;
    
    private Thread serverThread;
    private boolean serverRunning = false;

    public MainMenuFrame() {
        initializeGUI();
        startServer();
    }

    private void initializeGUI() {
        setTitle("Cerrahpaşa Docs");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAllClients();
                stopServer();
                System.exit(0);
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Durumu"));
        panel.setPreferredSize(new Dimension(0, 80));

        serverStatusLabel = new JLabel("Server başlatılıyor...", JLabel.CENTER);
        serverStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(serverStatusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        stopServerButton = new JButton("Server'ı Durdur");
        stopServerButton.addActionListener(e -> stopServer());
        stopServerButton.setEnabled(false);
        
        JButton restartServerButton = new JButton("Server'ı Yeniden Başlat");
        restartServerButton.addActionListener(e -> restartServer());
        
        buttonPanel.add(stopServerButton);
        buttonPanel.add(restartServerButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Açık Client'lar"));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        leftPanel.add(clientScrollPane, BorderLayout.CENTER);
        
        JPanel clientButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        newClientButton = new JButton("Yeni Client Aç");
        newClientButton.addActionListener(e -> openNewClient());
        newClientButton.setEnabled(false);
        
        JButton focusClientButton = new JButton("Client'e Geç");
        focusClientButton.addActionListener(e -> focusSelectedClient());
        
        JButton closeClientButton = new JButton("Client'i Kapat");
        closeClientButton.addActionListener(e -> closeSelectedClient());
        
        clientButtonPanel.add(newClientButton);
        clientButtonPanel.add(focusClientButton);
        clientButtonPanel.add(closeClientButton);
        
        leftPanel.add(clientButtonPanel, BorderLayout.SOUTH);
        panel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = createInfoPanel();
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bilgi"));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText(
            "Cerrahpaşa Docs - Çok Kullanıcılı Metin Editörü\n\n" +
            "Özellikler:\n" +
            "• Birden fazla kullanıcı aynı anda bağlanabilir\n" +
            "• Gerçek zamanlı metin senkronizasyonu\n" +
            "• Dosya paylaşımı ve yönetimi\n" +
            "• TCP/IP soket iletişimi\n" +
            "• Özel CerrahText Protocol (CTP)\n\n" +
            "Kullanım:\n" +
            "1. Server otomatik olarak başlatılır (Port: 9999)\n" +
            "2. 'Yeni Client Aç' butonuna tıklayın\n" +
            "3. Kullanıcı adınızı girin\n" +
            "4. Dosyaları açın ve düzenlemeye başlayın\n\n" +
            "Not: Aynı kullanıcı adıyla birden fazla bağlantı yapılamaz."
        );
        infoArea.setBackground(panel.getBackground());
        infoArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Log"));
        panel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void startServer() {
        if (serverRunning) {
            return;
        }

        serverThread = new Thread(() -> {
            try {
                addLog("Server başlatılıyor...");
                ServerMain.main(new String[]{});
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    addLog("Server hatası: " + e.getMessage());
                    serverStatusLabel.setText("❌ Server Hatası");
                    serverStatusLabel.setForeground(Color.RED);
                });
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();

        Timer timer = new Timer(2000, e -> {
            serverRunning = true;
            serverStatusLabel.setText("✅ Server Çalışıyor (Port: 9999)");
            serverStatusLabel.setForeground(Color.GREEN);
            newClientButton.setEnabled(true);
            stopServerButton.setEnabled(true);
            addLog("Server başarıyla başlatıldı (Port: 9999)");
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void stopServer() {
        if (!serverRunning) {
            return;
        }

        closeAllClients();
        
        if (serverThread != null) {
            serverThread.interrupt();
        }

        serverRunning = false;
        serverStatusLabel.setText("❌ Server Durduruldu");
        serverStatusLabel.setForeground(Color.RED);
        newClientButton.setEnabled(false);
        stopServerButton.setEnabled(false);
        addLog("Server durduruldu");
    }

    private void restartServer() {
        addLog("Server yeniden başlatılıyor...");
        stopServer();
        
        Timer timer = new Timer(1000, e -> startServer());
        timer.setRepeats(false);
        timer.start();
    }

    private void openNewClient() {
        if (!serverRunning) {
            JOptionPane.showMessageDialog(this, 
                "Server çalışmıyor! Önce server'ı başlatın.", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int clientNumber = clientCounter.getAndIncrement();
        String clientName = "Client-" + clientNumber;
        
        EditorFrame client = new EditorFrame(clientName, this);
        openClients.add(client);
        clientListModel.addElement(clientName + " (Bağlanıyor...)");
        
        client.setVisible(true);
        addLog("Yeni client açıldı: " + clientName);
    }

    private void focusSelectedClient() {
        int selectedIndex = clientList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < openClients.size()) {
            EditorFrame client = openClients.get(selectedIndex);
            client.toFront();
            client.requestFocus();
        }
    }

    private void closeSelectedClient() {
        int selectedIndex = clientList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < openClients.size()) {
            EditorFrame client = openClients.get(selectedIndex);
            closeClient(client);
        }
    }

    public void closeClient(EditorFrame client) {
        int index = openClients.indexOf(client);
        if (index >= 0) {
            openClients.remove(index);
            clientListModel.remove(index);
            client.dispose();
            addLog("Client kapatıldı: " + client.getClientName());
        }
    }

    private void closeAllClients() {
        for (EditorFrame client : new ArrayList<>(openClients)) {
            closeClient(client);
        }
    }
    
    public void updateClientStatus(EditorFrame client, String status) {
        int index = openClients.indexOf(client);
        if (index >= 0) {
            String clientName = client.getClientName();
            String username = client.getUsername();
            String displayText;
            
            if (username != null && !username.trim().isEmpty()) {
                displayText = clientName + " (" + username + ") - " + status;
            } else {
                displayText = clientName + " - " + status;
            }
            
            clientListModel.setElementAt(displayText, index);
        }
    }

    private void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
} 