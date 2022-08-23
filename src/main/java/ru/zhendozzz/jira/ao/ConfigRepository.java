package ru.zhendozzz.jira.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

@Transactional
@Repository
public class ConfigRepository {

    private final ActiveObjects ao;

    public ConfigRepository(ActiveObjects ao) {
        this.ao = ao;
    }

    public List<Config> getCustomPlanningConfiguration() {
        return newArrayList(ao.find(Config.class));
    }


    public List<Config> saveCustomPlanningConfiguration(String configValues) {
        Arrays.stream(ao.find(Config.class))
                .forEach(ao::delete);
        return Stream.of(configValues.split("\r")).map(customPlanning -> {
            Config config = ao.create(Config.class);
            config.setValue(customPlanning);
            config.setApplicable(String.valueOf(true));
            config.save();
            return config;
        }).collect(Collectors.toList());
    }


}
