package ru.zhendozzz.jira.action;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.zhendozzz.jira.service.config.PluginConfigService;

public class ConfigAction extends JiraWebActionSupport {

    @Autowired
    private final PluginConfigService pluginConfigService;

    @Getter
    @Setter
    private String configs;

    public ConfigAction(PluginConfigService pluginConfigService) {
        this.pluginConfigService = pluginConfigService;
    }

    public String doInput() {
        return INPUT;
    }

    public String doSave() {
        return INPUT;
    }

    public String getAllConfigValue() {
        return "";
    }

}
