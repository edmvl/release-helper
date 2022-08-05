package ru.zhendozzz.jira.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import ru.zhendozzz.jira.dto.TicketDto;

import java.util.*;
import java.util.stream.Collectors;

@ComponentScan
@Slf4j
public class ModelProvider extends AbstractJiraContextProvider {

    private final IssueLinkManager issueLinkManager;

    public ModelProvider(IssueLinkManager issueLinkManager) {
        this.issueLinkManager = issueLinkManager;
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
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject("customfield_10001")
        );
        String url = String.valueOf(customFieldValue);
        return TicketDto.builder()
                .url(url)
                .number(destinationObject.getKey())
                .status(getStatus(url))
                .build();
    }

    private String getStatus(String url) {
        if (url.equals("null")) {
            return "Не указана ссылка на МР";
        }
        return "Ok";
    }
}
