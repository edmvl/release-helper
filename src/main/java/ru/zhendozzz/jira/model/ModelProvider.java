package ru.zhendozzz.jira.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import lombok.extern.slf4j.Slf4j;
import ru.zhendozzz.jira.dto.TicketDto;
import ru.zhendozzz.jira.service.gitlab.GitlabService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ModelProvider extends AbstractJiraContextProvider {

    private final IssueLinkManager issueLinkManager;
    private final GitlabService gitlabService;

    public ModelProvider(GitlabService gitlabService) {
        this.issueLinkManager = ComponentAccessor.getIssueLinkManager();
        this.gitlabService = gitlabService;
    }

    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        Collection<IssueLink> issueLinks = issueLinkManager.getOutwardLinks(currentIssue.getId());
        List<TicketDto> mergeRequests = getTicketsDtos(issueLinks);
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("mergeRequests", mergeRequests);
        contextMap.put("host", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
        return contextMap;
    }

    private List<TicketDto> getTicketsDtos(Collection<IssueLink> issueLinks) {
        return issueLinks.stream().map(this::apply).collect(Collectors.toList());
    }

    private TicketDto apply(IssueLink issueLink) {
        if (Objects.isNull(issueLink)) {
            return TicketDto.builder().build();
        }
        Issue destinationObject = issueLink.getDestinationObject();
        Object customFieldValue = destinationObject.getCustomFieldValue(
                //TODO: вынести в настройки плагина
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject("customfield_10000")
        );
        String url = String.valueOf(customFieldValue);
        String branchName = gitlabService.getBranchName(url);
        return TicketDto.builder()
                .url(url)
                .number(destinationObject.getKey())
                .branch(branchName)
                .status(getStatus(url, branchName))
                .build();
    }

    private String getStatus(String url, String branchName) {
        if (url.equals("null")) {
            return "Не указана ссылка на МР";
        }
        if (Objects.isNull(branchName)) {
            return "Не удалось получить ветку из МР";
        }
        return "Ok";
    }
}
