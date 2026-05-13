package test;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;

/**
 * Competitive Programming Test Case Generator
 * Giao diện Java Swing cho hệ thống phân tích đề thi IOI/ICPC và sinh testcase
 */
public class CompetitiveProgrammingIDE extends JFrame {

    // ===== COLOR PALETTE =====
    static final Color BG_DARK       = new Color(13, 17, 23);
    static final Color BG_PANEL      = new Color(21, 27, 35);
    static final Color BG_CARD       = new Color(30, 37, 48);
    static final Color BG_INPUT      = new Color(22, 27, 34);
    static final Color ACCENT_CYAN   = new Color(0, 212, 255);
    static final Color ACCENT_GREEN  = new Color(0, 255, 136);
    static final Color ACCENT_YELLOW = new Color(255, 189, 46);
    static final Color ACCENT_RED    = new Color(255, 85, 85);
    static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    static final Color BORDER_COLOR  = new Color(48, 59, 76);
    static final Color TEXT_PRIMARY  = new Color(230, 237, 243);
    static final Color TEXT_MUTED    = new Color(100, 120, 140);
    static final Color TEXT_CODE     = new Color(180, 210, 180);

    // ===== FONTS =====
    static final Font FONT_MONO      = new Font("JetBrains Mono", Font.PLAIN, 13);
    static final Font FONT_MONO_BOLD = new Font("JetBrains Mono", Font.BOLD, 13);
    static final Font FONT_UI        = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_UI_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 20);
    static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);

    // ===== PANELS =====
    private JPanel leftSidebar;
    private JPanel mainContent;
    private JPanel rightPanel;
    private JTabbedPane mainTabs;
    private JTabbedPane rightTabs;

    // === Problem Input ===
    private JTextArea problemTextArea;
    private JLabel dropZoneLabel;
    private JPanel dropZonePanel;
    private JComboBox<String> contestTypeCombo;
    private JComboBox<String> languageCombo;
    private JTextField problemTitleField;

    // === AI Analysis ===
    private JTextArea analysisOutputArea;
    private JTextArea constraintArea;
    private JProgressBar aiProgressBar;
    private JLabel aiStatusLabel;

    // === Testcase Panel ===
    private DefaultTableModel testcaseTableModel;
    private JTable testcaseTable;
    private JTextArea inputPreview;
    private JTextArea outputPreview;
    private JSpinner numTestcaseSpinner;
    private JComboBox<String> testcaseTypeCombo;

    // === Code Panel ===
    private JTextArea sampleCodeArea;
    private JTextArea checkerCodeArea;
    private JComboBox<String> codeTypeCombo;
    private JComboBox<String> codeLangCombo;
    private JTextArea compileOutputArea;
    private JProgressBar compileProgress;

    // === Verdict Panel ===
    private JPanel verdictPanel;
    private DefaultTableModel verdictTableModel;
    private JTable verdictTable;
    private JLabel summaryLabel;

    // === Status Bar ===
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private JLabel modelLabel;

    public CompetitiveProgrammingIDE() {
        setTitle("CP TestGen — Competitive Programming Test Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 950);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        UIManager.put("TabbedPane.selected", BG_CARD);
        UIManager.put("TabbedPane.background", BG_PANEL);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.contentAreaColor", BG_CARD);
        UIManager.put("TabbedPane.focus", ACCENT_CYAN);
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        UIManager.put("ScrollBar.track", BG_PANEL);

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainLayout(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ========================================================
    // TOP BAR
    // ========================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 22, 30), getWidth(), 0, new Color(20, 28, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Logo + Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("◈") {{
            setFont(new Font("Segoe UI Symbol", Font.BOLD, 26));
            setForeground(ACCENT_CYAN);
        }};
        JLabel titleLabel = new JLabel("CP TestGen") {{
            setFont(FONT_TITLE);
            setForeground(TEXT_PRIMARY);
        }};
        JLabel versionBadge = makeBadge("v1.0", ACCENT_PURPLE);

        logoPanel.add(logoIcon);
        logoPanel.add(titleLabel);
        logoPanel.add(versionBadge);
        bar.add(logoPanel, BorderLayout.WEST);

        // Center: workflow steps
        JPanel stepsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        stepsPanel.setOpaque(false);
        String[] steps = {"① Nhập Đề", "② Phân Tích AI", "③ Sinh Testcase", "④ Sinh Code", "⑤ Kiểm Tra"};
        Color[] stepColors = {ACCENT_CYAN, ACCENT_PURPLE, ACCENT_GREEN, ACCENT_YELLOW, ACCENT_RED};
        for (int i = 0; i < steps.length; i++) {
            final int idx = i;
            JLabel step = new JLabel(steps[i]) {{
                setFont(FONT_SMALL);
                setForeground(stepColors[idx]);
                setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(stepColors[idx], 1, 6),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }};
            stepsPanel.add(step);
            if (i < steps.length - 1) {
                stepsPanel.add(new JLabel("→") {{ setForeground(TEXT_MUTED); setFont(FONT_SMALL); }});
            }
        }
        bar.add(stepsPanel, BorderLayout.CENTER);

        // Right: action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(makeTopButton("⚙ Cài đặt", TEXT_MUTED));
        actionsPanel.add(makeTopButton("⟳ Reset", ACCENT_YELLOW));
        actionsPanel.add(makeTopButton("▶ Chạy Tất Cả", ACCENT_GREEN));
        bar.add(actionsPanel, BorderLayout.EAST);

        return bar;
    }

    // ========================================================
    // MAIN LAYOUT
    // ========================================================
    private JPanel buildMainLayout() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);

        // Left sidebar (problem input + settings)
        leftSidebar = buildLeftSidebar();
        leftSidebar.setPreferredSize(new Dimension(320, 0));

        // Center content (tabs: analysis, testcase, code, verdict)
        mainContent = buildMainContent();

        // Right panel (preview / terminal)
        rightPanel = buildRightPanel();
        rightPanel.setPreferredSize(new Dimension(340, 0));

        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSidebar, mainContent);
        leftSplit.setDividerSize(4);
        leftSplit.setDividerLocation(320);
        leftSplit.setBackground(BORDER_COLOR);
        leftSplit.setBorder(null);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightPanel);
        mainSplit.setDividerSize(4);
        mainSplit.setDividerLocation(1260);
        mainSplit.setBackground(BORDER_COLOR);
        mainSplit.setBorder(null);

        panel.add(mainSplit, BorderLayout.CENTER);
        return panel;
    }

    // ========================================================
    // LEFT SIDEBAR — Problem Input
    // ========================================================
    private JPanel buildLeftSidebar() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel header = makeSectionHeader("NHẬP ĐỀ THI", ACCENT_CYAN);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Contest type
        content.add(makeLabel("Loại đề thi:"));
        content.add(Box.createVerticalStrut(4));
        contestTypeCombo = makeCombo(new String[]{"IOI", "ICPC", "Codeforces", "USACO", "AtCoder", "Tùy chỉnh"});
        content.add(contestTypeCombo);
        content.add(Box.createVerticalStrut(10));

        // Problem title
        content.add(makeLabel("Tên bài toán:"));
        content.add(Box.createVerticalStrut(4));
        problemTitleField = makeTextField("VD: Shortest Path, Two Sum...");
        content.add(problemTitleField);
        content.add(Box.createVerticalStrut(10));

        // Language
        content.add(makeLabel("Ngôn ngữ code:"));
        content.add(Box.createVerticalStrut(4));
        languageCombo = makeCombo(new String[]{"C++17", "C++14", "Java 17", "Python 3", "Pascal"});
        content.add(languageCombo);
        content.add(Box.createVerticalStrut(14));

        // Separator
        content.add(makeDivider("NHẬP ĐỀ (VĂN BẢN / ẢNH)"));
        content.add(Box.createVerticalStrut(8));

        // Drop zone
        dropZonePanel = buildDropZone();
        content.add(dropZonePanel);
        content.add(Box.createVerticalStrut(8));

        // OR divider
        content.add(makeOrDivider());
        content.add(Box.createVerticalStrut(8));

        // Text problem input
        content.add(makeLabel("Dán đề bài vào đây:"));
        content.add(Box.createVerticalStrut(4));
        problemTextArea = new JTextArea(10, 20);
        problemTextArea.setFont(FONT_UI);
        problemTextArea.setBackground(BG_INPUT);
        problemTextArea.setForeground(TEXT_PRIMARY);
        problemTextArea.setCaretColor(ACCENT_CYAN);
        problemTextArea.setLineWrap(true);
        problemTextArea.setWrapStyleWord(true);
        problemTextArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 6),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        problemTextArea.setText("Nhập đề bài tại đây...\n\nVí dụ:\nGiven an array of n integers, find the maximum subarray sum.\n\nInput: First line n (1≤n≤10^5), second line n integers.\nOutput: Maximum sum.");
        problemTextArea.setForeground(TEXT_MUTED);
        problemTextArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (problemTextArea.getForeground().equals(TEXT_MUTED)) {
                    problemTextArea.setText("");
                    problemTextArea.setForeground(TEXT_PRIMARY);
                }
            }
        });
        JScrollPane scrollProblem = new JScrollPane(problemTextArea);
        styleScrollPane(scrollProblem);
        scrollProblem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        content.add(scrollProblem);
        content.add(Box.createVerticalStrut(14));

        // Analyze button
        JButton analyzeBtn = makeActionButton("🤖  Phân tích bằng AI", ACCENT_CYAN);
        analyzeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        content.add(analyzeBtn);
        content.add(Box.createVerticalStrut(6));

        JButton clearBtn = makeSecondaryButton("✕  Xóa tất cả");
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(clearBtn);

        JScrollPane sideScroll = new JScrollPane(content);
        styleScrollPane(sideScroll);
        sideScroll.setBorder(null);
        panel.add(sideScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildDropZone() {
        JPanel zone = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 212, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                float[] dash = {6f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, 0f));
                g2.setColor(new Color(0, 212, 255, 80));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            }
        };
        zone.setOpaque(false);
        zone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        zone.setPreferredSize(new Dimension(290, 90));
        zone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zone.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        JLabel icon = new JLabel("📁", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text1 = new JLabel("Kéo thả ảnh / PDF đề bài", SwingConstants.CENTER);
        text1.setFont(FONT_SMALL);
        text1.setForeground(TEXT_MUTED);
        text1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text2 = new JLabel("hoặc click để chọn file", SwingConstants.CENTER);
        text2.setFont(FONT_SMALL);
        text2.setForeground(new Color(0, 212, 255, 150));
        text2.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(Box.createVerticalGlue());
        inner.add(icon);
        inner.add(Box.createVerticalStrut(4));
        inner.add(text1);
        inner.add(text2);
        inner.add(Box.createVerticalGlue());
        zone.add(inner, BorderLayout.CENTER);

        return zone;
    }

    // ========================================================
    // MAIN CONTENT — Tabs
    // ========================================================
    private JPanel buildMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);

        mainTabs = new JTabbedPane(JTabbedPane.TOP);
        styleTabPane(mainTabs);

        mainTabs.addTab("  🔍 Phân tích AI  ", buildAnalysisTab());
        mainTabs.addTab("  📋 Testcase  ", buildTestcaseTab());
        mainTabs.addTab("  💻 Sinh Code  ", buildCodeTab());
        mainTabs.addTab("  ✅ Kiểm tra  ", buildVerdictTab());

        panel.add(mainTabs, BorderLayout.CENTER);
        return panel;
    }

    // ----- TAB 1: AI Analysis -----
    private JPanel buildAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top: AI status card
        JPanel statusCard = buildCardPanel();
        statusCard.setLayout(new BorderLayout(12, 0));
        statusCard.setPreferredSize(new Dimension(0, 70));

        JPanel statusLeft = new JPanel(new GridLayout(2, 1, 2, 2));
        statusLeft.setOpaque(false);
        aiStatusLabel = new JLabel("⏳  Đang chờ đề bài...");
        aiStatusLabel.setFont(FONT_UI_BOLD);
        aiStatusLabel.setForeground(ACCENT_YELLOW);
        JLabel modelInfo = new JLabel("Mô hình: Claude Sonnet  •  OCR: Google Vision API");
        modelInfo.setFont(FONT_SMALL);
        modelInfo.setForeground(TEXT_MUTED);
        statusLeft.add(aiStatusLabel);
        statusLeft.add(modelInfo);

        aiProgressBar = new JProgressBar(0, 100);
        aiProgressBar.setValue(0);
        aiProgressBar.setStringPainted(false);
        aiProgressBar.setBackground(BG_INPUT);
        aiProgressBar.setForeground(ACCENT_CYAN);
        aiProgressBar.setBorderPainted(false);
        aiProgressBar.setPreferredSize(new Dimension(0, 6));

        JPanel statusRight = new JPanel(new GridLayout(2, 1));
        statusRight.setOpaque(false);
        statusRight.add(new JLabel());
        JButton reAnalyzeBtn = makeSmallButton("↺ Phân tích lại", ACCENT_CYAN);
        statusRight.add(reAnalyzeBtn);

        statusCard.add(statusLeft, BorderLayout.CENTER);
        statusCard.add(statusRight, BorderLayout.EAST);
        statusCard.add(aiProgressBar, BorderLayout.SOUTH);

        panel.add(statusCard, BorderLayout.NORTH);

        // Middle split: Analysis output + Constraints
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setDividerLocation(0.6);
        split.setResizeWeight(0.6);

        // Left: analysis output
        JPanel analysisCard = buildCardPanel();
        analysisCard.setLayout(new BorderLayout(0, 6));
        analysisCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 8),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel analysisTitle = makeCardTitle("📄 Kết quả phân tích AI");
        analysisOutputArea = new JTextArea();
        analysisOutputArea.setFont(FONT_UI);
        analysisOutputArea.setBackground(BG_INPUT);
        analysisOutputArea.setForeground(TEXT_PRIMARY);
        analysisOutputArea.setCaretColor(ACCENT_CYAN);
        analysisOutputArea.setLineWrap(true);
        analysisOutputArea.setWrapStyleWord(true);
        analysisOutputArea.setEditable(false);
        analysisOutputArea.setText(
            "── Loại bài: Graph / Shortest Path\n" +
            "── Thuật toán gợi ý: Dijkstra, BFS\n" +
            "── Độ phức tạp: O((V+E) log V)\n\n" +
            "── Ràng buộc phát hiện:\n" +
            "   • n ≤ 10^5 (nodes)\n" +
            "   • m ≤ 2×10^5 (edges)\n" +
            "   • w ≤ 10^9 (weight)\n" +
            "   • Time limit: 2s\n" +
            "   • Memory: 256MB\n\n" +
            "── Các trường hợp đặc biệt:\n" +
            "   • Đồ thị không liên thông\n" +
            "   • Self-loop, multiple edges\n" +
            "   • Negative weights (cần xử lý)\n\n" +
            "── Gợi ý checker: Special judge cần\n" +
            "   so sánh giá trị tối ưu."
        );
        JScrollPane scrollAnalysis = new JScrollPane(analysisOutputArea);
        styleScrollPane(scrollAnalysis);
        analysisCard.add(analysisTitle, BorderLayout.NORTH);
        analysisCard.add(scrollAnalysis, BorderLayout.CENTER);
        split.setLeftComponent(analysisCard);

        // Right: constraints + tags
        JPanel rightInfo = new JPanel();
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));
        rightInfo.setBackground(BG_DARK);

        // Constraints card
        JPanel constraintsCard = buildCardPanel();
        constraintsCard.setLayout(new BorderLayout(0, 6));
        JLabel cTitle = makeCardTitle("⚙ Ràng buộc");
        constraintArea = new JTextArea();
        constraintArea.setFont(FONT_MONO);
        constraintArea.setBackground(BG_INPUT);
        constraintArea.setForeground(ACCENT_GREEN);
        constraintArea.setText("1 ≤ n ≤ 100000\n1 ≤ m ≤ 200000\n1 ≤ w ≤ 1000000000\nTime: 2000ms\nMemory: 256MB");
        JScrollPane scrollConstraint = new JScrollPane(constraintArea);
        styleScrollPane(scrollConstraint);
        constraintsCard.add(cTitle, BorderLayout.NORTH);
        constraintsCard.add(scrollConstraint, BorderLayout.CENTER);

        // Tags card
        JPanel tagsCard = buildCardPanel();
        tagsCard.setLayout(new BorderLayout(0, 6));
        JLabel tTitle = makeCardTitle("🏷 Tags");
        JPanel tagsFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tagsFlow.setBackground(BG_CARD);
        String[] tags = {"graph", "dijkstra", "shortest-path", "greedy", "heap"};
        Color[] tagColors = {ACCENT_CYAN, ACCENT_GREEN, ACCENT_PURPLE, ACCENT_YELLOW, ACCENT_RED};
        for (int i = 0; i < tags.length; i++) {
            tagsFlow.add(makeBadge(tags[i], tagColors[i % tagColors.length]));
        }
        tagsCard.add(tTitle, BorderLayout.NORTH);
        tagsCard.add(tagsFlow, BorderLayout.CENTER);

        rightInfo.add(constraintsCard);
        rightInfo.add(Box.createVerticalStrut(8));
        rightInfo.add(tagsCard);
        split.setRightComponent(rightInfo);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ----- TAB 2: Testcase -----
    private JPanel buildTestcaseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top toolbar
        JPanel toolbar = buildCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbar.setPreferredSize(new Dimension(0, 58));

        toolbar.add(makeLabel("Số testcase:"));
        numTestcaseSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        styleSpinner(numTestcaseSpinner);
        toolbar.add(numTestcaseSpinner);

        toolbar.add(makeSeparatorV());
        toolbar.add(makeLabel("Loại:"));
        testcaseTypeCombo = makeCombo(new String[]{"Ngẫu nhiên", "Edge Cases", "Stress Test", "Tất cả"});
        toolbar.add(testcaseTypeCombo);

        toolbar.add(makeSeparatorV());
        toolbar.add(makeSmallButton("⚡ Sinh Testcase", ACCENT_CYAN));
        toolbar.add(makeSmallButton("💾 Xuất file", ACCENT_GREEN));
        toolbar.add(makeSmallButton("🗑 Xóa tất cả", ACCENT_RED));

        panel.add(toolbar, BorderLayout.NORTH);

        // Center: split table + preview
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setBackground(BG_DARK);
        split.setDividerLocation(0.5);
        split.setResizeWeight(0.55);

        // Left: testcase table
        JPanel tableCard = buildCardPanel();
        tableCard.setLayout(new BorderLayout(0, 6));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        tableCard.add(makeCardTitle("📋 Danh sách Testcase"), BorderLayout.NORTH);

        String[] cols = {"#", "Loại", "Kích thước", "Trạng thái", "Thời gian"};
        testcaseTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        // Sample data
        Object[][] sampleData = {
            {"1", "Edge Case", "n=1", "✅ Valid", "—"},
            {"2", "Random", "n=1000", "✅ Valid", "12ms"},
            {"3", "Stress", "n=100000", "⚡ Running", "—"},
            {"4", "Edge Case", "n=0", "❌ Invalid", "—"},
            {"5", "Random", "n=50000", "✅ Valid", "34ms"},
            {"6", "Random", "n=80000", "⏳ Pending", "—"},
        };
        for (Object[] row : sampleData) testcaseTableModel.addRow(row);

        testcaseTable = new JTable(testcaseTableModel);
        styleTable(testcaseTable);
        testcaseTable.getColumnModel().getColumn(0).setMaxWidth(40);
        testcaseTable.getColumnModel().getColumn(2).setMaxWidth(100);
        testcaseTable.getColumnModel().getColumn(4).setMaxWidth(80);

        // Color rows by status
        testcaseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(0, 212, 255, 40) : (r % 2 == 0 ? BG_CARD : BG_INPUT));
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                String status = (String) t.getValueAt(r, 3);
                if (c == 3) {
                    if (status.contains("✅")) setForeground(ACCENT_GREEN);
                    else if (status.contains("❌")) setForeground(ACCENT_RED);
                    else if (status.contains("⚡")) setForeground(ACCENT_YELLOW);
                    else setForeground(TEXT_MUTED);
                }
                return this;
            }
        });

        JScrollPane scrollTable = new JScrollPane(testcaseTable);
        styleScrollPane(scrollTable);
        tableCard.add(scrollTable, BorderLayout.CENTER);
        split.setLeftComponent(tableCard);

        // Right: input/output preview
        JPanel previewPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        previewPanel.setBackground(BG_DARK);

        JPanel inputCard = buildCardPanel();
        inputCard.setLayout(new BorderLayout(0, 6));
        inputCard.add(makeCardTitle("📥 Input"), BorderLayout.NORTH);
        inputPreview = new JTextArea();
        inputPreview.setFont(FONT_MONO);
        inputPreview.setBackground(BG_INPUT);
        inputPreview.setForeground(ACCENT_GREEN);
        inputPreview.setCaretColor(ACCENT_CYAN);
        inputPreview.setText("5 6\n1 2 4\n1 3 2\n2 3 1\n2 4 5\n3 4 8\n4 5 2");
        JScrollPane scrollIn = new JScrollPane(inputPreview);
        styleScrollPane(scrollIn);
        inputCard.add(scrollIn, BorderLayout.CENTER);

        JPanel outputCard = buildCardPanel();
        outputCard.setLayout(new BorderLayout(0, 6));
        outputCard.add(makeCardTitle("📤 Expected Output"), BorderLayout.NORTH);
        outputPreview = new JTextArea();
        outputPreview.setFont(FONT_MONO);
        outputPreview.setBackground(BG_INPUT);
        outputPreview.setForeground(ACCENT_CYAN);
        outputPreview.setCaretColor(ACCENT_CYAN);
        outputPreview.setText("9");
        JScrollPane scrollOut = new JScrollPane(outputPreview);
        styleScrollPane(scrollOut);
        outputCard.add(scrollOut, BorderLayout.CENTER);

        previewPanel.add(inputCard);
        previewPanel.add(outputCard);
        split.setRightComponent(previewPanel);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ----- TAB 3: Code Generation -----
    private JPanel buildCodeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = buildCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbar.setPreferredSize(new Dimension(0, 58));

        toolbar.add(makeLabel("Loại code:"));
        codeTypeCombo = makeCombo(new String[]{"AC (Đúng)", "WA (Sai)", "TLE (Chậm)", "MLE (Bộ nhớ)", "Checker"});
        toolbar.add(codeTypeCombo);

        toolbar.add(makeSeparatorV());
        toolbar.add(makeLabel("Ngôn ngữ:"));
        codeLangCombo = makeCombo(new String[]{"C++17", "Java", "Python 3"});
        toolbar.add(codeLangCombo);

        toolbar.add(makeSeparatorV());
        toolbar.add(makeSmallButton("🤖 Sinh Code AI", ACCENT_PURPLE));
        toolbar.add(makeSmallButton("▶ Biên dịch", ACCENT_CYAN));
        toolbar.add(makeSmallButton("⚡ Chạy tất cả TC", ACCENT_GREEN));

        panel.add(toolbar, BorderLayout.NORTH);

        // Split: code editor + checker
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setBackground(BG_DARK);
        split.setDividerLocation(0.55);
        split.setResizeWeight(0.55);

        // Left: code editor
        JPanel codeCard = buildCardPanel();
        codeCard.setLayout(new BorderLayout(0, 6));
        JPanel codeHeader = new JPanel(new BorderLayout());
        codeHeader.setOpaque(false);
        codeHeader.add(makeCardTitle("💻 Code mẫu"), BorderLayout.WEST);
        JPanel codeBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        codeBadges.setOpaque(false);
        codeBadges.add(makeBadge("AC", ACCENT_GREEN));
        codeBadges.add(makeBadge("C++17", ACCENT_CYAN));
        codeHeader.add(codeBadges, BorderLayout.EAST);

        sampleCodeArea = new JTextArea();
        sampleCodeArea.setFont(FONT_MONO);
        sampleCodeArea.setBackground(new Color(16, 21, 28));
        sampleCodeArea.setForeground(TEXT_CODE);
        sampleCodeArea.setCaretColor(ACCENT_CYAN);
        sampleCodeArea.setSelectionColor(new Color(0, 212, 255, 60));
        sampleCodeArea.setText(
            "#include <bits/stdc++.h>\nusing namespace std;\n\ntypedef pair<long long,int> pli;\ntypedef vector<pli> vpli;\n\nconst long long INF = 1e18;\nint n, m;\nvpli adj[100005];\nlong long dist[100005];\n\nvoid dijkstra(int s) {\n    fill(dist, dist+n+1, INF);\n    priority_queue<pli, vector<pli>, greater<>> pq;\n    dist[s] = 0;\n    pq.push({0, s});\n    while (!pq.empty()) {\n        auto [d, u] = pq.top(); pq.pop();\n        if (d > dist[u]) continue;\n        for (auto [w, v] : adj[u]) {\n            if (dist[u] + w < dist[v]) {\n                dist[v] = dist[u] + w;\n                pq.push({dist[v], v});\n            }\n        }\n    }\n}\n\nint main() {\n    ios_base::sync_with_stdio(false);\n    cin.tie(NULL);\n    cin >> n >> m;\n    for (int i = 0; i < m; i++) {\n        int u, v; long long w;\n        cin >> u >> v >> w;\n        adj[u].push_back({w, v});\n        adj[v].push_back({w, u});\n    }\n    dijkstra(1);\n    cout << (dist[n] == INF ? -1 : dist[n]) << \"\\n\";\n    return 0;\n}"
        );
        JScrollPane scrollCode = new JScrollPane(sampleCodeArea);
        styleScrollPane(scrollCode);
        codeCard.add(codeHeader, BorderLayout.NORTH);
        codeCard.add(scrollCode, BorderLayout.CENTER);

        // Compile output
        compileProgress = new JProgressBar(0, 100);
        compileProgress.setValue(100);
        compileProgress.setBackground(BG_INPUT);
        compileProgress.setForeground(ACCENT_GREEN);
        compileProgress.setBorderPainted(false);
        compileProgress.setPreferredSize(new Dimension(0, 4));

        compileOutputArea = new JTextArea(4, 20);
        compileOutputArea.setFont(FONT_MONO);
        compileOutputArea.setBackground(new Color(10, 14, 20));
        compileOutputArea.setForeground(ACCENT_GREEN);
        compileOutputArea.setEditable(false);
        compileOutputArea.setText("✓  Biên dịch thành công  [0.34s]\n✓  Không có warning\n✓  Binary size: 48KB");
        JScrollPane scrollCompile = new JScrollPane(compileOutputArea);
        styleScrollPane(scrollCompile);
        scrollCompile.setPreferredSize(new Dimension(0, 100));

        JPanel codeFull = new JPanel(new BorderLayout(0, 4));
        codeFull.setBackground(BG_DARK);
        codeFull.add(codeCard, BorderLayout.CENTER);
        codeFull.add(compileProgress, BorderLayout.SOUTH);

        split.setLeftComponent(codeFull);

        // Right: checker
        JPanel checkerCard = buildCardPanel();
        checkerCard.setLayout(new BorderLayout(0, 6));
        JPanel checkerHeader = new JPanel(new BorderLayout());
        checkerHeader.setOpaque(false);
        checkerHeader.add(makeCardTitle("🔍 Checker"), BorderLayout.WEST);
        checkerHeader.add(makeBadge("Special Judge", ACCENT_YELLOW), BorderLayout.EAST);

        checkerCodeArea = new JTextArea();
        checkerCodeArea.setFont(FONT_MONO);
        checkerCodeArea.setBackground(new Color(16, 21, 28));
        checkerCodeArea.setForeground(TEXT_CODE);
        checkerCodeArea.setCaretColor(ACCENT_CYAN);
        checkerCodeArea.setText(
            "#include \"testlib.h\"\n#include <bits/stdc++.h>\nusing namespace std;\n\nint main(int argc, char* argv[]) {\n    registerChecker(argc, argv);\n    \n    long long ans = ans.readLong();\n    long long ja  = ouf.readLong();\n    \n    if (ans == ja)\n        quitf(_ok, \"Correct: %lld\", ja);\n    else\n        quitf(_wa, \"Expected %lld, got %lld\",\n              ans, ja);\n}"
        );
        JScrollPane scrollChecker = new JScrollPane(checkerCodeArea);
        styleScrollPane(scrollChecker);

        JPanel compilePane = new JPanel(new BorderLayout(0, 4));
        compilePane.setBackground(BG_DARK);
        JLabel compileLabel = makeCardTitle("🖥 Compile Output");
        compilePane.add(compileLabel, BorderLayout.NORTH);
        compilePane.add(scrollCompile, BorderLayout.CENTER);
        compilePane.setPreferredSize(new Dimension(0, 120));

        checkerCard.add(checkerHeader, BorderLayout.NORTH);
        checkerCard.add(scrollChecker, BorderLayout.CENTER);
        checkerCard.add(compilePane, BorderLayout.SOUTH);

        split.setRightComponent(checkerCard);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ----- TAB 4: Verdict / Result -----
    private JPanel buildVerdictTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Summary row
        JPanel summaryRow = new JPanel(new GridLayout(1, 5, 10, 0));
        summaryRow.setBackground(BG_DARK);
        summaryRow.setPreferredSize(new Dimension(0, 90));

        summaryRow.add(buildStatCard("Tổng TC", "20", TEXT_PRIMARY));
        summaryRow.add(buildStatCard("✅ AC", "14", ACCENT_GREEN));
        summaryRow.add(buildStatCard("❌ WA", "3", ACCENT_RED));
        summaryRow.add(buildStatCard("⏱ TLE", "2", ACCENT_YELLOW));
        summaryRow.add(buildStatCard("💀 MLE", "1", ACCENT_PURPLE));

        panel.add(summaryRow, BorderLayout.NORTH);

        // Verdict table
        JPanel tableCard = buildCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.add(makeCardTitle("📊 Kết quả chi tiết"), BorderLayout.NORTH);

        String[] vcols = {"TC", "Loại Input", "Code AC", "Code WA", "Code TLE", "Checker", "Thời gian AC", "Thời gian WA"};
        verdictTableModel = new DefaultTableModel(vcols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        Object[][] vdata = {
            {"1", "Edge n=1",   "✅ AC", "✅ AC",  "✅ AC",  "✅ OK", "8ms",   "9ms"},
            {"2", "Random 1k",  "✅ AC", "❌ WA",  "✅ AC",  "✅ OK", "34ms",  "36ms"},
            {"3", "Stress 100k","✅ AC", "✅ AC",  "⏱ TLE", "✅ OK", "312ms", "—"},
            {"4", "Edge cycle",  "✅ AC", "❌ WA",  "✅ AC",  "✅ OK", "6ms",   "5ms"},
            {"5", "Random 50k", "✅ AC", "✅ AC",  "✅ AC",  "✅ OK", "198ms", "201ms"},
            {"6", "Disconn.",   "✅ AC", "❌ WA",  "⏱ TLE", "✅ OK", "4ms",   "—"},
        };
        for (Object[] row : vdata) verdictTableModel.addRow(row);

        verdictTable = new JTable(verdictTableModel);
        styleTable(verdictTable);
        verdictTable.getColumnModel().getColumn(0).setMaxWidth(40);
        verdictTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(0, 212, 255, 40) : (r % 2 == 0 ? BG_CARD : BG_INPUT));
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                String s = v == null ? "" : v.toString();
                if (s.contains("✅") || s.contains("AC") || s.contains("OK")) setForeground(ACCENT_GREEN);
                else if (s.contains("❌") || s.contains("WA")) setForeground(ACCENT_RED);
                else if (s.contains("⏱") || s.contains("TLE")) setForeground(ACCENT_YELLOW);
                else if (s.contains("💀") || s.contains("MLE")) setForeground(ACCENT_PURPLE);
                return this;
            }
        });

        JScrollPane scrollVerdict = new JScrollPane(verdictTable);
        styleScrollPane(scrollVerdict);
        tableCard.add(scrollVerdict, BorderLayout.CENTER);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottomBar.setOpaque(false);
        bottomBar.add(makeSmallButton("📤 Xuất báo cáo", ACCENT_CYAN));
        bottomBar.add(makeSmallButton("🔄 Sinh thêm TC mạnh hơn", ACCENT_YELLOW));
        bottomBar.add(makeSmallButton("✅ Hoàn thành", ACCENT_GREEN));
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // ========================================================
    // RIGHT PANEL — Terminal + File Explorer
    // ========================================================
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        JPanel header = makeSectionHeader("TERMINAL & FILES", ACCENT_GREEN);
        panel.add(header, BorderLayout.NORTH);

        rightTabs = new JTabbedPane(JTabbedPane.TOP);
        styleTabPane(rightTabs);
        rightTabs.setFont(FONT_SMALL);

        rightTabs.addTab(" Terminal ", buildTerminalPanel());
        rightTabs.addTab(" Files ", buildFilesPanel());
        rightTabs.addTab(" Log ", buildLogPanel());

        panel.add(rightTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTerminalPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);

        JTextArea terminal = new JTextArea();
        terminal.setFont(FONT_MONO);
        terminal.setBackground(new Color(8, 12, 16));
        terminal.setForeground(ACCENT_GREEN);
        terminal.setCaretColor(ACCENT_GREEN);
        terminal.setText(
            "$ ./gen 1 > test1.txt\n" +
            "$ ./sol < test1.txt > out1.txt\n" +
            "✓  Correct [8ms]\n\n" +
            "$ ./gen 2 > test2.txt\n" +
            "$ ./sol < test2.txt > out2.txt\n" +
            "✓  Correct [34ms]\n\n" +
            "$ ./gen 3 > test3.txt\n" +
            "$ ./brute < test3.txt > ans3.txt\n" +
            "$ ./sol < test3.txt > out3.txt\n" +
            "✗  WRONG ANSWER on test 3!\n" +
            "  Expected: 42\n" +
            "  Got:      39\n\n" +
            "$ # Investigating...\n" +
            "▊"
        );
        terminal.setEditable(false);
        JScrollPane scroll = new JScrollPane(terminal);
        styleScrollPane(scroll);
        scroll.setBorder(null);

        JPanel inputBar = new JPanel(new BorderLayout(4, 0));
        inputBar.setBackground(new Color(8, 12, 16));
        inputBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        inputBar.setPreferredSize(new Dimension(0, 36));
        JLabel prompt = new JLabel("  $ ");
        prompt.setFont(FONT_MONO_BOLD);
        prompt.setForeground(ACCENT_GREEN);
        JTextField cmdField = new JTextField();
        cmdField.setFont(FONT_MONO);
        cmdField.setBackground(new Color(8, 12, 16));
        cmdField.setForeground(ACCENT_GREEN);
        cmdField.setCaretColor(ACCENT_GREEN);
        cmdField.setBorder(null);
        inputBar.add(prompt, BorderLayout.WEST);
        inputBar.add(cmdField, BorderLayout.CENTER);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(inputBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFilesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        DefaultListModel<String> fileModel = new DefaultListModel<>();
        String[] files = {
            "📁 input/", "   📄 test1.txt", "   📄 test2.txt", "   📄 test3.txt",
            "📁 output/", "   📄 out1.txt", "   📄 out2.txt",
            "📁 code/", "   💻 solution.cpp", "   💻 brute.cpp", "   🔍 checker.cpp",
            "📄 problem.pdf", "📄 analysis.json"
        };
        for (String f : files) fileModel.addElement(f);

        JList<String> fileList = new JList<>(fileModel);
        fileList.setFont(FONT_MONO);
        fileList.setBackground(BG_INPUT);
        fileList.setForeground(TEXT_PRIMARY);
        fileList.setSelectionBackground(new Color(0, 212, 255, 50));
        fileList.setSelectionForeground(ACCENT_CYAN);
        fileList.setCellRenderer((list, value, index, sel, foc) -> {
            JLabel lbl = new JLabel(value);
            lbl.setFont(FONT_MONO);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            lbl.setBackground(sel ? new Color(0, 212, 255, 40) : BG_INPUT);
            lbl.setForeground(value.startsWith("📁") ? ACCENT_YELLOW :
                              value.contains(".cpp") ? ACCENT_CYAN :
                              value.contains(".txt") ? TEXT_PRIMARY : TEXT_MUTED);
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(fileList);
        styleScrollPane(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);

        JTextArea log = new JTextArea();
        log.setFont(new Font("Consolas", Font.PLAIN, 11));
        log.setBackground(BG_INPUT);
        log.setForeground(TEXT_MUTED);
        log.setEditable(false);
        log.setText(
            "[14:32:01] INFO  Bắt đầu phân tích đề\n" +
            "[14:32:03] INFO  Gọi Claude API...\n" +
            "[14:32:08] ✓    Phân tích thành công\n" +
            "[14:32:08] INFO  Sinh testcase batch 1/3\n" +
            "[14:32:10] ✓    10 testcase sinh xong\n" +
            "[14:32:11] INFO  Biên dịch solution.cpp\n" +
            "[14:32:11] ✓    Biên dịch OK\n" +
            "[14:32:12] INFO  Chạy tất cả testcase...\n" +
            "[14:32:14] ✓    TC 1: AC (8ms)\n" +
            "[14:32:14] ✓    TC 2: AC (34ms)\n" +
            "[14:32:15] ✗    TC 3: WA!\n" +
            "[14:32:15] WARN  Testcase 3 phát hiện bug\n"
        );
        JScrollPane scroll = new JScrollPane(log);
        styleScrollPane(scroll);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ========================================================
    // STATUS BAR
    // ========================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        statusLabel = makeStatusLabel("● Sẵn sàng", ACCENT_GREEN);
        left.add(statusLabel);
        left.add(makeStatusLabel("│", BORDER_COLOR));
        left.add(makeStatusLabel("IOI Mode", ACCENT_CYAN));
        left.add(makeStatusLabel("│", BORDER_COLOR));
        left.add(makeStatusLabel("C++17", TEXT_MUTED));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        modelLabel = makeStatusLabel("Claude Sonnet 3.7", ACCENT_PURPLE);
        connectionLabel = makeStatusLabel("● Kết nối", ACCENT_GREEN);
        right.add(modelLabel);
        right.add(makeStatusLabel("│", BORDER_COLOR));
        right.add(connectionLabel);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ========================================================
    // HELPER / FACTORY METHODS
    // ========================================================
    private JPanel buildCardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 8),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return card;
    }

    private JPanel buildStatCard(String label, String value, Color valueColor) {
        JPanel card = buildCardPanel();
        card.setLayout(new GridLayout(2, 1, 0, 4));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Segoe UI", Font.BOLD, 26));
        val.setForeground(valueColor);
        card.add(val);
        card.add(lbl);
        return card;
    }

    private JPanel makeSectionHeader(String title, Color accent) {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.setColor(accent);
                g2.fillRect(0, 0, 3, getHeight());
            }
        };
        h.setPreferredSize(new Dimension(0, 38));
        h.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(accent);
        h.add(lbl);
        return h;
    }

    private JLabel makeCardTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_UI_BOLD);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_UI);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeBadge(String text, Color color) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL);
        badge.setForeground(color);
        badge.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80), 1, 6),
            BorderFactory.createEmptyBorder(2, 7, 2, 7)
        ));
        badge.setOpaque(false);
        return badge;
    }

    private JLabel makeStatusLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(color);
        return lbl;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_UI);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 1, 6),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return tf;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_UI);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(new RoundedBorder(BORDER_COLOR, 1, 6));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        combo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, i, sel, foc);
                setBackground(sel ? new Color(0, 212, 255, 50) : BG_INPUT);
                setForeground(sel ? ACCENT_CYAN : TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                setFont(FONT_UI);
                return this;
            }
        });
        return combo;
    }

    private JButton makeActionButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? color.darker() :
                           getModel().isRollover() ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 180) : color;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_UI_BOLD);
        btn.setForeground(new Color(10, 15, 20));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_UI);
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_INPUT);
        btn.setOpaque(true);
        btn.setBorder(new RoundedBorder(BORDER_COLOR, 1, 6));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton makeSmallButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ?
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 50) :
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 20);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setForeground(color);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80), 1, 6),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton makeTopButton(String text, Color color) {
        JButton btn = makeSmallButton(text, color);
        btn.setFont(FONT_UI);
        return btn;
    }

    private JPanel makeDivider(String label) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(BORDER_COLOR);
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER_COLOR);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        p.add(sep1, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        p.add(sep2, BorderLayout.EAST);
        return p;
    }

    private JPanel makeOrDivider() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JSeparator s1 = new JSeparator(); s1.setForeground(BORDER_COLOR);
        JSeparator s2 = new JSeparator(); s2.setForeground(BORDER_COLOR);
        JLabel or = new JLabel("HOẶC", SwingConstants.CENTER);
        or.setFont(new Font("Segoe UI", Font.BOLD, 10));
        or.setForeground(TEXT_MUTED);
        p.add(s1, BorderLayout.WEST);
        p.add(or, BorderLayout.CENTER);
        p.add(s2, BorderLayout.EAST);
        return p;
    }

    private JSeparator makeSeparatorV() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(BORDER_COLOR);
        sep.setPreferredSize(new Dimension(1, 24));
        return sep;
    }

    private void styleScrollPane(JScrollPane sp) {
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_INPUT);
        sp.getVerticalScrollBar().setBackground(BG_PANEL);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = BORDER_COLOR; trackColor = BG_PANEL;
            }
            protected JButton createDecreaseButton(int o) { return makeZeroButton(); }
            protected JButton createIncreaseButton(int o) { return makeZeroButton(); }
            private JButton makeZeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
        sp.getHorizontalScrollBar().setBackground(BG_PANEL);
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_UI);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0, 212, 255, 40));
        table.setSelectionForeground(ACCENT_CYAN);
        table.getTableHeader().setFont(FONT_UI_BOLD);
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void styleTabPane(JTabbedPane tabs) {
        tabs.setBackground(BG_PANEL);
        tabs.setForeground(TEXT_MUTED);
        tabs.setFont(FONT_UI_BOLD);
        tabs.setBorder(null);
        tabs.setTabPlacement(JTabbedPane.TOP);
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_UI);
        spinner.setBackground(BG_INPUT);
        spinner.setForeground(TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(BG_INPUT);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFont(FONT_UI);
        spinner.setBorder(new RoundedBorder(BORDER_COLOR, 1, 6));
        spinner.setPreferredSize(new Dimension(80, 32));
    }

    // ========================================================
    // ROUNDED BORDER HELPER
    // ========================================================
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;
        RoundedBorder(Color c, int t, int r) { color = c; thickness = t; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    // ========================================================
    // MAIN
    // ========================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            CompetitiveProgrammingIDE ide = new CompetitiveProgrammingIDE();
            ide.setVisible(true);
        });
    }
}