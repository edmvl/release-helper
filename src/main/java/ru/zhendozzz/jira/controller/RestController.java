package ru.zhendozzz.jira.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.zhendozzz.jira.dto.TicketListDto;
import ru.zhendozzz.jira.service.GitlabService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@Component
@Consumes({MediaType.APPLICATION_JSON})
public class RestController {

    private final GitlabService gitlabService;

    public RestController(GitlabService gitlabService) {
        this.gitlabService = gitlabService;
    }

    @POST
    @Path("branch/")
    public ResponseEntity createBranch(@RequestBody String issueKey) {
        //TODO: узнать правила наименования веток
        gitlabService.createReleaseBranch(issueKey);
        return ResponseEntity.ok().build();
    }

    @POST
    @Path("merge/")
    public ResponseEntity getBranches(@RequestBody TicketListDto ticketListDto) {
        gitlabService.mergeAllBranches(ticketListDto);
        return ResponseEntity.ok().build();
    }

}
