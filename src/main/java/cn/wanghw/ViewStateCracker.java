package cn.wanghw;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import cn.wanghw.models.VSCAuditResult;
import cn.wanghw.scanner.ViewStateScanner;
import cn.wanghw.utils.CommonUtil;
import cn.wanghw.utils.MessageDialog;
import cn.wanghw.utils.ViewStateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewStateCracker implements BurpExtension, ContextMenuItemsProvider {
    public static Logging logging;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        CommonUtil.montoyaApi = montoyaApi;
        logging = CommonUtil.montoyaApi.logging();
        ViewStateUtil.loadKeys();
        CommonUtil.montoyaApi.extension().setName("ViewState-Cracker");
        CommonUtil.montoyaApi.scanner().registerScanCheck(new ViewStateScanner());
        CommonUtil.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
        logging.logToOutput("ViewState-Cracker loaded successfully!\r\n");
        logging.logToOutput("Anthor: Whwlsfb");
        logging.logToOutput("Email: whwlsfb@wanghw.cn");
        logging.logToOutput("Github: https://github.com/whwlsfb/ViewState-Cracker");
    }

    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            HttpRequestResponse requestResponse = event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0);
            List<Component> menuItemList = new ArrayList<>();
            JMenuItem sendTo = new JMenuItem("Send To ViewState-Cracker");
            sendTo.addActionListener(e -> {
                ViewStateScanner scanner = new ViewStateScanner();
                VSCAuditResult result = (VSCAuditResult) scanner.passiveAudit(requestResponse);
                StringBuilder sb = new StringBuilder();
                if (result != null && result.getViewStateData() != null) {
                    sb.append("[+] ViewState data found.<br><br>");
                } else {
                    sb.append("[-] ViewState data not found.<br><br>");
                }
                if (result != null && !result.auditIssues().isEmpty()) {
                    AuditIssue issue = result.auditIssues().get(0);
                    sb.append(issue.name());
                    sb.append("<br><br>");
                    sb.append(issue.detail());
                } else {
                    sb.append("[-] Crack failed.");
                }
                new MessageDialog(CommonUtil.montoyaApi.userInterface().swingUtils().suiteFrame(), sb.toString(), "Scan Result");
            });
            menuItemList.add(sendTo);
            return menuItemList;
        }
        return null;
    }
}
