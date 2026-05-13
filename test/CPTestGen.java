package test;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * CP TestGen — Giao diện đơn giản sinh testcase cho IOI/ICPC
 * Chỉ cần JDK, không cần thư viện ngoài
 */
public class CPTestGen extends JFrame {

    // --- Màu sắc ---
    static final Color C_BG      = new Color(245, 246, 248);
    static final Color C_WHITE   = Color.WHITE;
    static final Color C_BORDER  = new Color(220, 222, 228);
    static final Color C_PRIMARY = new Color(59, 130, 246);
    static final Color C_GREEN   = new Color(34, 197, 94);
    static final Color C_RED     = new Color(239, 68, 68);
    static final Color C_YELLOW  = new Color(234, 179, 8);
    static final Color C_TEXT    = new Color(30, 30, 35);
    static final Color C_MUTED   = new Color(120, 125, 140);

    // --- Font ---
    static final Font F_NORMAL = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    static final Font F_BOLD   = new Font("Segoe UI Emoji", Font.BOLD, 13);
    static final Font F_TITLE  = new Font("Segoe UI Emoji", Font.BOLD, 15);
    static final Font F_MONO   = new Font("Consolas", Font.PLAIN, 13);

    // --- Components ---
    private JTextArea  problemArea;
    private JTextArea  analysisArea;
    private JTextArea  inputArea;
    private JTextArea  outputArea;
    private JTextArea  codeArea;
    private JTextArea  compileArea;
    private JTable     testcaseTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> contestCombo;
    private JComboBox<String> langCombo;
    private JComboBox<String> codeTypeCombo;
    private JSpinner   numSpinner;
    private JLabel     statusLabel;
    private JProgressBar progressBar;
    private JTabbedPane tabs;

    public CPTestGen() {
        setTitle("CP TestGen");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(8, 8));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ─── TOP BAR ──────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Left: title + combos
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("CP TestGen");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(C_PRIMARY);
        left.add(title);

        left.add(sep());
        left.add(label("Loại đề:"));
        contestCombo = combo(new String[]{"IOI", "ICPC", "Codeforces", "USACO", "AtCoder"});
        left.add(contestCombo);

        left.add(label("Ngôn ngữ:"));
        langCombo = combo(new String[]{"C++17", "Java", "Python 3"});
        left.add(langCombo);

        bar.add(left, BorderLayout.WEST);

        // Right: main action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btn("Phân tích AI", C_PRIMARY));
        right.add(btn("Sinh Testcase", C_GREEN));
        right.add(btn("Chạy tất cả", new Color(139, 92, 246)));
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // ─── CENTER: split left + right ───────────────────────
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildLeftPanel(), buildRightTabs());
        split.setDividerLocation(340);
        split.setDividerSize(5);
        split.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        split.setBackground(C_BG);
        return split;
    }

    // ─── LEFT: Nhập đề ────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            panel.getBorder(),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Header
        panel.add(sectionTitle("Nhập đề bài"), BorderLayout.NORTH);

        // Center: text area
        problemArea = new JTextArea();
        problemArea.setFont(F_NORMAL);
        problemArea.setLineWrap(true);
        problemArea.setWrapStyleWord(true);
        problemArea.setText(
            "Dán đề bài vào đây...\n\n" +
            "Ví dụ:\nGiven an array of n integers, " +
            "find the maximum subarray sum.\n\n" +
            "Input: n, then n integers\n" +
            "Output: maximum sum\nConstraints: 1≤n≤10^5"
        );
        problemArea.setForeground(C_MUTED);
        problemArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (problemArea.getForeground().equals(C_MUTED)) {
                    problemArea.setText("");
                    problemArea.setForeground(C_TEXT);
                }
            }
        });
        JScrollPane scroll = scrollPane(problemArea);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom buttons
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        JButton uploadBtn = btn("Tải file / ảnh", C_MUTED);
        JButton clearBtn  = btn("Xóa", C_RED);
        btns.add(uploadBtn);
        btns.add(clearBtn);
        panel.add(btns, BorderLayout.SOUTH);

        return panel;
    }

    // ─── RIGHT: Tabs ──────────────────────────────────────
    private JTabbedPane buildRightTabs() {
        tabs = new JTabbedPane();
        tabs.setFont(F_BOLD);
        tabs.setBackground(C_BG);
        tabs.addTab("Phân tích",  buildAnalysisTab());
        tabs.addTab("Testcase",   buildTestcaseTab());
        tabs.addTab("Code",       buildCodeTab());
        tabs.addTab("Kết quả",    buildVerdictTab());
        return tabs;
    }

    // ─── TAB 1: Phân tích AI ──────────────────────────────
    private JPanel buildAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        // Progress
        JPanel progressPanel = new JPanel(new BorderLayout(8, 0));
        progressPanel.setOpaque(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setForeground(C_PRIMARY);
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressPanel.add(new JLabel("Trạng thái:") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        panel.add(progressPanel, BorderLayout.NORTH);

        // Analysis output
        analysisArea = new JTextArea();
        analysisArea.setFont(F_NORMAL);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setText(
            "Loại bài: Graph / Shortest Path\n" +
            "Thuật toán: Dijkstra, BFS\n" +
            "Độ phức tạp: O((V+E) log V)\n\n" +
            "Ràng buộc:\n" +
            "  n ≤ 100,000\n  m ≤ 200,000\n  w ≤ 10^9\n" +
            "  Time limit: 2s | Memory: 256MB\n\n" +
            "Edge cases cần test:\n" +
            "  • Đồ thị không liên thông\n" +
            "  • Self-loop, multiple edges\n" +
            "  • n = 1\n\n" +
            "Checker: Cần special judge so sánh giá trị."
        );
        panel.add(scrollPane(analysisArea), BorderLayout.CENTER);

        return panel;
    }

    // ─── TAB 2: Testcase ──────────────────────────────────
    private JPanel buildTestcaseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);
        toolbar.add(label("Số TC:"));
        numSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        numSpinner.setFont(F_NORMAL);
        numSpinner.setPreferredSize(new Dimension(70, 30));
        toolbar.add(numSpinner);
        toolbar.add(combo(new String[]{"Ngẫu nhiên", "Edge Cases", "Stress"}));
        toolbar.add(btn("Sinh", C_PRIMARY));
        toolbar.add(btn("Xuất file", C_GREEN));
        toolbar.add(btn("Xóa", C_RED));
        panel.add(toolbar, BorderLayout.NORTH);

        // Split: table | input/output preview
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(340);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(C_BG);

        // Table
        String[] cols = {"#", "Loại", "Trạng thái", "Thời gian"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        Object[][] rows = {
            {"1", "Edge n=1",   "✅ Valid",   "—"},
            {"2", "Random 1k",  "✅ Valid",   "12ms"},
            {"3", "Stress 100k","⏳ Running", "—"},
            {"4", "Edge n=0",   "❌ Invalid",  "—"},
            {"5", "Random 50k", "✅ Valid",   "34ms"},
        };
        for (Object[] r : rows) tableModel.addRow(r);
        testcaseTable = new JTable(tableModel);
        testcaseTable.setFont(F_NORMAL);
        testcaseTable.setRowHeight(28);
        testcaseTable.setShowGrid(false);
        testcaseTable.setIntercellSpacing(new Dimension(0, 1));
        testcaseTable.getTableHeader().setFont(F_BOLD);
        testcaseTable.getTableHeader().setBackground(new Color(248, 249, 251));
        testcaseTable.getColumnModel().getColumn(0).setMaxWidth(35);
        testcaseTable.getColumnModel().getColumn(3).setMaxWidth(70);
        testcaseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(219, 234, 254) : (r % 2 == 0 ? C_WHITE : new Color(250, 251, 252)));
                setForeground(C_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                String s = v == null ? "" : v.toString();
                if (c == 2) {
                    if (s.contains("✅")) setForeground(new Color(22, 163, 74));
                    else if (s.contains("❌")) setForeground(C_RED);
                    else setForeground(C_YELLOW);
                }
                return this;
            }
        });
        split.setLeftComponent(scrollPane(testcaseTable));

        // Input/Output preview
        JPanel preview = new JPanel(new GridLayout(2, 1, 0, 8));
        preview.setBackground(C_BG);

        JPanel inputCard = card();
        inputCard.setLayout(new BorderLayout(0, 4));
        inputCard.setBorder(BorderFactory.createCompoundBorder(inputCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        inputCard.add(new JLabel("Input") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        inputArea = new JTextArea("5 6\n1 2 4\n1 3 2\n2 3 1\n2 4 5\n3 4 8\n4 5 2");
        inputArea.setFont(F_MONO);
        inputArea.setForeground(new Color(20, 130, 60));
        inputCard.add(scrollPane(inputArea), BorderLayout.CENTER);

        JPanel outputCard = card();
        outputCard.setLayout(new BorderLayout(0, 4));
        outputCard.setBorder(BorderFactory.createCompoundBorder(outputCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        outputCard.add(new JLabel("Expected Output") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        outputArea = new JTextArea("9");
        outputArea.setFont(F_MONO);
        outputArea.setForeground(C_PRIMARY);
        outputCard.add(scrollPane(outputArea), BorderLayout.CENTER);

        preview.add(inputCard);
        preview.add(outputCard);
        split.setRightComponent(preview);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ─── TAB 3: Code ──────────────────────────────────────
    private JPanel buildCodeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);
        toolbar.add(label("Loại:"));
        codeTypeCombo = combo(new String[]{"AC (Đúng)", "WA (Sai)", "TLE (Chậm)", "Checker"});
        toolbar.add(codeTypeCombo);
        toolbar.add(btn("Sinh Code AI", C_PRIMARY));
        toolbar.add(btn("Biên dịch", new Color(234, 88, 12)));
        toolbar.add(btn("Chạy TC", C_GREEN));
        panel.add(toolbar, BorderLayout.NORTH);

        // Split: code | compile output
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(380);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(C_BG);

        // Code editor
        JPanel codeCard = card();
        codeCard.setLayout(new BorderLayout(0, 4));
        codeCard.setBorder(BorderFactory.createCompoundBorder(codeCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        codeCard.add(new JLabel("Code mẫu") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        codeArea = new JTextArea();
        codeArea.setFont(F_MONO);
        codeArea.setForeground(new Color(30, 80, 160));
        codeArea.setText(
            "#include <bits/stdc++.h>\nusing namespace std;\n\ntypedef pair<long long,int> pli;\nconst long long INF = 1e18;\nint n, m;\nvector<pli> adj[100005];\nlong long dist[100005];\n\nvoid dijkstra(int s) {\n    fill(dist, dist+n+1, INF);\n    priority_queue<pli, vector<pli>, greater<>> pq;\n    dist[s] = 0; pq.push({0, s});\n    while (!pq.empty()) {\n        auto [d, u] = pq.top(); pq.pop();\n        if (d > dist[u]) continue;\n        for (auto [w, v] : adj[u])\n            if (dist[u]+w < dist[v])\n                pq.push({dist[v]=dist[u]+w, v});\n    }\n}\n\nint main() {\n    cin >> n >> m;\n    for (int i = 0; i < m; i++) {\n        int u, v; long long w;\n        cin >> u >> v >> w;\n        adj[u].push_back({w, v});\n        adj[v].push_back({w, u});\n    }\n    dijkstra(1);\n    cout << (dist[n]==INF ? -1 : dist[n]) << \"\\n\";\n}"
        );
        codeCard.add(scrollPane(codeArea), BorderLayout.CENTER);
        split.setTopComponent(codeCard);

        // Compile output
        JPanel compileCard = card();
        compileCard.setLayout(new BorderLayout(0, 4));
        compileCard.setBorder(BorderFactory.createCompoundBorder(compileCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        compileCard.add(new JLabel("Compile / Run Output") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        compileArea = new JTextArea();
        compileArea.setFont(F_MONO);
        compileArea.setEditable(false);
        compileArea.setForeground(new Color(22, 163, 74));
        compileArea.setText("✓  Biên dịch thành công (0.31s)\n✓  Chạy TC 1: AC (8ms)\n✓  Chạy TC 2: AC (34ms)\n✗  Chạy TC 3: WA!\n   Expected: 42   Got: 39");
        compileCard.add(scrollPane(compileArea), BorderLayout.CENTER);
        split.setBottomComponent(compileCard);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ─── TAB 4: Kết quả ───────────────────────────────────
    private JPanel buildVerdictTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 5, 8, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 72));
        stats.add(statCard("Tổng TC",  "20",  C_TEXT));
        stats.add(statCard("✅ AC",    "14",  new Color(22, 163, 74)));
        stats.add(statCard("❌ WA",    "3",   C_RED));
        stats.add(statCard("⏱ TLE",   "2",   C_YELLOW));
        stats.add(statCard("💀 MLE",   "1",   new Color(139, 92, 246)));
        panel.add(stats, BorderLayout.NORTH);

        // Verdict table
        String[] cols = {"TC", "Input", "AC", "WA", "TLE", "Thời gian"};
        DefaultTableModel vm = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        Object[][] vrows = {
            {"1", "Edge n=1",   "✅", "✅", "✅", "8ms"},
            {"2", "Random 1k",  "✅", "❌", "✅", "34ms"},
            {"3", "Stress 100k","✅", "✅", "⏱", "312ms"},
            {"4", "Edge cycle", "✅", "❌", "✅", "6ms"},
            {"5", "Disconnect", "✅", "❌", "⏱", "4ms"},
        };
        for (Object[] r : vrows) vm.addRow(r);
        JTable vt = new JTable(vm);
        vt.setFont(F_NORMAL);
        vt.setRowHeight(28);
        vt.setShowGrid(false);
        vt.setIntercellSpacing(new Dimension(0, 1));
        vt.getTableHeader().setFont(F_BOLD);
        vt.getTableHeader().setBackground(new Color(248, 249, 251));
        vt.getColumnModel().getColumn(0).setMaxWidth(35);
        vt.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(219, 234, 254) : (r % 2 == 0 ? C_WHITE : new Color(250, 251, 252)));
                setForeground(C_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                String s = v == null ? "" : v.toString();
                if (s.contains("✅")) setForeground(new Color(22, 163, 74));
                else if (s.contains("❌")) setForeground(C_RED);
                else if (s.contains("⏱")) setForeground(C_YELLOW);
                return this;
            }
        });

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 6));
        tableCard.setBorder(BorderFactory.createCompoundBorder(tableCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        tableCard.add(new JLabel("Bảng kết quả") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        tableCard.add(scrollPane(vt), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bottom.setOpaque(false);
        bottom.add(btn("Xuất báo cáo", C_PRIMARY));
        bottom.add(btn("Sinh thêm TC", C_YELLOW));
        tableCard.add(bottom, BorderLayout.SOUTH);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // ─── STATUS BAR ───────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));

        statusLabel = new JLabel("● Sẵn sàng");
        statusLabel.setFont(F_NORMAL);
        statusLabel.setForeground(new Color(22, 163, 74));
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel right = new JLabel("Claude Sonnet  •  IOI Mode  •  C++17");
        right.setFont(F_NORMAL);
        right.setForeground(C_MUTED);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─── HELPERS ──────────────────────────────────────────
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        return p;
    }

    private JScrollPane scrollPane(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        area.setBackground(C_WHITE);
        return sp;
    }

    private JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        table.setBackground(C_WHITE);
        return sp;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setOpaque(true);
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_NORMAL);
        l.setForeground(C_TEXT);
        return l;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_TITLE);
        l.setForeground(C_TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(F_NORMAL);
        return c;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(C_BORDER);
        s.setPreferredSize(new Dimension(1, 22));
        return s;
    }

    private JPanel statCard(String label, String value, Color valueColor) {
        JPanel p = card();
        p.setLayout(new BorderLayout(0, 2));
        p.setBorder(BorderFactory.createCompoundBorder(
            p.getBorder(),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(valueColor);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(C_MUTED);
        p.add(val, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    // ─── MAIN ─────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new CPTestGen().setVisible(true));
    }
}