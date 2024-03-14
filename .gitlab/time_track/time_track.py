#!/usr/bin/env python3

import requests, argparse
from datetime import datetime, timedelta
from graphql_queries import TIME_LOGS_QUERY
from urllib.parse import urlparse
from wiki import create_wiki_page

def parse_arguments():
    parser = argparse.ArgumentParser(description="Time tracking tool for GitLab")
    parser.add_argument('gitlab_url', type=str, help='The gitLab URL')
    parser.add_argument('private_token', type=str, help='The private token')
    parser.add_argument('project_url', type=str, help='The project URL')
    return parser.parse_args()

def is_valid_url(url):
    try:
        result = urlparse(url)
        return all([result.scheme, result.netloc])
    except ValueError:
        return False

def validate_urls(urls):
    if not all(map(is_valid_url, urls)):
        print("One or more URLs are invalid.")
        exit(1)

def get_project_info(gitlab_url, private_token, project_path_encoded):
    response = requests.get(f'{gitlab_url}/api/v4/projects/{project_path_encoded}?private_token={private_token}')
    return response.json()

def remove_lithuanian_letters(s):
    lithuanian_letters   = 'ąčęėįšųūžĄČĘĖĮŠŲŪŽ'
    non_accented_letters = 'aceeisuuzACEEISUUZ'
    trans = str.maketrans(lithuanian_letters, non_accented_letters)
    return s.translate(trans)

def parse_and_validate_args():
    args = parse_arguments()
    validate_urls([args.gitlab_url, args.project_url])
    return args


def format_time(seconds):
    if seconds < 60:
        return f"{seconds} s"
    elif seconds < 3600:
        minutes = seconds // 60
        seconds_remaining = seconds % 60
        if seconds_remaining == 0:
            return f"{minutes} min"
        else:
            return f"{minutes} min {seconds_remaining} s"
    else:
        hours = seconds // 3600
        minutes = (seconds % 3600) // 60
        if minutes == 0:
            return f"{hours} h"
        else:
            return f"{hours} h {minutes} min"

# Function to group time logs by user names and entities (issues or merge requests)
def group_time_logs(nodes, start_date, end_date):
    time_logs_by_user = {}
    for node in nodes:
        author = node['author']['name']
        labels = [label['title'] for label in node['labels']['nodes']]
        for time_log in node['timelogs']['nodes']:
            time_log_date = datetime.strptime(time_log['spentAt'], "%Y-%m-%dT%H:%M:%SZ")
            if time_log_date > start_date and time_log_date < end_date:
                user_name = time_log['user']['name']
                entity_title = node['title']
                entity_id = node['iid']

                if user_name not in time_logs_by_user:
                    time_logs_by_user[user_name] = {"total_time": 0, "logs": []}
                # Check if an entry with the same entity title and ID already exists for the user
                existing_entry_index = next((i for i, entry in enumerate(time_logs_by_user[user_name]["logs"]) if entry["entity_title"] == entity_title and entry["entity"] == entity_id), None)
                # If an entry already exists, update the time spent and spentAt list
                if existing_entry_index is not None:
                    time_logs_by_user[user_name]["logs"][existing_entry_index]["timeSpent"] += time_log['timeSpent']
                else:
                    # If no entry exists, add a new entry
                    time_logs_by_user[user_name]["logs"].append({"timeSpent": time_log['timeSpent'], "entity": entity_id, "entity_title": entity_title, "labels": labels, "author": author})
                # Update the total time spent for the user
                time_logs_by_user[user_name]["total_time"] += time_log['timeSpent']
    return time_logs_by_user

def main():
    args = parse_and_validate_args()

    project_path = args.project_url.replace(args.gitlab_url + '/', '')
    project_path_encoded = requests.utils.quote(project_path, safe='')
    project = get_project_info(args.gitlab_url, args.private_token, project_path_encoded)
    project_id = project['id']

    # Define the variables for the query
    end_date = datetime.now() # Timestamp representing the current date
    start_date = end_date - timedelta(days=7) # Timestamp representing 7 days ago

    # Define the variables for the query
    variables = {
        "fullPath": "sus/Studiosus",
        "since": start_date.isoformat() + "Z",  
        "endDate": end_date.isoformat() + "Z"  
    }

    # Define the GitLab GraphQL endpoint
    endpoint = "https://git.mif.vu.lt/api/graphql"

    # Make the request
    response = requests.post(endpoint, json={"query": TIME_LOGS_QUERY, "variables": variables})

    # Parse the response JSON
    data = response.json()

    # Group time logs for issues by user names
    issues_time_logs_by_user = group_time_logs(
        data['data']['project']['issues']['nodes'],
        start_date,
        end_date
    )

    # Group time logs for merge requests by user names
    merge_requests_time_logs_by_user = group_time_logs(
        data['data']['project']['mergeRequests']['nodes'],
        start_date,
        end_date
    )

    users = set(issues_time_logs_by_user.keys()).union(merge_requests_time_logs_by_user.keys())
    for user_name in users:
        # Check if user_name exists in both dictionaries
        issues_logs = issues_time_logs_by_user.get(user_name, {"total_time": 0, "logs": []})
        merge_requests_logs = merge_requests_time_logs_by_user.get(user_name, {"total_time": 0, "logs": []})

        output = ""
        output += f"{start_date.strftime('%Y-%m-%d')}/{end_date.strftime('%Y-%m-%d')}\n"

        output += f"\n**ISSUES & CODE:**\n"
        for time_log in issues_logs["logs"]:
            # check if labels dont have "Team Meeting"
            if not any("Team meet" in s for s in time_log["labels"]):
                if time_log["author"] == user_name:
                    output += f"- Created and worked on issue {time_log['entity_title']} (#{time_log['entity']}) ({format_time(time_log['timeSpent'])})\n"
                else:
                    output += f"- Worked on issue {time_log['entity_title']} (#{time_log['entity']}) ({format_time(time_log['timeSpent'])})\n"
            
        for time_log in merge_requests_logs["logs"]:
            if time_log["author"] == user_name:
                output += f"- Created & worked on merge request {time_log['entity_title']} (!{time_log['entity']}) ({format_time(time_log['timeSpent'])})\n"
            else:
                output += f"- Reviewed merge request {time_log['entity_title']} (!{time_log['entity']}) ({format_time(time_log['timeSpent'])})\n"

        output += f"\n**TEAM MEETS:**\n"
        for time_log in issues_logs["logs"]:
            meeting_count = 1
            # check if labels dont have "Team Meeting"
            if any("Team meet" in s for s in time_log["labels"]):
                output += f"- Meet {meeting_count}: {time_log['entity_title']} (#{time_log['entity']}) {format_time(time_log['timeSpent'])}\n"
                meeting_count += 1

        # Get total time spent for the user
        total_time = issues_logs["total_time"] + merge_requests_logs["total_time"]
        output += f"\nOverall time spend this week: **{format_time(total_time)}**"

        user_name = remove_lithuanian_letters(user_name)
        username_no_whitespaces = user_name.replace(" ", "")

        # The title and content of the new wiki page
        title = f"Weekly-reports/{username_no_whitespaces}/{end_date.strftime('%Y-%m-%d')}"

        status_code = 0
        status_code = create_wiki_page(
                args.gitlab_url,
                args.private_token,
                project_id,
                title,
                output
            )

        if status_code == 201:
                print(f"Wiki page {title} created successfully.")
        else:
                print(f"Failed to create wiki page {title}. Status code: {status_code}")

if __name__ == '__main__':
    main()