package app;

import app.Interface.aCallBack;
import app.Interface.bCallBack;
import app.Interface.cCallBack;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;
public class GUI extends JFrame {
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
    static final Font F_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD, 15);
    static final Font F_MONO   = new Font("Consolas", Font.PLAIN, 13);

    // --- Field
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
    private JProgressBar progressBar;
    private JTabbedPane tabs;

    private JButton analyzeBtn;
    private JButton uploadBtn;
    private JButton generateCodeBtn;
    private JButton genTestcaseBtn;
    private JButton runTestcaseBtn;
    private JButton runAllBtn;
    private JButton addTestcaseBtn;
    private JButton updateTestcaseBtn;
    private JButton exportTestcaseBtn;
    private JButton saveApiKeyBtn;
    private JComboBox<String> testcaseTypeCombo;
    private JLabel statusLabel;
    private JSpinner numSpinner;
    private JPasswordField apiKeyField;

    private Controller controller;
    private CodeRunner codeRunner;
    private Services.Result lastAnalysisResult;
    private List<Services.Testcase> testcases = new ArrayList<>();
    private final Map<String, StrengthRunSummary> strengthRuns = new HashMap<>();
    public GUI(){
        setTitle("74OJ - Bài tập lớn JAVA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(8, 8));

        add(TopBar(),    BorderLayout.NORTH);
        add(Center(),    BorderLayout.CENTER);
        add(StatusBar(), BorderLayout.SOUTH);

        controller = new Controller();
        codeRunner = new CodeRunner();
        Listener();
    }

    private void Listener(){
        analyzeBtn.addActionListener(e -> {
            String text = problemArea.getText().trim();
            if (text.isEmpty() || text.startsWith("Dán đề bài")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đề bài!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            analyzeBtn.setEnabled(false);
            statusLabel.setText("Đang phân tích...");
            statusLabel.setForeground(C_YELLOW);
            progressBar.setIndeterminate(true);
            analysisArea.setText("Đang gửi yêu cầu tới Gemini AI...\nVui lòng đợi giây lát.");
            tabs.setSelectedIndex(0);

            controller.anlyzeAsync(text, new aCallBack() {
                @Override
                public void onSuccess(Services.Result rs) {
                    lastAnalysisResult = rs;
                    analysisArea.setText(rs.toString());
                    statusLabel.setText("● Phân tích hoàn tất");
                    statusLabel.setForeground(C_GREEN);
                    analyzeBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);

                    if(!rs.sampleInput.isEmpty()) inputArea.setText(rs.sampleInput);
                    if(!rs.sampleOutput.isEmpty()) outputArea.setText(rs.sampleOutput);
                }

                @Override
                public void onError(String err) {
                    lastAnalysisResult = null;
                    analysisArea.setText("LỖI PHÂN TÍCH:\n" + err);
                    statusLabel.setText("● Lỗi hệ thống");
                    statusLabel.setForeground(C_RED);
                    analyzeBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                }
            });
        });

        generateCodeBtn.addActionListener(e -> {
            String text = problemArea.getText().trim();
            if (text.isEmpty() || text.startsWith("Dán đề bài")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đề bài!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String type = (String) codeTypeCombo.getSelectedItem();
            String lang = (String) langCombo.getSelectedItem();

            generateCodeBtn.setEnabled(false);
            statusLabel.setText("Đang sinh code...");
            statusLabel.setForeground(C_YELLOW);
            progressBar.setIndeterminate(true);
            codeArea.setText("// Đang sinh mã nguồn " + lang + " (" + type + ")...\nVui lòng đợi.");
            tabs.setSelectedIndex(2);

            controller.generateCodeAsync(text, type, lang, lastAnalysisResult, new bCallBack() {
                @Override
                public void onSuccess(String code) {
                    codeArea.setText(code);
                    statusLabel.setText("Đã sinh code xong");
                    statusLabel.setForeground(C_GREEN);
                    generateCodeBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                }

                @Override
                public void onError(String error) {
                    codeArea.setText("// LỖI KHI SINH CODE:\n" + error);
                    statusLabel.setText("Lỗi sinh code");
                    statusLabel.setForeground(C_RED);
                    generateCodeBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                }
            });
        });

        genTestcaseBtn.addActionListener(e -> {
            String text = problemArea.getText().trim();
            if (text.isEmpty() || text.startsWith("Dán đề bài")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đề bài!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int count = (Integer) numSpinner.getValue();
            String type = (String) testcaseTypeCombo.getSelectedItem();

            genTestcaseBtn.setEnabled(false);
            statusLabel.setText("Đang sinh testcase...");
            statusLabel.setForeground(C_YELLOW);
            progressBar.setIndeterminate(true);
            tabs.setSelectedIndex(1);

            controller.generateTestcaseAsync(text, count, type, lastAnalysisResult, new cCallBack() {
                @Override
                public void onSuccess(List<Services.Testcase> cases) {
                    testcases = cases;
                    clearStrengthEvidence();
                    tableModel.setRowCount(0);
                    for (int i = 0; i < cases.size(); i++) {
                        Services.Testcase tc = cases.get(i);
                        String rowType = tc.type.isBlank() ? type : tc.type;
                        if (tc.strength != null && !tc.strength.isBlank()) {
                            rowType = rowType + " / " + tc.strength;
                        }
                        tableModel.addRow(new Object[]{i + 1, rowType, "OK", "-"});
                    }
                    if (!cases.isEmpty()) {
                        testcaseTable.setRowSelectionInterval(0, 0);
                        inputArea.setText(cases.get(0).input);
                        outputArea.setText(cases.get(0).output);
                    }
                    statusLabel.setText("Đã sinh testcase");
                    statusLabel.setForeground(C_GREEN);
                    genTestcaseBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                }

                @Override
                public void onError(String error) {
                    statusLabel.setText("Lỗi sinh testcase");
                    statusLabel.setForeground(C_RED);
                    genTestcaseBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    JOptionPane.showMessageDialog(GUI.this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file đề bài (ảnh hoặc văn bản)");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
            chooser.setFileFilter(new FileNameExtensionFilter("Supported files (*.png, *.jpg, *.jpeg, *.txt)", "png", "jpg", "jpeg", "txt"));
            if (chooser.showOpenDialog(GUI.this) == JFileChooser.APPROVE_OPTION) {
                File file =  chooser.getSelectedFile();
                controller.readFileWithOCR(file, new bCallBack() {
                    @Override
                    public void onSuccess(String code) {
                        problemArea.setText(code);
                        statusLabel.setText("Đọc file thành công: " + file.getName());
                    }

                    @Override
                    public void onError(String error) {
                        problemArea.setText(error);
                        statusLabel.setText("Lỗi đọc file");
                    }
                });
            }
        });


        runTestcaseBtn.addActionListener(e -> runCurrentTestcases());
        if (runAllBtn != null) {
            runAllBtn.addActionListener(e -> runCurrentTestcases());
        }

        addTestcaseBtn.addActionListener(e -> addManualTestcase());
        updateTestcaseBtn.addActionListener(e -> updateSelectedTestcase());
        exportTestcaseBtn.addActionListener(e -> exportTestcasesToFile());

        saveApiKeyBtn.addActionListener(e -> {
            String key = new String(apiKeyField.getPassword()).trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập API key!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                controller.setApiKey(key);
                statusLabel.setText("● Đã lưu API key");
                statusLabel.setForeground(C_GREEN);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    private JPanel TopBar(){
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(0, 50));
        bar.setBackground(C_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        bar.setLayout(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("74OJ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(C_PRIMARY);
        left.add(title);

        left.add(label("Ngôn ngữ:"));
        langCombo = combo(new String[]{"C++17", "Java", "Python 3"});
        left.add(langCombo);

        left.add(label("API Key:"));
        apiKeyField = new JPasswordField();
        apiKeyField.setFont(F_NORMAL);
        apiKeyField.setPreferredSize(new Dimension(160, 28));
        left.add(apiKeyField);
        saveApiKeyBtn = btn("Lưu", C_GREEN);
        left.add(saveApiKeyBtn);
        bar.add(BorderLayout.WEST, left);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        analyzeBtn = btn("Phân tích AI", C_PRIMARY);
        right.add(analyzeBtn);
        runAllBtn = btn("Chạy tất cả", new Color(139, 92, 246));
        right.add(runAllBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane Center() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightTabs());
        split.setDividerLocation(340);
        split.setDividerSize(5);
        split.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        split.setBackground(C_BG);
        return split;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                panel.getBorder(),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        panel.add(sectionTitle("Nhập đề bài"), BorderLayout.NORTH);

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

        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        uploadBtn = btn("Tải file / ảnh", C_MUTED);
        JButton clearBtn  = btn("Xóa", C_RED);
        clearBtn.addActionListener(e -> {
            problemArea.setText("");
            problemArea.setForeground(C_TEXT);
            problemArea.requestFocus();
        });
        btns.add(uploadBtn);
        btns.add(clearBtn);
        panel.add(btns, BorderLayout.SOUTH);

        return panel;
    }

    private JTabbedPane buildRightTabs() {
        tabs = new JTabbedPane();
        tabs.setFont(F_BOLD);
        tabs.setBackground(C_BG);
        tabs.addTab("Phân tích",  buildAnalysisTab());
        tabs.addTab("Testcase",   buildTestcaseTab());
        tabs.addTab("Code",       buildCodeTab());
        return tabs;
    }

    private JPanel buildAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

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

        analysisArea = new JTextArea();
        analysisArea.setFont(F_NORMAL);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        panel.add(scrollPane(analysisArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTestcaseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);
        toolbar.add(label("Số TC:"));
        numSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        numSpinner.setFont(F_NORMAL);
        numSpinner.setPreferredSize(new Dimension(70, 30));
        toolbar.add(numSpinner);
        testcaseTypeCombo = combo(new String[]{"Ngẫu nhiên", "Edge Cases", "Stress"});
        toolbar.add(testcaseTypeCombo);
        genTestcaseBtn = btn("Sinh", C_PRIMARY);
        toolbar.add(genTestcaseBtn);
        addTestcaseBtn = btn("Thêm TC", new Color(59, 130, 246));
        toolbar.add(addTestcaseBtn);
        updateTestcaseBtn = btn("Lưu sửa", new Color(15, 118, 110));
        toolbar.add(updateTestcaseBtn);
        exportTestcaseBtn = btn("Xuất file", C_GREEN);
        toolbar.add(exportTestcaseBtn);
        toolbar.add(btn("Xóa", C_RED));
        panel.add(toolbar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(340);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(C_BG);

        String[] cols = {"#", "Loại", "Trạng thái", "Thời gian"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        testcaseTable = new JTable(tableModel);
        testcaseTable.setFont(F_NORMAL);
        testcaseTable.setRowHeight(28);
        testcaseTable.setShowGrid(false);
        testcaseTable.setIntercellSpacing(new Dimension(0, 1));
        testcaseTable.getTableHeader().setFont(F_BOLD);
        testcaseTable.getTableHeader().setBackground(new Color(248, 249, 251));
        testcaseTable.getColumnModel().getColumn(0).setMaxWidth(35);
        testcaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testcaseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(219, 234, 254) : (r % 2 == 0 ? C_WHITE : new Color(250, 251, 252)));
                setForeground(C_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 4));
        tableCard.setBorder(BorderFactory.createCompoundBorder(tableCard.getBorder(),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        tableCard.add(new JLabel("Danh sách testcase") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        tableCard.add(scrollPane(testcaseTable), BorderLayout.CENTER);
        split.setLeftComponent(tableCard);


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

        testcaseTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = testcaseTable.getSelectedRow();
            if (row >= 0 && row < testcases.size()) {
                Services.Testcase tc = testcases.get(row);
                inputArea.setText(tc.input);
                outputArea.setText(tc.output);
            }
        });

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void addManualTestcase() {
        String input = inputArea.getText().trim();
        String output = outputArea.getText().trim();
        if (input.isEmpty() || output.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input/Output không được trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Services.Testcase tc = new Services.Testcase();
        tc.type = (String) testcaseTypeCombo.getSelectedItem();
        tc.strength = "MEDIUM";
        tc.input = input;
        tc.output = output;
        testcases.add(tc);
        clearStrengthEvidence();
        int index = testcases.size();
        tableModel.addRow(new Object[]{index, tc.type + " / " + tc.strength, "Thủ công", "-"});
        testcaseTable.setRowSelectionInterval(index - 1, index - 1);
        statusLabel.setText("Đã thêm testcase thủ công");
        statusLabel.setForeground(C_GREEN);
    }

    private void updateSelectedTestcase() {
        int row = testcaseTable.getSelectedRow();
        if (row < 0 || row >= testcases.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn testcase để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String input = inputArea.getText().trim();
        String output = outputArea.getText().trim();
        if (input.isEmpty() || output.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input/Output không được trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Services.Testcase tc = testcases.get(row);
        tc.input = input;
        tc.output = output;
        clearStrengthEvidence();
        tableModel.setValueAt("Đã sửa", row, 2);
        statusLabel.setText("Đã cập nhật testcase");
        statusLabel.setForeground(C_GREEN);
    }

    private void runCurrentTestcases() {
        if (testcases == null || testcases.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có testcase để chạy.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String lang = (String) langCombo.getSelectedItem();
        if (lang == null || lang.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngôn ngữ trước khi chạy.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = codeArea.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập code " + lang + " trước khi chạy.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String currentCodeType = normalizeCodeTypeKey((String) codeTypeCombo.getSelectedItem());

        runTestcaseBtn.setEnabled(false);
        if (runAllBtn != null) runAllBtn.setEnabled(false);
        statusLabel.setText("Đang chạy testcase...");
        statusLabel.setForeground(C_YELLOW);
        progressBar.setIndeterminate(true);
        tabs.setSelectedIndex(2);
        compileArea.setText("Đang chạy testcases cho " + lang + "...\n");

        new SwingWorker<List<RunResult>, Void>() {
            @Override
            protected List<RunResult> doInBackground() {
                return codeRunner.runTestcases(lang, code, testcases);
            }

            @Override
            protected void done() {
                try {
                    List<RunResult> results = get();
                    StringBuilder log = new StringBuilder();
                    int pass = 0;
                    for (int i = 0; i < results.size(); i++) {
                        RunResult rs = results.get(i);
                        Services.Testcase tc = testcases.get(i);
                        String expected = normalizeOutput(tc.output);
                        String actual = normalizeOutput(rs.stdout);
                        String status;
                        if (rs.exitCode != 0 || (rs.stderr != null && !rs.stderr.isBlank())) {
                            status = "RE";
                        } else if (expected.equals(actual)) {
                            status = "AC";
                            pass++;
                        } else {
                            status = "WA";
                        }
                        tableModel.setValueAt(status, i, 2);
                        tableModel.setValueAt(rs.durationMs + " ms", i, 3);

                        log.append("TC ").append(i + 1).append(": ").append(status)
                                .append(" (").append(rs.durationMs).append(" ms)").append("\n");
                        if (status.equals("WA")) {
                            log.append("Expected:\n").append(tc.output).append("\n")
                               .append("Actual:\n").append(rs.stdout).append("\n");
                        }
                        if (status.equals("RE")) {
                            if (rs.stderr != null && !rs.stderr.isBlank()) {
                                log.append("Error:\n").append(rs.stderr).append("\n");
                            }
                        }
                        log.append("---\n");
                    }
                    updateStrengthEvidence(currentCodeType, pass, results.size());
                    log.append("\n=== ĐÁNH GIÁ ĐỘ MẠNH TESTCASE ===\n")
                            .append(buildStrengthReport())
                            .append("\n");
                    compileArea.setText(log.toString());
                    statusLabel.setText("Đã chạy xong " + pass + "/" + results.size() + " testcase");
                    statusLabel.setForeground(pass == results.size() ? C_GREEN : C_YELLOW);
                } catch (Exception ex) {
                    compileArea.setText("Lỗi khi chạy testcase:\n" + ex.getMessage());
                    statusLabel.setText("Lỗi khi chạy testcase");
                    statusLabel.setForeground(C_RED);
                } finally {
                    runTestcaseBtn.setEnabled(true);
                    if (runAllBtn != null) runAllBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                }
            }
        }.execute();
    }

    private void exportTestcasesToFile() {
        if (testcases == null || testcases.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có testcase để xuất.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn thư mục để xuất testcase");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int option = chooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedDirectory = chooser.getSelectedFile();
        if (selectedDirectory == null || !selectedDirectory.exists()) {
            JOptionPane.showMessageDialog(this, "Thư mục không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Path exportRoot = selectedDirectory.toPath().resolve("testcases_export");
        int suffix = 1;
        while (Files.exists(exportRoot)) {
            suffix++;
            exportRoot = selectedDirectory.toPath().resolve("testcases_export_" + suffix);
        }

        try {
            Files.createDirectories(exportRoot);
            for (int i = 0; i < testcases.size(); i++) {
                Services.Testcase tc = testcases.get(i);
                Path testcaseFolder = exportRoot.resolve(String.format("testcase_%03d", i + 1));
                Files.createDirectories(testcaseFolder);
                Files.writeString(testcaseFolder.resolve("input.txt"), tc.input == null ? "" : tc.input);
                Files.writeString(testcaseFolder.resolve("output.txt"), tc.output == null ? "" : tc.output);
            }
            statusLabel.setText("Đã xuất " + testcases.size() + " testcase vào: " + exportRoot.getFileName());
            statusLabel.setForeground(C_GREEN);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi file:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Lỗi xuất testcase");
            statusLabel.setForeground(C_RED);
        }
    }

    private String normalizeOutput(String text) {
        if (text == null) return "";
        return text.replace("\r\n", "\n").trim();
    }

    private void clearStrengthEvidence() {
        strengthRuns.clear();
    }

    private void updateStrengthEvidence(String codeType, int pass, int total) {
        if (codeType == null || codeType.isBlank() || total <= 0) return;
        strengthRuns.put(codeType, new StrengthRunSummary(pass, total));
    }

    private String buildStrengthReport() {
        StrengthRunSummary ac = strengthRuns.get("AC");
        StrengthRunSummary wa = strengthRuns.get("WA");
        StrengthRunSummary tle = strengthRuns.get("TLE");

        StringBuilder sb = new StringBuilder();
        sb.append("Lần chạy đã có: ").append(formatRunSummary("AC", ac))
          .append(", ").append(formatRunSummary("WA", wa))
          .append(", ").append(formatRunSummary("TLE", tle)).append(".\n");

        if (ac == null) {
            sb.append("Kết luận: Chưa đủ dữ liệu. Hãy chạy bộ code AC trước.");
            return sb.toString();
        }
        if (ac.passCount < ac.totalCount) {
            sb.append("Kết luận: Chưa thể chấm độ mạnh vì code AC chưa qua hết testcase.");
            return sb.toString();
        }

        boolean hasNegative = wa != null || tle != null;
        int killedCount = 0;
        if (wa != null && wa.passCount < wa.totalCount) killedCount++;
        if (tle != null && tle.passCount < tle.totalCount) killedCount++;

        if (!hasNegative) {
            sb.append("Kết luận: Chưa đủ dữ liệu. Hãy chạy thêm WA hoặc TLE để đo độ mạnh.");
        } else if (killedCount == 0) {
            sb.append("Kết luận: Testcase CHƯA đủ mạnh (WA/TLE vẫn qua toàn bộ).");
        } else if (killedCount == 1) {
            sb.append("Kết luận: Testcase tạm đủ mạnh (đã loại được 1 nhóm code sai/chậm).");
        } else {
            sb.append("Kết luận: Testcase đủ mạnh (AC qua hết, cả WA và TLE đều bị loại).");
        }
        return sb.toString();
    }

    private String formatRunSummary(String codeType, StrengthRunSummary summary) {
        if (summary == null) return codeType + " chưa chạy";
        return codeType + " " + summary.passCount + "/" + summary.totalCount;
    }

    private String normalizeCodeTypeKey(String rawType) {
        if (rawType == null) return "";
        if (rawType.startsWith("AC")) return "AC";
        if (rawType.startsWith("WA")) return "WA";
        if (rawType.startsWith("TLE")) return "TLE";
        return rawType.trim();
    }

    private static class StrengthRunSummary {
        private final int passCount;
        private final int totalCount;

        private StrengthRunSummary(int passCount, int totalCount) {
            this.passCount = passCount;
            this.totalCount = totalCount;
        }
    }

    private JPanel buildCodeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setBackground(C_BG);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);
        toolbar.add(label("Loại:"));
        codeTypeCombo = combo(new String[]{"AC (Đúng)", "WA (Sai)", "TLE (Chậm)", "Checker"});
        toolbar.add(codeTypeCombo);
        generateCodeBtn = btn("Sinh Code AI", C_PRIMARY);
        toolbar.add(generateCodeBtn);
        toolbar.add(btn("Biên dịch", new Color(234, 88, 12)));
        runTestcaseBtn = btn("Chạy TC", C_GREEN);
        toolbar.add(runTestcaseBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(380);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(C_BG);

        JPanel codeCard = card();
        codeCard.setLayout(new BorderLayout(0, 4));
        codeCard.setBorder(BorderFactory.createCompoundBorder(codeCard.getBorder(),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        codeCard.add(new JLabel("Code mẫu") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        codeArea = new JTextArea();
        codeArea.setFont(F_MONO);
        codeArea.setForeground(new Color(30, 80, 160));

        codeCard.add(scrollPane(codeArea), BorderLayout.CENTER);
        split.setTopComponent(codeCard);

        JPanel compileCard = card();
        compileCard.setLayout(new BorderLayout(0, 4));
        compileCard.setBorder(BorderFactory.createCompoundBorder(compileCard.getBorder(),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        compileCard.add(new JLabel("Compile / Run Output") {{ setFont(F_BOLD); setForeground(C_TEXT); }}, BorderLayout.NORTH);
        compileArea = new JTextArea();
        compileArea.setFont(F_MONO);
        compileArea.setEditable(false);
        compileArea.setForeground(new Color(22, 163, 74));
        compileCard.add(scrollPane(compileArea), BorderLayout.CENTER);
        split.setBottomComponent(compileCard);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }



    private JPanel StatusBar() {
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

        JLabel right = new JLabel("Bài tập lớn HP JAVA");
        right.setFont(F_NORMAL);
        right.setForeground(C_MUTED);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }


    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_NORMAL);
        l.setForeground(C_TEXT);
        return l;
    }

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
}

