#!/usr/bin/env python3

import requests, re, argparse, warnings
from datetime import datetime, timedelta
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



def main():
    args = parse_and_validate_args()

    project_path = args.project_url.replace(args.gitlab_url + '/', '')
    project_path_encoded = requests.utils.quote(project_path, safe='')
    project = get_project_info(args.gitlab_url, args.private_token, project_path_encoded)
    project_id = project['id']

    # Initialize the dictionary
    time_spent_per_user = {}
    team_meets_per_user = {}

    now = datetime.now()
    seven_days_ago = now - timedelta(days=7)

    # Format dates for the API
    now_str = now.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
    seven_days_ago_str = seven_days_ago.strftime('%Y-%m-%dT%H:%M:%S.%fZ')

    # Get all issues and merge requests for the specific project updated in the last 7 days
    for issue_type in ['issues', 'merge_requests']:
        page = 1
        while True:
            response = requests.get(f'{args.gitlab_url}/api/v4/projects/{project_id}/{issue_type}?private_token={args.private_token}&updated_after={seven_days_ago_str}&updated_before={now_str}&per_page=100&page={page}')
            items = response.json()
            if not items:
                break
            for item in items:
                item_iid = item['iid']
                item_title = item['title']
                item_author_id = item['author']['id']  # Get the author of the item
                item_assignee_id = item['assignee']['id'] if item['assignee'] else None  # Get the assignee of the item
                # Get the issue type
                issue_types = item['labels']
                if 'Team meet' in issue_types:
                    isTeamMeeting = True
                else:
                    isTeamMeeting = False

                # Get the creation date of the item
                created_at = datetime.strptime(item['created_at'], '%Y-%m-%dT%H:%M:%S.%fZ')
                # Get all notes for the issue or merge request
                response = requests.get(f'{args.gitlab_url}/api/v4/projects/{project_id}/{issue_type}/{item_iid}/notes?private_token={args.private_token}')
                notes = response.json()
                for note in notes:
                    # Check if the note is a system note about time tracking
                    if note['system'] and 'time spent' in note['body']:
                        # Extract the time spent from the note body
                        match = re.search(r'added (\d+)([hms]) of time spent at (\d{4}-\d{2}-\d{2})', note['body'])
                        if match:
                            time_spent = match.group(1)
                            unit = match.group(2)
                            date = datetime.strptime(match.group(3), '%Y-%m-%d')
                            # Check if the date is within the last 7 days
                            if date >= seven_days_ago:
                                # Convert the time spent to seconds
                                if unit == 'd':
                                    time_spent_seconds = int(time_spent) * 28800 # 8 hours
                                elif unit == 'h':
                                    time_spent_seconds = int(time_spent) * 3600 # 1 hour
                                elif unit == 'm':
                                    time_spent_seconds = int(time_spent) * 60 # 1 minute
                                elif unit == 's':
                                    time_spent_seconds = int(time_spent)
                                
                                # Get the user ID
                                user_id = note['author'].get('id')

                                # Check if the user is the author of the item
                                is_own_item = user_id == item_author_id

                                # Check if the user is the assignee of the item
                                is_assignee = user_id == item_assignee_id

                                # If it's a team meeting, add it to the team_meets_per_user dictionary
                                if isTeamMeeting:
                                    if user_id in team_meets_per_user:
                                        if item_title in team_meets_per_user[user_id]:
                                            team_meets_per_user[user_id][item_title]['time_spent'] += time_spent_seconds
                                        else:
                                            team_meets_per_user[user_id][item_title] = {'time_spent': time_spent_seconds, 'type': issue_type, 'number': item_iid, 'created_at': created_at}
                                    else:
                                        team_meets_per_user[user_id] = {item_title: {'time_spent': time_spent_seconds, 'type': issue_type, 'number': item_iid, 'created_at': created_at}}

                                # Add the time spent to the time_spent_per_user dictionary
                                elif user_id in time_spent_per_user:
                                    if item_title in time_spent_per_user[user_id]:
                                        time_spent_per_user[user_id][item_title]['time_spent'] += time_spent_seconds
                                    else:
                                        time_spent_per_user[user_id][item_title] = {'first_last_name': note['author'].get('name'), 'time_spent': time_spent_seconds, 'type': issue_type, 'number': item_iid, 'is_own_item': is_own_item, 'is_assignee': is_assignee, 'created_at': created_at}
                                else:
                                    time_spent_per_user[user_id] = {item_title: {'first_last_name': note['author'].get('name'), 'time_spent': time_spent_seconds, 'type': issue_type, 'number': item_iid, 'is_own_item': is_own_item, 'is_assignee': is_assignee, 'created_at': created_at}}

            
            page += 1

    # Print the total time spent per user, the item names, the item numbers, the types, whether it's the user's own item, and whether the user is the assignee
    for user_id, time_spent_dict in time_spent_per_user.items():
        output = ""    
        
        # Print dates and user name
        date = f"{seven_days_ago.strftime('%Y-%m-%d')}/{now.strftime('%Y-%m-%d')}"
        output += date + "\n"

        username = remove_lithuanian_letters(time_spent_dict[list(time_spent_dict.keys())[0]]["first_last_name"])

        output += "\n**ISSUES & CODE:**\n"
        total_time = 0
        for item, item_info in time_spent_dict.items():
            if item_info['type'] == 'issues':
                if item_info['is_own_item']:
                    if item_info['is_assignee']:
                        # check if item was created in the last 7 days
                        if item_info['created_at'] >= seven_days_ago:
                            # print that user created and worked on issue
                            output += f"- Created and worked on issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                        else:
                            # print that user worked on issue
                            output += f"- Worked on issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                    else:
                        # check if item was created in the last 7 days
                        if item_info['created_at'] >= seven_days_ago:
                            # print that user created an issue
                            output += f"- Created issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                        else:
                            # print that user commented on issue
                            output += f"- Commented on issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                else:
                    if item_info['is_assignee']:
                        # print that user is assignee
                        output += f"- Worked on issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                    else:
                        # print that user commented on issue
                        output += f"- Commented on issue {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
            elif item_info['type'] == 'merge_requests':
                if item_info['is_own_item']:
                    if item_info['is_assignee']:
                        # check if item was created in the last 7 days
                        if item_info['created_at'] >= seven_days_ago:
                            # print that user is assignee
                            output += f"- Created merge request {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                        else:
                            # print that user worked on merge request
                            output += f"- Worked on merge request {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                    else:
                        # check if item was created in the last 7 days
                        if item_info['created_at'] >= seven_days_ago:
                            # print that user created an issue
                            output += f"- Created merge request {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                        else:
                            # print that user commented on merge request
                            output += f"- Reviewed merge request {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                else:                    
                    # print that user commented on merge request
                    output += f"- Reviewed merge request {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
            
            total_time += item_info['time_spent']

        # Print the team meetings
        output += "\n**TEAM MEETS:**\n"
        meeting_count = 1
        if user_id in team_meets_per_user:
            for item, item_info in team_meets_per_user[user_id].items():
                output += f"- Meet {meeting_count}: {item} ({('!' if item_info['type'] == 'merge_requests' else '#')}{item_info['number']}) ({item_info['time_spent']/60:0.0f} min)\n"
                total_time += item_info['time_spent']    
        
        # Convert total time from seconds to minutes
        total_time_in_minutes = total_time / 60

        # Calculate the total time spent in hours and minutes
        total_hours = int(total_time_in_minutes // 60)
        total_minutes = int(total_time_in_minutes % 60)

        # Print the total time spent this week
        if total_hours > 0:
            if total_minutes > 0:
                output += f"\nOverall time spend this week: **{total_hours} h {total_minutes} min**"
            else:
                output += f"\nOverall time spend this week: **{total_hours} h**"
        else:
            output += f"\nOverall time spend this week: **{total_minutes} min**"

        username_no_whitespaces = username.replace(" ", "")

        # The title and content of the new wiki page
        title = f"Weekly-reports/{username_no_whitespaces}/{now.strftime('%Y-%m-%d')}"

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