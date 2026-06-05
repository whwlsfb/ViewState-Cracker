package cn.wanghw.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import cn.wanghw.models.VSCAuditResult;
import cn.wanghw.models.ViewStateData;
import cn.wanghw.scanner.ViewStateScanner;
import cn.wanghw.utils.ViewStateUtil;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ViewStateCrackerTab extends JPanel {
    private final MontoyaApi api;
    private final ViewStateScanner scanner;
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;
    private final JEditorPane resultPane;
    private final JButton scanButton;
    private final JButton clearButton;
    private final JButton clearHostsButton;
    private final JButton reloadKeysButton;
    private final JButton languageButton;
    private final JCheckBox passiveScan;
    private final JCheckBox skipDuplicates;
    private final JCheckBox bruteForce;
    private final JCheckBox addIssuesToSiteMap;
    private HttpRequestResponse currentRequestResponse;
    private StringBuilder results = new StringBuilder();
    private boolean chinese;

    public ViewStateCrackerTab(MontoyaApi api, ViewStateScanner scanner) {
        super(new BorderLayout());
        this.api = api;
        this.scanner = scanner;
        this.requestEditor = api.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
        this.responseEditor = api.userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);
        this.resultPane = createResultPane();
        this.scanButton = new JButton("Scan current");
        this.clearButton = new JButton("Clear results");
        this.clearHostsButton = new JButton("Reset host cache");
        this.reloadKeysButton = new JButton("Reload keys");
        this.languageButton = new JButton("中文");
        this.passiveScan = new JCheckBox("Passive scan check", ViewStateScanner.isPassiveScanEnabled());
        this.skipDuplicates = new JCheckBox("Skip duplicate hosts", ViewStateScanner.isSkipDuplicateHosts());
        this.bruteForce = new JCheckBox("Brute force keys", ViewStateScanner.isBruteForceEnabled());
        this.addIssuesToSiteMap = new JCheckBox("Add issues to Site map", true);
        buildUi();
        api.userInterface().applyThemeToComponent(this);
    }

    public void loadRequestResponse(HttpRequestResponse requestResponse, boolean scanNow) {
        currentRequestResponse = requestResponse;
        requestEditor.setRequest(requestResponse.request());
        responseEditor.setResponse(requestResponse.hasResponse() ? requestResponse.response() : HttpResponse.httpResponse(""));
        appendStatus(text("Loaded ", "已加载 ") + escape(summary(requestResponse)) + ".");
        if (scanNow) {
            scanCurrent();
        }
    }

    public String sendToMenuText() {
        return text("Send to ViewState-Cracker", "发送到 ViewState-Cracker");
    }

    private void buildUi() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passiveScan.addActionListener(e -> ViewStateScanner.setPassiveScanEnabled(passiveScan.isSelected()));
        skipDuplicates.addActionListener(e -> ViewStateScanner.setSkipDuplicateHosts(skipDuplicates.isSelected()));
        bruteForce.addActionListener(e -> ViewStateScanner.setBruteForceEnabled(bruteForce.isSelected()));

        scanButton.addActionListener(e -> scanCurrent());
        clearButton.addActionListener(e -> clearResults());
        clearHostsButton.addActionListener(e -> {
            scanner.clearScannedHosts();
            appendStatus(text("Host cache reset.", "Host 缓存已重置。"));
        });
        reloadKeysButton.addActionListener(e -> {
            ViewStateUtil.loadKeys();
            appendStatus(text("MachineKey dictionary reloaded.", "MachineKey 字典已重新加载。"));
        });
        languageButton.addActionListener(e -> {
            chinese = !chinese;
            updateLanguage();
            appendStatus(text("Language switched to English.", "语言已切换为中文。"));
        });

        toolbar.add(scanButton);
        toolbar.add(clearButton);
        toolbar.add(clearHostsButton);
        toolbar.add(reloadKeysButton);
        toolbar.add(languageButton);
        toolbar.add(passiveScan);
        toolbar.add(skipDuplicates);
        toolbar.add(bruteForce);
        toolbar.add(addIssuesToSiteMap);
        add(toolbar, BorderLayout.NORTH);

        JSplitPane messageSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, requestEditor.uiComponent(), responseEditor.uiComponent());
        messageSplit.setResizeWeight(0.5);

        JScrollPane resultScroll = new JScrollPane(resultPane);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageSplit, resultScroll);
        mainSplit.setResizeWeight(0.65);
        add(mainSplit, BorderLayout.CENTER);

        updateLanguage();
        clearResults();
    }

    private JEditorPane createResultPane() {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setEditorKit(new HTMLEditorKit());
        return pane;
    }

    private void scanCurrent() {
        if (currentRequestResponse == null) {
            appendStatus(text("No request loaded. Use the context menu on a request first.", "未加载数据包。请先在数据包右键菜单中发送到插件。"));
            return;
        }

        HttpRequestResponse requestResponse = currentRequestResponse;
        scanButton.setEnabled(false);
        appendStatus(text("Scanning ", "正在扫描 ") + escape(summary(requestResponse)) + "...");

        new SwingWorker<VSCAuditResult, Void>() {
            @Override
            protected VSCAuditResult doInBackground() {
                return scanner.manualAudit(requestResponse);
            }

            @Override
            protected void done() {
                try {
                    renderResult(get(), requestResponse);
                } catch (Exception e) {
                    appendStatus(text("Scan failed: ", "扫描失败：") + escape(e.getMessage()));
                } finally {
                    scanButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void renderResult(VSCAuditResult result, HttpRequestResponse requestResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("<hr>");
        sb.append("<b>").append(escape(timestamp())).append(" - ").append(escape(summary(requestResponse))).append("</b><br>");

        ViewStateData viewStateData = result.getViewStateData();
        if (viewStateData == null) {
            sb.append(text("[-] ViewState data not found.<br>", "[-] 未发现 ViewState 数据。<br>"));
        } else {
            sb.append(text("[+] ViewState data found.<br>", "[+] 发现 ViewState 数据。<br>"));
            appendData(sb, text("Path", "路径"), viewStateData.getPath());
            appendData(sb, text("Generator", "生成器"), viewStateData.getViewStateGenerator());
            appendData(sb, text("Encrypted", "已加密"), yesNo(viewStateData.isViewStateEncrypted()));
            appendData(sb, "EventValidation", yesNo(viewStateData.getEventValidation() != null));
        }

        if (result.auditIssues().isEmpty()) {
            sb.append(text("[-] No issue found.", "[-] 未发现问题。"));
        } else {
            for (AuditIssue issue : result.auditIssues()) {
                sb.append("<br><b>").append(escape(issue.name())).append("</b><br>");
                sb.append(issue.detail());
                if (addIssuesToSiteMap.isSelected()) {
                    api.siteMap().add(issue);
                }
            }
        }

        appendHtml(sb.toString());
    }

    private void appendData(StringBuilder sb, String name, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(escape(name)).append(": ").append(escape(value)).append("<br>");
        }
    }

    private void appendStatus(String message) {
        appendHtml("<hr><b>" + escape(timestamp()) + "</b><br>" + message);
    }

    private void appendHtml(String html) {
        results.append(html);
        resultPane.setText("<html><body style=\"font-family:sans-serif;font-size:12px;\">" + results + "</body></html>");
        resultPane.setCaretPosition(resultPane.getDocument().getLength());
    }

    private void clearResults() {
        results = new StringBuilder(text(
                "Ready. Right-click a request in Repeater, Proxy, Target, or Logger and choose Send to ViewState-Cracker.",
                "就绪。在 Repeater、Proxy、Target 或 Logger 的数据包上右键，选择发送到 ViewState-Cracker。"
        ));
        resultPane.setText("<html><body style=\"font-family:sans-serif;font-size:12px;\">" + results + "</body></html>");
    }

    private void updateLanguage() {
        scanButton.setText(text("Scan current", "扫描当前"));
        clearButton.setText(text("Clear results", "清空结果"));
        clearHostsButton.setText(text("Reset host cache", "重置 Host 缓存"));
        reloadKeysButton.setText(text("Reload keys", "重载密钥"));
        languageButton.setText(text("中文", "English"));
        passiveScan.setText(text("Passive scan check", "启用被动扫描"));
        skipDuplicates.setText(text("Skip duplicate hosts", "跳过重复 Host"));
        bruteForce.setText(text("Brute force keys", "爆破密钥"));
        addIssuesToSiteMap.setText(text("Add issues to Site map", "问题写入 Site map"));
        revalidate();
        repaint();
    }

    private String yesNo(boolean value) {
        return value ? text("Yes", "是") : text("No", "否");
    }

    private String text(String english, String zhCn) {
        return chinese ? zhCn : english;
    }

    private String summary(HttpRequestResponse requestResponse) {
        return requestResponse.request().method() + " " + requestResponse.request().url();
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
