function releaseHelperCallAPI(url, callback) {
    AJS.$.ajax({
        type: "GET",
        url: url,
        cache: false,
        contentType: false,
        processData: false,
        timeout: 7200000,
        success: callback,
        error: function (e) {
            console.log("Data fetch failed: " + e.responseText);
        },
    });
}


function mergeBranches() {
    var url = AJS.contextPath() + "/rest/release/1.0/release/merge?issue_id=" + JIRA.Issue.getIssueKey();
    releaseHelperCallAPI(url, function (data) {
        console.log(data);
    })
}