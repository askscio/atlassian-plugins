import api, { fetch, route, webTrigger } from '@forge/api';

async function doPost(payload, queryParams) {
  // for backend perms: https://developer.atlassian.com/platform/forge/manifest-reference/permissions/#external-permissions
  var targetURL = 'https://apps-be.glean.com/datasources/jira/forge'
  if (!!queryParams) {
    targetURL += '?' + queryParams
  }
  return fetch(targetURL, {
    method: 'POST',
    body: JSON.stringify(payload),
    headers: {'Content-Type': 'application/json'}
  })
}

export async function eventHandler(event, context) {
  console.log('Event Captured')
  const response = await doPost(event, 'event_type=product');
  return response.status;
}

export async function healthcheckHandler(request, context) {
  console.log('Received healthcheck request')
  return {
    "statusCode": 200  // required
  }
}

export async function installedHandler(event, context) {
  console.log('App installed. Installation ID:', event.id);

  const response = await api.asUser().requestJira(route`/rest/api/2/serverInfo`, {
    headers: {
      'Accept': 'application/json'
    }
  });
  const data = await response.json();
  const baseUrl = data.baseUrl

  const healthcheckUrl = await webTrigger.getUrl("healthcheck-trigger");

  const postResponse = await doPost({
    'baseUrl': data.baseUrl,
    'installationId': event.id,
    'healthcheckUrl': healthcheckUrl,
  }, 'event_type=installed')

  return postResponse.status
}
