package framesLearn;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class Application extends JFrame {
    
    // Colors
    private final Color PRIMARY_COLOR     = new Color(0, 122, 204);
    private final Color SECONDARY_COLOR   = new Color(255, 179, 71);
    private final Color DARK_BG           = new Color(24, 26, 30);
    private final Color LIGHT_BG          = new Color(44, 47, 51);
    private final Color HIGHLIGHT_COLOR   = new Color(0, 180, 255);
    private final Color POSITIVE_COLOR    = new Color(34, 197, 94);
    private final Color NEGATIVE_COLOR    = new Color(239, 68, 68);

    // Data
    private List<Transaction> transactions = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private User currentUser;
    private double currentBalance = 0;
    private double targetBalance = 0;
    private Timer balanceAnimationTimer;
    private float fadeAlpha = 0f;
    private Timer fadeTimer;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JTable transactionsTable;
    private DefaultTableModel transactionsModel;
    private JLabel balanceLabel;
    private JTextArea consoleArea;
    private JProgressBar loadingBar;
    private JPanel calculationsPanel;
    
    // Date format
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public Application() {
        setTitle("E-19 Accounting System - ZAR");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(DARK_BG);
        getContentPane().setBackground(DARK_BG);
        
        initFadeAnimation();
        showWelcomeBanner();
        showSplashScreen();
        initializeSampleData();
    }
    
    private void initFadeAnimation() {
        fadeTimer = new Timer(10, e -> {
            if (isVisible()) {
                fadeAlpha = Math.min(1f, fadeAlpha + 0.05f);
                if (fadeAlpha >= 1f) {
                    fadeTimer.stop();
                }
            } else {
                fadeAlpha = Math.max(0f, fadeAlpha - 0.05f);
                if (fadeAlpha <= 0f) {
                    fadeTimer.stop();
                }
            }
            repaint();
        });
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        if (fadeAlpha > 0f && fadeAlpha < 1f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        }
        super.paint(g2d);
        g2d.dispose();
    }
    
    private void showWelcomeBanner() {
        String banner = "\n" +
            "  _______  _____  _____   ____  _____  \n" +
            " | ____\\ \\/ / _ \\|  ___| / ___|| ____| \n" +
            " |  _|  \\  / | | | |_    \\___ \\|  _|   \n" +
            " | |___ /  \\ |_| |  _|    ___) | |___  \n" +
            " |_____/_/\\_\\___/|_|     |____/|_____| \n" +
            "                                       \n" +
            " Accounting System v2.0\n" +
            " Developed by Khuliso Manyatshe (Error19)\n" +
            " System Initializing...\n";
        
        System.out.println(banner);
    }
    
    private void showSplashScreen() {
        JWindow splashScreen = new JWindow();
        splashScreen.setSize(500, 300);
        splashScreen.setLocationRelativeTo(null);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(DARK_BG);
        
        JLabel logoLabel = new JLabel("E-19 Accounting System", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(SECONDARY_COLOR);
        logoLabel.setBorder(new EmptyBorder(50, 0, 30, 0));
        
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(false);
        loadingBar.setStringPainted(true);
        loadingBar.setForeground(PRIMARY_COLOR);
        loadingBar.setBackground(LIGHT_BG);
        loadingBar.setBorder(new EmptyBorder(10, 50, 50, 50));
        
        contentPane.add(logoLabel, BorderLayout.CENTER);
        contentPane.add(loadingBar, BorderLayout.SOUTH);
        
        splashScreen.setContentPane(contentPane);
        splashScreen.setVisible(true);
        
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep(30);
                    final int progress = i;
                    SwingUtilities.invokeLater(() -> {
                        loadingBar.setValue(progress);
                        loadingBar.setString("Loading " + progress + "%");
                        
                        if (progress == 20) {
                            logToConsole("Loading system modules...");
                        } else if (progress == 50) {
                            logToConsole("Initializing database connection...");
                        } else if (progress == 80) {
                            logToConsole("Preparing user interface...");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            SwingUtilities.invokeLater(() -> {
                splashScreen.dispose();
                showLoginDialog();
                
                if (currentUser != null) {
                    initUI();
                    refreshTransactions();
                    logToConsole("System Initialized. Welcome, " + currentUser.getUsername() + ".");
                    logToConsole("System ready. Current balance: R" + String.format("%,.2f", currentBalance));
                    
                    fadeAlpha = 0f;
                    fadeTimer.start();
                    setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }).start();
    }
    
    private void initializeSampleData() {
        users.add(new User("Error19", "admin123", "Administrator"));
        users.add(new User("user", "user123", "Standard User"));
        
        transactions.add(new Transaction("TRX-001", "2023-10-01", "Office Supplies", 1250.50, "Expense", 15));
        transactions.add(new Transaction("TRX-002", "2023-10-05", "Client Payment", 8500.00, "Income", 0));
        transactions.add(new Transaction("TRX-003", "2023-10-10", "Software License", 3200.75, "Expense", 15));
        
        // Sample inventory data
        transactions.add(new Transaction("INV-001", "2023-10-01", "Opening Inventory", 5000.00, "Inventory", 0));
        transactions.add(new Transaction("INV-002", "2023-10-15", "Inventory Purchase", 3000.00, "Inventory", 0));
        transactions.add(new Transaction("INV-003", "2023-10-31", "Closing Inventory", 2000.00, "Inventory", 0));
    }
    
    private void showLoginDialog() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BG);
        
        JLabel headerLabel = new JLabel("E-19 ACCOUNTING SYSTEM", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(SECONDARY_COLOR);
        headerLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel versionLabel = new JLabel("v2.0 | Developed by Error19", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        versionLabel.setForeground(Color.LIGHT_GRAY);
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(versionLabel, BorderLayout.SOUTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_BG);
        formPanel.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setPreferredSize(new Dimension(400, 300));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        JTextField usernameField = createStyledTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(LIGHT_BG);
        passwordField.setForeground(Color.WHITE);
        passwordField.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        passwordField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        JButton loginButton = createStyledButton("LOGIN");
        loginButton.setForeground(Color.BLACK);
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    currentUser = user;
                    ((Window) SwingUtilities.getRoot(loginButton)).dispose();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        });
        formPanel.add(loginButton, gbc);
        
        gbc.gridy = 3;
        JButton exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 12));
        exitButton.setForeground(Color.BLACK);
        exitButton.setPreferredSize(new Dimension(150, 40));
        exitButton.setBackground(new Color(204, 0, 0));
        exitButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        exitButton.addActionListener(e -> System.exit(0));
        formPanel.add(exitButton, gbc);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setUndecorated(true);
        loginDialog.setContentPane(mainPanel);
        loginDialog.pack();
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setVisible(true);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        
        JLabel titleLabel = new JLabel("E-19 ACCOUNTING SYSTEM - ZAR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(SECONDARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        userPanel.setBackground(DARK_BG);
        
        JLabel userLabel = new JLabel("User: " + currentUser.getUsername());
        userLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        logoutButton.addActionListener(e -> {
            fadeTimer.start();
            new Timer(300, evt -> {
                currentUser = null;
                dispose();
                showLoginDialog();
            }).start();
        });
        
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(DARK_BG);
        tabbedPane.setForeground(Color.BLACK);
        
        tabbedPane.addTab("DASHBOARD", createDashboardPanel());
        tabbedPane.addTab("TRANSACTIONS", createTransactionsPanel());
        tabbedPane.addTab("REPORTS", createReportsPanel());
        tabbedPane.addTab("FINANCIAL CALCULATIONS", createCalculationsPanel());
        tabbedPane.addTab("CONSOLE", createConsolePanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(DARK_BG);
        statusBar.setBorder(new MatteBorder(2, 0, 0, 0, PRIMARY_COLOR));
        
        balanceLabel = new JLabel("BALANCE: R0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setForeground(SECONDARY_COLOR);
        balanceLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel dateLabel = new JLabel("Date: " + dateFormat.format(new Date()));
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JButton clearConsoleBtn = new JButton("Clear Console");
        clearConsoleBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        clearConsoleBtn.setForeground(Color.BLACK);
        clearConsoleBtn.setBackground(LIGHT_BG);
        clearConsoleBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        clearConsoleBtn.addActionListener(e -> consoleArea.setText(""));
        
        statusBar.add(balanceLabel, BorderLayout.WEST);
        statusBar.add(clearConsoleBtn, BorderLayout.CENTER);
        statusBar.add(dateLabel, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
        
        balanceAnimationTimer = new Timer(20, e -> {
            double difference = targetBalance - currentBalance;
            if (Math.abs(difference) < 0.01) {
                currentBalance = targetBalance;
                balanceAnimationTimer.stop();
            } else {
                currentBalance += difference * 0.1;
            }
            updateBalanceDisplay();
        });
    }
    
    private void updateBalanceDisplay() {
        String formattedBalance = String.format("BALANCE: R%,.2f", currentBalance);
        balanceLabel.setText(formattedBalance);
        
        if (currentBalance >= 0) {
            balanceLabel.setForeground(POSITIVE_COLOR);
        } else {
            balanceLabel.setForeground(NEGATIVE_COLOR);
        }
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setBackground(DARK_BG);
        
        summaryPanel.add(createSummaryCard("TOTAL INCOME", "R0.00", POSITIVE_COLOR));
        summaryPanel.add(createSummaryCard("TOTAL EXPENSES", "R0.00", NEGATIVE_COLOR));
        summaryPanel.add(createSummaryCard("NET BALANCE", "R0.00", SECONDARY_COLOR));
        summaryPanel.add(createSummaryCard("TOTAL VAT", "R0.00", PRIMARY_COLOR));
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setBackground(DARK_BG);
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Income", 0);
        dataset.setValue("Expenses", 0);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Income vs Expenses", 
            dataset, 
            true, 
            true, 
            false
        );
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Income", POSITIVE_COLOR);
        plot.setSectionPaint("Expenses", NEGATIVE_COLOR);
        plot.setBackgroundPaint(DARK_BG);
        plot.setLabelBackgroundPaint(DARK_BG);
        plot.setLabelPaint(Color.WHITE);
        chart.setBackgroundPaint(DARK_BG);
        chart.getTitle().setPaint(SECONDARY_COLOR);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(DARK_BG);
        chartPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        centerPanel.add(chartPanel);
        
        String[] columns = {"ID", "DATE", "DESCRIPTION", "AMOUNT (ZAR)", "TYPE", "VAT"};
        transactionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(transactionsModel);
        styleTable(transactionsTable);
        
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        centerPanel.add(scrollPane);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(DARK_BG);
        
        JButton addButton = createStyledButton("ADD TRANSACTION");
        addButton.addActionListener(e -> showAddTransactionDialog());
        addButton.setForeground(Color.BLACK);
        
        JButton deleteButton = createStyledButton("DELETE SELECTED");
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        deleteButton.setForeground(Color.BLACK);
        
        JButton exportButton = createStyledButton("EXPORT TO CSV");
        exportButton.addActionListener(e -> exportToCSV());
        exportButton.setForeground(Color.BLACK);
        
        JButton importButton = createStyledButton("IMPORT CSV");
        importButton.addActionListener(e -> importFromCSV());
        importButton.setForeground(Color.BLACK);
        
        toolbar.add(addButton);
        toolbar.add(deleteButton);
        toolbar.add(exportButton);
        toolbar.add(importButton);
        panel.add(toolbar, BorderLayout.NORTH);
        
        transactionsModel = new DefaultTableModel(new String[]{"ID", "DATE", "DESCRIPTION", "AMOUNT (ZAR)", "TYPE", "VAT %"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(transactionsModel);
        styleTable(transactionsTable);
        
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("REPORTS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBackground(DARK_BG);
        
        JButton incomeReportBtn = createStyledButton("INCOME REPORT");
        incomeReportBtn.addActionListener(e -> generateReport("Income"));
        incomeReportBtn.setForeground(Color.BLACK);
        
        JButton expenseReportBtn = createStyledButton("EXPENSE REPORT");
        expenseReportBtn.addActionListener(e -> generateReport("Expense"));
        expenseReportBtn.setForeground(Color.BLACK);
        
        JButton summaryReportBtn = createStyledButton("FINANCIAL SUMMARY");
        summaryReportBtn.addActionListener(e -> generateReport("Summary"));
        summaryReportBtn.setForeground(Color.BLACK);
        
        JButton vatReportBtn = createStyledButton("VAT REPORT");
        vatReportBtn.addActionListener(e -> generateReport("VAT"));
        vatReportBtn.setForeground(Color.BLACK);
        
        buttonPanel.add(incomeReportBtn);
        buttonPanel.add(expenseReportBtn);
        buttonPanel.add(summaryReportBtn);
        buttonPanel.add(vatReportBtn);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCalculationsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("ADVANCED FINANCIAL CALCULATIONS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        calculationsPanel = new JPanel();
        calculationsPanel.setBackground(DARK_BG);
        calculationsPanel.setLayout(new BoxLayout(calculationsPanel, BoxLayout.Y_AXIS));
        
        // Create a scroll pane for the calculations panel
        JScrollPane scrollPane = new JScrollPane(calculationsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add calculation sections
        addCalculationSection("Income Calculations", new String[]{
            "Net Income = Total Revenue - Total Expenses"
        });
        
        addCalculationSection("Profit Calculations", new String[]{
            "Gross Profit = Sales Revenue - Cost of Goods Sold (COGS)",
            "Gross Profit Margin = (Gross Profit / Sales Revenue) × 100",
            "Net Profit Margin = (Net Profit / Total Revenue) × 100",
            "Markup % = [(Selling Price - Cost Price) / Cost Price] × 100"
        });
        
        addCalculationSection("Inventory Calculations", new String[]{
            "COGS = Opening Inventory + Purchases - Closing Inventory",
            "Inventory Turnover = Cost of Goods Sold / Average Inventory"
        });
        
        addCalculationSection("Financial Ratios", new String[]{
            "AR Turnover = Net Credit Sales / Average Accounts Receivable"
        });
        
        addCalculationSection("Business Metrics", new String[]{
            "Break-Even Sales = Fixed Costs / (Selling Price per Unit - Variable Cost per Unit)"
        });
        
        addCalculationSection("Accounting Equation", new String[]{
            "Assets = Liabilities + Owner's Equity"
        });
        
        // Add calculate button at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton calculateBtn = createStyledButton("CALCULATE ALL");
        calculateBtn.addActionListener(e -> performAllCalculations());
        calculateBtn.setForeground(Color.BLACK);
        buttonPanel.add(calculateBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void addCalculationSection(String title, String[] formulas) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setBackground(DARK_BG);
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, PRIMARY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 16));
        sectionTitle.setForeground(PRIMARY_COLOR);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        sectionPanel.add(sectionTitle);
        
        for (String formula : formulas) {
            JPanel formulaPanel = new JPanel(new GridBagLayout());
            formulaPanel.setBackground(DARK_BG);
            formulaPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Formula label
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.7;
            JLabel formulaLabel = new JLabel(formula);
            formulaLabel.setForeground(Color.WHITE);
            formulaLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            formulaPanel.add(formulaLabel, gbc);
            
            // Result field
            gbc.gridx = 1;
            gbc.weightx = 0.3;
            JTextField resultField = createStyledTextField();
            resultField.setEditable(false);
            resultField.setName(formula.split("=")[0].trim());
            formulaPanel.add(resultField, gbc);
            
            sectionPanel.add(formulaPanel);
        }
        
        calculationsPanel.add(sectionPanel);
    }
    
    private void performAllCalculations() {
        // Get all inventory transactions
        double openingInventory = 0;
        double purchases = 0;
        double closingInventory = 0;
        
        for (Transaction t : transactions) {
            if (t.getType().equals("Inventory")) {
                if (t.getDescription().contains("Opening")) {
                    openingInventory = t.getAmount();
                } else if (t.getDescription().contains("Purchase")) {
                    purchases = t.getAmount();
                } else if (t.getDescription().contains("Closing")) {
                    closingInventory = t.getAmount();
                }
            }
        }
        
        // Calculate total revenue and expenses
        double totalRevenue = 0;
        double totalExpenses = 0;
        double salesRevenue = 0;
        
        for (Transaction t : transactions) {
            if (t.getType().equals("Income")) {
                totalRevenue += t.getAmount();
                salesRevenue += t.getAmount();
            } else if (t.getType().equals("Expense")) {
                totalExpenses += t.getAmount();
            }
        }
        
        // Calculate all formulas and update UI
        for (Component comp : calculationsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel sectionPanel = (JPanel) comp;
                for (Component formulaComp : sectionPanel.getComponents()) {
                    if (formulaComp instanceof JPanel) {
                        JPanel formulaPanel = (JPanel) formulaComp;
                        for (Component fieldComp : formulaPanel.getComponents()) {
                            if (fieldComp instanceof JTextField) {
                                JTextField field = (JTextField) fieldComp;
                                String calculationName = field.getName();
                                
                                try {
                                    double result = 0;
                                    String formattedResult = "";
                                    
                                    switch (calculationName) {
                                        case "Net Income":
                                            result = calculateNetIncome(totalRevenue, totalExpenses);
                                            formattedResult = String.format("R%,.2f", result);
                                            break;
                                        case "Gross Profit":
                                            double cogs = calculateCOGS(openingInventory, purchases, closingInventory);
                                            result = calculateGrossProfit(salesRevenue, cogs);
                                            formattedResult = String.format("R%,.2f", result);
                                            break;
                                        case "Gross Profit Margin":
                                            double cogsForMargin = calculateCOGS(openingInventory, purchases, closingInventory);
                                            double grossProfit = calculateGrossProfit(salesRevenue, cogsForMargin);
                                            result = calculateGrossProfitMargin(grossProfit, salesRevenue);
                                            formattedResult = String.format("%,.2f%%", result);
                                            break;
                                        case "Net Profit Margin":
                                            double netIncome = calculateNetIncome(totalRevenue, totalExpenses);
                                            result = calculateNetProfitMargin(netIncome, totalRevenue);
                                            formattedResult = String.format("%,.2f%%", result);
                                            break;
                                        case "Markup %":
                                            double costPrice = 100;
                                            double sellingPrice = 150;
                                            result = calculateMarkupPercentage(costPrice, sellingPrice);
                                            formattedResult = String.format("%,.2f%%", result);
                                            break;
                                        case "COGS":
                                            result = calculateCOGS(openingInventory, purchases, closingInventory);
                                            formattedResult = String.format("R%,.2f", result);
                                            break;
                                        case "Inventory Turnover":
                                            double cogsForTurnover = calculateCOGS(openingInventory, purchases, closingInventory);
                                            double avgInventory = (openingInventory + closingInventory) / 2;
                                            result = calculateInventoryTurnover(cogsForTurnover, avgInventory);
                                            formattedResult = String.format("%,.2f", result);
                                            break;
                                        case "AR Turnover":
                                            double netCreditSales = salesRevenue * 0.8;
                                            double avgAccountsReceivable = 2000;
                                            result = calculateARTurnover(netCreditSales, avgAccountsReceivable);
                                            formattedResult = String.format("%,.2f", result);
                                            break;
                                        case "Break-Even Sales":
                                            double fixedCosts = 5000;
                                            double sellingPricePerUnit = 50;
                                            double variableCostPerUnit = 30;
                                            result = calculateBreakEvenSales(fixedCosts, sellingPricePerUnit, variableCostPerUnit);
                                            formattedResult = String.format("%,.2f units", result);
                                            break;
                                        case "Assets":
                                            double liabilities = 10000;
                                            double ownersEquity = 15000;
                                            result = calculateAssets(liabilities, ownersEquity);
                                            formattedResult = String.format("R%,.2f", result);
                                            break;
                                        default:
                                            formattedResult = "N/A";
                                    }
                                    
                                    field.setText(formattedResult);
                                } catch (Exception e) {
                                    field.setText("Error in calculation");
                                    logToConsole("Error calculating " + calculationName + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        logToConsole("All financial calculations completed");
    }
    
    private void importFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Transactions");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean headerSkipped = false;
                int importedCount = 0;
                
                while ((line = br.readLine()) != null) {
                    if (!headerSkipped) {
                        headerSkipped = true;
                        continue;
                    }
                    
                    String[] values = line.split(",");
                    if (values.length >= 6) {
                        try {
                            String id = values[0].trim();
                            String date = values[1].trim();
                            String description = values[2].trim();
                            double amount = Double.parseDouble(values[3].trim());
                            String type = values[4].trim();
                            int vatRate = Integer.parseInt(values[5].trim());
                            
                            transactions.add(new Transaction(id, date, description, amount, type, vatRate));
                            importedCount++;
                        } catch (NumberFormatException e) {
                            logToConsole("Error parsing line: " + line);
                        }
                    }
                }
                
                refreshTransactions();
                logToConsole("Imported " + importedCount + " transactions from: " + file.getName());
                JOptionPane.showMessageDialog(this, 
                    "Successfully imported " + importedCount + " transactions", 
                    "Import Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                logToConsole("Import failed: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Import failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Calculation methods
    private double calculateNetIncome(double totalRevenue, double totalExpenses) {
        return totalRevenue - totalExpenses;
    }
    
    private double calculateGrossProfit(double salesRevenue, double cogs) {
        return salesRevenue - cogs;
    }
    
    private double calculateCOGS(double openingInventory, double purchases, double closingInventory) {
        return openingInventory + purchases - closingInventory;
    }
    
    private double calculateMarkupPercentage(double costPrice, double sellingPrice) {
        return ((sellingPrice - costPrice) / costPrice) * 100;
    }
    
    private double calculateGrossProfitMargin(double grossProfit, double salesRevenue) {
        return (grossProfit / salesRevenue) * 100;
    }
    
    private double calculateNetProfitMargin(double netIncome, double totalRevenue) {
        return (netIncome / totalRevenue) * 100;
    }
    
    private double calculateInventoryTurnover(double cogs, double averageInventory) {
        return cogs / averageInventory;
    }
    
    private double calculateARTurnover(double netCreditSales, double averageAccountsReceivable) {
        return netCreditSales / averageAccountsReceivable;
    }
    
    private double calculateBreakEvenSales(double fixedCosts, double sellingPricePerUnit, double variableCostPerUnit) {
        return fixedCosts / (sellingPricePerUnit - variableCostPerUnit);
    }
    
    private double calculateAssets(double liabilities, double ownersEquity) {
        return liabilities + ownersEquity;
    }
    
    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        consoleArea = new JTextArea();
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(HIGHLIGHT_COLOR);
        consoleArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        consoleArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Helper methods
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_COLOR.darker(), 2),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(LIGHT_BG);
        field.setForeground(Color.WHITE);
        field.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LIGHT_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void styleTable(JTable table) {
        table.setBackground(LIGHT_BG);
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(HIGHLIGHT_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setGridColor(PRIMARY_COLOR);
        table.getTableHeader().setBackground(DARK_BG);
        table.getTableHeader().setForeground(SECONDARY_COLOR);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    }
    
    private void logToConsole(String message) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timestamp = timeFormat.format(new Date());
        consoleArea.append("[" + timestamp + "] " + message + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }
    
    private void showAddTransactionDialog() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField idField = createStyledTextField();
        idField.setText("TRX-" + (transactions.size() + 1001));
        idField.setEditable(false);
        
        JTextField dateField = createStyledTextField();
        dateField.setText(dateFormat.format(new Date()));
        
        JTextField descField = createStyledTextField();
        JTextField amountField = createStyledTextField();
        amountField.setText("R");
        
        JComboBox<String> typeField = new JComboBox<>(new String[]{"Income", "Expense", "Inventory"});
        typeField.setBackground(LIGHT_BG);
        typeField.setForeground(Color.WHITE);
        typeField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JSpinner vatSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 20, 1));
        vatSpinner.setBackground(LIGHT_BG);
        vatSpinner.setForeground(Color.WHITE);
        vatSpinner.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        
        panel.add(new JLabel("Transaction ID:"));
        panel.add(idField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Amount (ZAR):"));
        panel.add(amountField);
        panel.add(new JLabel("Type:"));
        panel.add(typeField);
        panel.add(new JLabel("VAT Rate (%):"));
        panel.add(vatSpinner);
        
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(Color.WHITE);
            }
        }
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add Transaction",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText();
                String date = dateField.getText();
                String description = descField.getText();
                
                String amountStr = amountField.getText().replace("R", "").trim();
                double amount = Double.parseDouble(amountStr);
                
                String type = (String) typeField.getSelectedItem();
                int vatRate = (int) vatSpinner.getValue();
                
                transactions.add(new Transaction(id, date, description, amount, type, vatRate));
                refreshTransactions();
                logToConsole("Added transaction: " + description + " (R" + amount + ") with VAT " + vatRate + "%");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow != -1) {
            String id = (String) transactionsTable.getValueAt(selectedRow, 0);
            transactions.remove(selectedRow);
            refreshTransactions();
            logToConsole("Deleted transaction: " + id);
        } else {
            JOptionPane.showMessageDialog(this, "No transaction selected", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Date,Description,Amount (ZAR),Type,VAT Rate");
                for (Transaction t : transactions) {
                    writer.println(t.toCSV());
                }
                logToConsole("Exported transactions to: " + file.getName());
                JOptionPane.showMessageDialog(this, "Export completed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                logToConsole("Export failed: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void generateReport(String reportType) {
        double totalIncome = 0;
        double totalExpense = 0;
        double totalVAT = 0;
        
        for (Transaction t : transactions) {
            if (t.getType().equals("Income")) {
                totalIncome += t.getAmount();
            } else if (t.getType().equals("Expense")) {
                totalExpense += t.getAmount();
                totalVAT += (t.getAmount() * t.getVatRate() / 100);
            }
        }
        
        String report;
        switch (reportType) {
            case "Income":
                report = "INCOME REPORT\n" +
                         "================\n" +
                         "Total Income: R" + String.format("%,.2f", totalIncome) + "\n" +
                         "Number of Transactions: " + transactions.stream().filter(t -> t.getType().equals("Income")).count() + "\n" +
                         "Generated on: " + dateFormat.format(new Date());
                break;
            case "Expense":
                report = "EXPENSE REPORT\n" +
                         "================\n" +
                         "Total Expenses: R" + String.format("%,.2f", totalExpense) + "\n" +
                         "Number of Transactions: " + transactions.stream().filter(t -> t.getType().equals("Expense")).count() + "\n" +
                         "Generated on: " + dateFormat.format(new Date());
                break;
            case "Summary":
                report = "FINANCIAL SUMMARY\n" +
                         "================\n" +
                         "Total Income: R" + String.format("%,.2f", totalIncome) + "\n" +
                         "Total Expenses: R" + String.format("%,.2f", totalExpense) + "\n" +
                         "Net Balance: R" + String.format("%,.2f", (totalIncome - totalExpense)) + "\n" +
                         "Generated on: " + dateFormat.format(new Date());
                break;
            case "VAT":
                report = "VAT REPORT\n" +
                         "================\n" +
                         "Total VAT Collected: R" + String.format("%,.2f", totalVAT) + "\n" +
                         "Generated on: " + dateFormat.format(new Date());
                break;
            default:
                report = "Invalid report type";
        }
        
        logToConsole("Generated " + reportType + " report");
        JOptionPane.showMessageDialog(this, report, reportType + " Report", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void refreshTransactions() {
        transactionsModel.setRowCount(0);
        
        double totalIncome = 0;
        double totalExpense = 0;
        double totalVAT = 0;
        
        for (Transaction t : transactions) {
            transactionsModel.addRow(new Object[]{
                t.getId(),
                t.getDate(),
                t.getDescription(),
                String.format("R%,.2f", t.getAmount()),
                t.getType(),
                t.getVatRate() + "%"
            });
            
            if (t.getType().equals("Income")) {
                totalIncome += t.getAmount();
            } else if (t.getType().equals("Expense")) {
                totalExpense += t.getAmount();
                totalVAT += (t.getAmount() * t.getVatRate() / 100);
            }
        }
        
        targetBalance = totalIncome - totalExpense;
        if (!balanceAnimationTimer.isRunning()) {
            balanceAnimationTimer.start();
        }
        
        Component dashboard = tabbedPane.getComponentAt(0);
        if (dashboard instanceof JPanel) {
            JPanel dashboardPanel = (JPanel) dashboard;
            Component[] components = dashboardPanel.getComponents();
            if (components.length > 0 && components[0] instanceof JPanel) {
                JPanel summaryPanel = (JPanel) components[0];
                if (summaryPanel.getComponentCount() >= 4) {
                    ((JLabel) ((JPanel) summaryPanel.getComponent(0)).getComponent(1)).setText(String.format("R%,.2f", totalIncome));
                    ((JLabel) ((JPanel) summaryPanel.getComponent(1)).getComponent(1)).setText(String.format("R%,.2f", totalExpense));
                    ((JLabel) ((JPanel) summaryPanel.getComponent(2)).getComponent(1)).setText(String.format("R%,.2f", targetBalance));
                    ((JLabel) ((JPanel) summaryPanel.getComponent(3)).getComponent(1)).setText(String.format("R%,.2f", totalVAT));
                }
            }
            
            for (Component comp : dashboardPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel centerPanel = (JPanel) comp;
                    for (Component chartComp : centerPanel.getComponents()) {
                        if (chartComp instanceof ChartPanel) {
                            ChartPanel chartPanel = (ChartPanel) chartComp;
                            DefaultPieDataset dataset = new DefaultPieDataset();
                            dataset.setValue("Income", totalIncome);
                            dataset.setValue("Expenses", totalExpense);
                            ((PiePlot) chartPanel.getChart().getPlot()).setDataset(dataset);
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            Application app = new Application();
            app.setMinimumSize(new Dimension(800, 600));
        });
    }
    
    // Data models
    static class Transaction {
        private String id;
        private String date;
        private String description;
        private double amount;
        private String type;
        private int vatRate;
        
        public Transaction(String id, String date, String description, double amount, String type, int vatRate) {
            this.id = id;
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.type = type;
            this.vatRate = vatRate;
        }
        
        public String getId() { return id; }
        public String getDate() { return date; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getType() { return type; }
        public int getVatRate() { return vatRate; }
        
        public String toCSV() {
            return String.format("%s,%s,%s,%.2f,%s,%d", id, date, description, amount, type, vatRate);
        }
    }
    
    static class User {
        private String username;
        private String password;
        private String fullName;
        
        public User(String username, String password, String fullName) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
    }
}