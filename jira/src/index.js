import { fetch } from '@forge/api';

export async function eventHandler(event, context) {
	console.log('Event Captured')
	// Replace this with scio-apps event handler
	// for backend perms: https://developer.atlassian.com/platform/forge/manifest-reference/permissions/#external-permissions
	var targetURL = 'https://webhook.site/c780948b-91d8-40ee-800b-3a7096831bdf'
	console.log(`Target URL ${targetURL}`)
	const response = await fetch(targetURL, {
		method: 'POST', 
		body: JSON.stringify(event),
		headers: {'Content-Type': 'application/json'}
	})
	return response
	
}
