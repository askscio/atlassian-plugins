import { fetch } from '@forge/api';

export async function eventHandler(event, context) {
	console.log('Event Captured')
	// for backend perms: https://developer.atlassian.com/platform/forge/manifest-reference/permissions/#external-permissions
	var targetURL = 'https://apps-be.glean.com/datasources/jira/forge'
	const status = await fetch(targetURL, {
		method: 'POST', 
		body: JSON.stringify(event),
		headers: {'Content-Type': 'application/json'}
	}).then(response => response.status)
	return status
}
