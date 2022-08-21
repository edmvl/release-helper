package ru.zhendozzz.jira.service.gitlab;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class GitlabService {

    private Integer PROJECT_ID = 38630086; //"eugene156/test";
    private String MAIN_BRANCH = "main";
    private String HOST_URL = "https://gitlab.com";
    private String ACCESS_TOKEN = "glpat-Ft5H_rre1qQys19Uhh9N";
    private GitlabAPI api;

    private final IssueManager issueManager;
    private final IssueLinkManager issueLinkManager;


    @Autowired
    public GitlabService(IssueManager issueManager, IssueLinkManager issueLinkManager) {
        this.issueManager = issueManager;
        this.issueLinkManager = issueLinkManager;
        this.api = GitlabAPI.connect(HOST_URL, ACCESS_TOKEN);
    }

    public String getBranchName(String url) {
        String branch = "";
        try {
            GitlabMergeRequest mergeRequest = api.getMergeRequest(PROJECT_ID, getMRIdByUrl(url));
            branch = mergeRequest.getSourceBranch();
        } catch (Exception e) {
            return null;
        }
        return branch;
    }

    public String mergeAllBranches(String issueId) {
        MutableIssue issue = issueManager.getIssueObject(issueId);
        if (Objects.isNull(issue)) {
            return "";
        }
        String summary = issue.getSummary();
        String releaseBranch = createReleaseBranch(summary);
        List<String> urlList = getMrUrlsFromIssues(issueLinkManager.getOutwardLinks(issue.getId()));
        List<GitlabMergeRequest> mrList = createReleaseMRs(releaseBranch, urlList);
        String result = tryMergeReleaseMR(mrList);
        if (result.contains("error")) {
            closeAllMergeRequests(mrList);
            deleteBranch(summary);
        }
        return result;
    }

    private String tryMergeReleaseMR(List<GitlabMergeRequest> mrList) {
        return mrList.stream().map(mr -> {
            StringBuilder res = new StringBuilder();
            res.append(mr.getSourceBranch());
            try {
                api.acceptMergeRequest(mr.getProjectId(), mr.getIid(), null);
                res.append(" merged");
            } catch (IOException e) {
                res.append(" merging error: ");
                res.append(e.getMessage());
            }
            return res;
        }).collect(Collectors.joining("\n"));
    }

    private List<GitlabMergeRequest> createReleaseMRs(String releaseBranch, List<String> urlList) {
        return urlList.stream().map(url -> {
            try {
                GitlabMergeRequest old = api.getMergeRequest(PROJECT_ID, getMRIdByUrl(url));
                String sourceBranch = old.getSourceBranch();
                return api.createMergeRequest(
                        PROJECT_ID,
                        sourceBranch,
                        releaseBranch,
                        null,
                        "merge " + old.getTitle() + " to " + releaseBranch
                );
            } catch (IOException e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    private List<String> getMrUrlsFromIssues(Collection<IssueLink> issueLinks) {
        return issueLinks.stream().map(issueLink -> {
            Issue destinationObject = issueLink.getDestinationObject();
            Object customFieldValue = destinationObject.getCustomFieldValue(
                    //TODO: вынести в настройки плагина
                    ComponentAccessor.getCustomFieldManager().getCustomFieldObject("customfield_10000")
            );
            return String.valueOf(customFieldValue);
        }).collect(Collectors.toList());
    }

    private void closeAllMergeRequests(List<GitlabMergeRequest> mrList) {
        mrList.forEach(mr -> {
            try {
                GitlabUser assignee = mr.getAssignee();
                api.updateMergeRequest(
                        mr.getProjectId(),
                        mr.getIid(),
                        mr.getTargetBranch(),
                        Objects.nonNull(assignee) ? assignee.getId() : null,
                        mr.getTitle(),
                        mr.getDescription(),
                        "close",
                        String.join(", ", mr.getLabels())
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String createReleaseBranch(String branchName) {
        try {
            api.createBranch(PROJECT_ID, branchName, MAIN_BRANCH);
            return branchName;
        } catch (Exception e) {
            return null;
        }
    }

    private void deleteBranch(String branchName) {
        try {
            api.deleteBranch(PROJECT_ID, branchName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getMRIdByUrl(String url) {
        String p = ".*/merge_requests/(\\d+)";
        Matcher m = Pattern.compile(p).matcher(url);
        if (m.find()) {
            return Integer.valueOf(m.group(1));
        }
        return null;
    }
}
