package ru.zhendozzz.jira.controller;


import ru.zhendozzz.jira.service.gitlab.GitlabService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/release")
public class RestController {

    private final GitlabService gitlabService;

    public RestController(GitlabService gitlabService) {
        this.gitlabService = gitlabService;
    }

    @GET
    @Path("/merge")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Object getBranches(@QueryParam(value = "issue_id") String issueId) {
        return gitlabService.mergeAllBranches(issueId);
    }

}
