import requests
import json

def create_wiki_page(gitlab_url, private_token, project_id, title, content):
   
    # The API endpoint to create a new wiki page
    url = f'{gitlab_url}/api/v4/projects/{project_id}/wikis'

    # The data to send in the request
    data = {
        'title': title,
        'content': content,
    }

    # The headers for the request
    headers = {
        'Private-Token': private_token,
        'Content-Type': 'application/json',
    }

    # Make the request
    response = requests.post(url, headers=headers, data=json.dumps(data))

    return response.status_code