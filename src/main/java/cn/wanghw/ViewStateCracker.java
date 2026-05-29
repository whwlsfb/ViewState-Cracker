package cn.wanghw;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import cn.wanghw.scanner.ViewStateScanner;
import cn.wanghw.utils.CommonUtil;
import cn.wanghw.utils.ViewStateUtil;
import cn.wanghw.ui.ViewStateCrackerTab;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewStateCracker implements BurpExtension, ContextMenuItemsProvider {
    public static Logging logging;
    private ViewStateScanner scanner;
    private ViewStateCrackerTab viewStateCrackerTab;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        CommonUtil.montoyaApi = montoyaApi;
        logging = CommonUtil.montoyaApi.logging();
        ViewStateUtil.loadKeys();
        CommonUtil.montoyaApi.extension().setName("ViewState-Cracker");
        scanner = new ViewStateScanner();
        viewStateCrackerTab = new ViewStateCrackerTab(CommonUtil.montoyaApi, scanner);
        CommonUtil.montoyaApi.scanner().registerScanCheck(scanner);
        CommonUtil.montoyaApi.userInterface().registerSuiteTab("ViewState-Cracker", viewStateCrackerTab);
        CommonUtil.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
        logging.logToOutput("ViewState-Cracker loaded successfully!\r\n");
        logging.logToOutput("Anthor: Whwlsfb");
        logging.logToOutput("Email: whwlsfb@wanghw.cn");
        logging.logToOutput("Github: https://github.com/whwlsfb/ViewState-Cracker");
    }

    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            HttpRequestResponse requestResponse = selectedRequestResponse(event);
            if (requestResponse == null) {
                return null;
            }
            List<Component> menuItemList = new ArrayList<>();
            JMenuItem sendTo = new JMenuItem(viewStateCrackerTab.sendToMenuText());
            sendTo.addActionListener(e -> viewStateCrackerTab.loadRequestResponse(requestResponse, true));
            menuItemList.add(sendTo);
            return menuItemList;
        }
        return null;
    }

    private HttpRequestResponse selectedRequestResponse(ContextMenuEvent event) {
        if (event.messageEditorRequestResponse().isPresent()) {
            return event.messageEditorRequestResponse().get().requestResponse();
        }
        if (!event.selectedRequestResponses().isEmpty()) {
            return event.selectedRequestResponses().get(0);
        }
        return null;
    }
}
