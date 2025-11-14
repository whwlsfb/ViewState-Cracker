package cn.wanghw.scanner;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.ConsolidationAction;
import burp.api.montoya.scanner.ScanCheck;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import cn.wanghw.models.*;
import cn.wanghw.utils.CommonUtil;
import cn.wanghw.utils.ViewStateExtractor;
import cn.wanghw.utils.ViewStateParser;
import cn.wanghw.utils.ViewStateUtil;

import java.util.concurrent.ConcurrentSkipListSet;

public class ViewStateScanner implements ScanCheck {
    private final ViewStateExtractor viewStateExtractor;
    private final ConcurrentSkipListSet<Object> scannedHosts = new ConcurrentSkipListSet<>();

    public ViewStateScanner() {
        viewStateExtractor = new ViewStateExtractor();
    }

    @Override
    public AuditResult activeAudit(HttpRequestResponse httpRequestResponse, AuditInsertionPoint auditInsertionPoint) {
        return startScan(httpRequestResponse, true);
    }

    @Override
    public AuditResult passiveAudit(HttpRequestResponse httpRequestResponse) {
        return startScan(httpRequestResponse, false);
    }

    private boolean isScanned(Object host) {
        synchronized (scannedHosts) {
            if (scannedHosts.contains(host))
                return true;
            else scannedHosts.add(host);
            return false;
        }
    }

    private AuditResult startScan(HttpRequestResponse httpRequestResponse, boolean active) {
        VSCAuditResult result = new VSCAuditResult();
        ViewStateData viewStateData = parseViewState(httpRequestResponse);
        if (viewStateData != null) {
            result.setViewStateData(viewStateData);
            viewStateData.setPath(httpRequestResponse.request().pathWithoutQuery());
            String hostAndPort = String.format("%s:%s", httpRequestResponse.httpService().host(), httpRequestResponse.httpService().port());
            if (!isScanned(hostAndPort)) {
                if (ViewStateParser.isWithoutMACViewState(viewStateData.getViewState())) {
                    result.addAuditIssue(new ViewStateWithoutMacIssue(httpRequestResponse.httpService(), httpRequestResponse));
                } else {
                    BruteResult bruteResult = ViewStateUtil.bruteViewStateKey(viewStateData);
                    if (bruteResult.isSuccess()) {
                        result.addAuditIssue(new ViewStateKeyFoundIssue(httpRequestResponse.httpService(), httpRequestResponse, bruteResult));
                    }
                }
            }
        }
        return result;
    }

    private ViewStateData parseViewState(HttpRequestResponse httpRequestResponse) {
        ViewStateData viewStateData = viewStateExtractor.parseRequest(httpRequestResponse.request());
        if (viewStateData == null)
            viewStateData = viewStateExtractor.parseResponse(httpRequestResponse.response());
        return viewStateData;
    }

    @Override
    public ConsolidationAction consolidateIssues(AuditIssue newIssue, AuditIssue existingIssue) {
        return existingIssue.name().equals(newIssue.name()) && existingIssue.baseUrl().equalsIgnoreCase(newIssue.baseUrl()) ? ConsolidationAction.KEEP_NEW : ConsolidationAction.KEEP_BOTH;
    }
}
