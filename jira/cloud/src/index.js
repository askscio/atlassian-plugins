import { fetch } from '@forge/api';

export async function eventHandler(event, context) {
	console.log('Event Captured')
	// for backend perms: https://developer.atlassian.com/platform/forge/manifest-reference/permissions/#external-permissions
	var targetURL = 'https://scio-apps.appspot.com/datasources/jira/events'
	console.log(`Target URL ${targetURL}`)
	const response = await fetch(targetURL, {
		method: 'POST', 
		body: JSON.stringify(event),
		headers: {'Content-Type': 'application/json'}
	})
	return response
	
}
