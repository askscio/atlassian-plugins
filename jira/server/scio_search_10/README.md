# Jira Glean Search Plugin - Jira 10.x.x

The Glean Search plugin for Jira Server or Data Center editions provides additional ranking
signals for Glean. It sends additional webhooks back to Glean whenever a user views a page. This
search plugin is updated to support Jira 10.x.x. The
content of these messages are:

* URL visited
* Username
* WebhookEvent (type of event, only VIEW for now)

## Installation

Releases are hosted
on [GitHub](https://github.com/askscio/atlassian-plugins/releases/tag/glean-jira-v1.0). To install:

1. Go to `Manage apps` in Jira's administration settings.
1. Click `Upload app`
1. Enter the URL to the `JAR` file from the release page.

## Implementation

### ScioSearchServletFilter

A [Servlet Filter Plugin Module](https://developer.atlassian.com/server/framework/atlassian-sdk/servlet-filter-plugin-module/).
Runs after login, so we have user information available. Webhooks will be of the form:

```
{
    "url": "http://jira-instance:8080/browse/AC-1",
    "user": "admin"
    "webhookEvent": "VIEW"
}
```

Webhooks are sent on a single background thread. A limited size queue holds outstanding views.

### ScioSearchConfigRestPlugin

A [REST Plugin Module](https://developer.atlassian.com/server/framework/atlassian-sdk/rest-plugin-module/).
Listens on `/rest/scio_search/1.0/configure`. Requires `jira-administrator` credentials.
Accepts `POST` of the form:

```
{
    "target": "https://<customer>-be.glean.com/instance/JIRA/scio_event"
}
```

The `target` URL will then be sent webhooks for page views. `GET` will return the current config.

## Debugging

To enable debug logs:

1. Go to `Logging and Profiling` in Jira's administration settings.
1. Add a new entry for `ScioSearchConfigRestPlugin.impl`
1. Set the level to:
    * `WARN` to see error messages.
    * `ALL` to see all activity, which include usernames and pages viewed.

Logging is typically written to `/var/atlassian/application-data/jira/log/atlassian-jira.log`.
