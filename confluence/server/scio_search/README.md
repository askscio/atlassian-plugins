# Confluence Scio Search Plugin

The Scio Search plugin for Confluence Server or Data Center editions provides additional ranking
signals for Scio. It sends additional webhooks back to Scio whenever a user views a page. The
content of these messages are:
* URL visited
* Username

## Installation

Releases are hosted on [GitHub](https://github.com/askscio/atlassian-plugins/releases). To install:
1. Go to `Manage apps` in Confluence's administration settings.
1. Click `Upload app`
1. Enter the URL to the `JAR` file from the release page.

## Implementation

### ScioSearchServletFilter

A [Servlet Filter Plugin Module](https://developer.atlassian.com/server/framework/atlassian-sdk/servlet-filter-plugin-module/).
Runs after login, so we have user information available. Webhooks will be of the form:
```
{
    "url": "http://confluence-instance:8090/display/en/child+page+1",
    "user": "admin"
}
```

Webhooks are sent on a single background thread. A limited size queue holds outstanding views.

### ScioSearchConfigRestPlugin

A [REST Plugin Module](https://developer.atlassian.com/server/framework/atlassian-sdk/rest-plugin-module/).
Listens on `/rest/scio_search/1.0/configure`. Requires `confluence-administrator` credentials.
Accepts `POST` of the form:
```
{
    "target": "https://<customer>-be.glean.com/instance/CONFLUENCE/scio_event"
}
```
The `target` URL will then be sent webhooks for page views. `GET` will return the current config.

## Debugging

To enable debug logs:
1. Go to `Logging and Profiling` in Confluence's administration settings.
1. Add a new entry for `com.askscio.atlassian_plugins.confluence.impl`
1. Set the level to:
   * `WARN` to see error messages.
   * `ALL` to see all activity, which include usernames and pages viewed.

Logging is typically written to `/var/atlassian/application-data/confluence/logs/atlassian-confluence.log`.
See [Atlassian's help](https://confluence.atlassian.com/doc/working-with-confluence-logs-108364721.html)
for more details.

#OPEN 1
#OPEN 2