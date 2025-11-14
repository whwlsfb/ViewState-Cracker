package cn.wanghw.models;

import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.audit.issues.AuditIssue;

import java.util.ArrayList;
import java.util.List;

public class VSCAuditResult implements AuditResult {
    private List<AuditIssue> auditIssues = new ArrayList<>();
    private ViewStateData viewStateData;

    @Override
    public List<AuditIssue> auditIssues() {
        return auditIssues;
    }

    public void addAuditIssue(AuditIssue auditIssue) {
        auditIssues.add(auditIssue);
    }

    public ViewStateData getViewStateData() {
        return viewStateData;
    }

    public void setViewStateData(ViewStateData viewStateData) {
        this.viewStateData = viewStateData;
    }
}
