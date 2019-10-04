# ML Bouncing Racoon

![](logo.png)

## Introduction

http://docs.aws.amazon.com/ses/latest/DeveloperGuide/best-practices-bounces-complaints.html

## Local development

You start the main containers with the following command:

`(cd ~/src/ml-bouncing-racoon/docker && docker-compose -f docker-compose-run.yaml up -d)`

Then you can start RacoonApplication from the IDE.
Alternatively you can use the following command all Docker containers:

`(cd ~/src/ml-bouncing-racoon/docker && docker-compose -f docker-compose-run-all.yaml up -d)`

## Database

Access to the DEV and Stage database is via the Jump Server:

`ssh ubuntu@jump.internal.magicline.com`

**DEV**

`PGPASSWORD=igA6gAfQDzzTYZWmydkKptJy psql -h ml-db-bouncingracoon.cwwu8vfqevzy.eu-west-1.rds.amazonaws.com -U racoon_dev`

**Stage**

`PGPASSWORD=eVsxshroerc9UJVFAsfBCVDU psql -h ml-db-bouncingracoon.cwwu8vfqevzy.eu-west-1.rds.amazonaws.com -U racoon_stage`

## Communication

RTEV callbacks `GET /?taskid=<taskId>`

### Swagger
* [LOCAL](http://localhost:8107/swagger-ui.html)
* [DEV](https://bouncing-racoon.dev.magicline.com/swagger-ui.html)
* [STAGE](https://bouncing-racoon.stage.magicline.com/swagger-ui.html)
* [PROD](https://bouncing-racoon.magicline.com/swagger-ui.html)

## External communication

### Real-Time Email Validation API

https://www.email-validator.net/api.html

> During the entire process, we never send any email to the recipient address.

A multi-layer checking process:
* Syntax verification (IETF/RFC standard conformance)
* DNS validation, including MX record lookup
* Disposable email address detection
* Misspelled domain detection to prevent Typosquatting
* SMTP connection and availability checking
* Temporary unavailability detection
* Mailbox existence checking
* Catch-All testing
* Greylisting detection

#### Asynchronous Bulk API

You send a request  and receive a callback by email (NotifyEmail) 
or HTTP (NotifyURL) when processing is complete.

1. You send a POST request to
```
http[s]://bulk.email-validator.net/api/verify as specified here:
https://www.email-validator.net/api.html#bulk-api
```
On success you receive a HTTP reply with status 121 (Task Accepted)
and the taskid of your validation task.

2. When your task is finished, we send you an email and a HTTP GET
request to the NotifyURL (with a 'taskid' parameter in the URL).

3. The validation results can be downloaded as CSV file using this URL:
`https://www.email-validator.net/download?id=<taskid>&cmd=download`

You can specify the output with these additional parameters:
```
- validaddresses-nocatchall (valid addresses, without catchall addresses)
- catchalladdresses (catchall addresses)
- invalidaddresses (invalid addresses)
- suspectaddresses (suspect addresses)
- indeterminateaddresses (indeterminate addresses)
- output=long generates a CSV with the email addresses, validation result codes and detailed descriptions.
```

To get the list of all valid addresses (including the catchall
addresses) for a validation task, you can use this URL:
`https://www.email-validator.net/download?id=<taskid>&cmd=download&validaddresses-nocatchall&catchalladdresses`

To get the list of invalid addresses for a validation task,
you can use this URL:
`https://www.email-validator.net/download?id=<taskid>&cmd=download&invalidaddresses`