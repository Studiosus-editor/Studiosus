TIME_LOGS_QUERY = """
query ($fullPath: ID!, $since: Time!, $endDate: Time!) {
    project(fullPath: $fullPath) {
        issues(updatedAfter: $since, createdBefore: $endDate, sort: UPDATED_DESC) {
            nodes {
                iid
                title
                labels {
                    nodes {
                        title
                    }
                }
                author {
                    name
                }
                assignees {
                    nodes {
                        name
                    }
                }
                timelogs {
                    nodes {
                        timeSpent
                        spentAt
                        user {
                            name
                        }
                    }
                }
            }
        }
        mergeRequests(updatedAfter: $since, createdBefore: $endDate, sort: UPDATED_DESC) {
            nodes {
                iid
                title
                labels {
                    nodes {
                        title
                    }
                }
                author {
                    name
                }
                assignees {
                    nodes {
                        name
                    }
                }
                timelogs {
                    nodes {
                        timeSpent
                        spentAt
                        user {
                            name
                        }
                    }
                }
            }
        }
    }
}
"""
