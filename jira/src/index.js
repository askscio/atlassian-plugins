import { fetch, storage, webTrigger } from '@forge/api';

export async function eventHandler(event, context) {
	console.log('Event Captured')
	// Replace this with scio-apps event handler
	// for backend perms: https://developer.atlassian.com/platform/forge/manifest-reference/permissions/#external-permissions
	var targetURL = 'https://webhook.site/7ef76c62-d490-4f9b-927f-7bb19a34fa84'
	console.log(`Target URL ${targetURL}`)
	const response = await fetch(targetURL, {
		method: 'POST', 
		body: JSON.stringify(event),
		headers: {'Content-Type': 'application/json'}
	})
	return response
	
}
