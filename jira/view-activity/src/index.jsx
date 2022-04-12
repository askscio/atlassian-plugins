export async function viewHandler(event, context) {
	console.log('Issue viewed');
	console.log(`Issue id: ${event.issue.id}`)
	//console.log("event: " + JSON.stringify(event));
	//console.log("context: " + JSON.stringify(context));	
}
export async function createHandler(event, context) {
	console.log('Issue created');
	console.log(`Issue id: ${event.issue.id}`)
	//console.log("event: " + JSON.stringify(event));
	//console.log("context: " + JSON.stringify(context));	
}
export async function updateHandler(event, context) {
	console.log('Issue updated');
	console.log(`Issue id: ${event.issue.id}`)
	//console.log("event: " + JSON.stringify(event));
	//console.log("context: " + JSON.stringify(context));	
}



