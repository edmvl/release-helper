package ru.zhendozzz.jira.condition;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class VisibilityCondition extends AbstractIssueWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue, JiraHelper jiraHelper) {
        //TODO: вынести в настройки тип задачи в который подставляется блок
        return issue.getIssueType().getName().equals("Релиз");
    }
}
