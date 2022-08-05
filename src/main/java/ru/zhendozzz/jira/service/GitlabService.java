package ru.zhendozzz.jira.service;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Service;
import ru.zhendozzz.jira.dto.TicketDto;
import ru.zhendozzz.jira.dto.TicketListDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitlabService {

    private GitLabApi gitLabApi;


    public GitlabService() {
        //TODO: вынести в настройки
        gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");
    }

    public String createReleaseBranch(String name) {
        try {
            gitLabApi.getRepositoryApi().createBranch(null, name, "master");
        } catch (GitLabApiException e) {
            return e.toString();
        }
        return "Ok";
    }

    public String mergeAllBranches(TicketListDto ticketListDto) {
        List<TicketDto> ticketDtoList = ticketListDto.getTicketDtoList();
        ticketDtoList.stream().forEach(ticketDto -> {
            String url = ticketDto.getUrl();
            Long mrIdByUrl = getMRIdByUrl(url);
            try {
                MergeRequest old = gitLabApi.getMergeRequestApi().getMergeRequest(null, mrIdByUrl);
                String sourceBranch = old.getSourceBranch();
                String targetBranch = "master";
                MergeRequest newRequest = gitLabApi.getMergeRequestApi().createMergeRequest(
                        null,
                        sourceBranch,
                        targetBranch,
                        "merge" + old.getDescription(),
                        null, null
                );
                gitLabApi.getMergeRequestApi().acceptMergeRequest(null, newRequest.getIid());
            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
        });
        return "Ok";
    }

    Long getMRIdByUrl(String url){
        String p = "/projects/\\d/merge_requests/(\\d)";
        Matcher m = Pattern.compile(p).matcher(url);
        if (m.find()){
            return Long.valueOf(m.group(1));
        }
        return null;
    }
}
