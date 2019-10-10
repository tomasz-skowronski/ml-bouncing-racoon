# ML Bouncing Racoon

![](logo.png)

## Introduction

http://docs.aws.amazon.com/ses/latest/DeveloperGuide/best-practices-bounces-complaints.html

## Local development

You can start the required Docker containers with the following command:

`(cd ~/src/ml-bouncing-racoon/docker && docker-compose up -d)`

Then you can start RacoonApplication from the IDE.
Alternatively you can use the following command to run all containers:

`(cd ~/src/ml-bouncing-racoon/docker && docker-compose -f docker-compose-run-all.yaml up -d)`

## Database

Access to the DEV and Stage database is via the Jump Server:

`ssh ubuntu@jump.internal.magicline.com`

| env   | pgpassword |
|-------|:-------------:|
| DEV   | `PGPASSWORD=igA6gAfQDzzTYZWmydkKptJy psql -h ml-db-bouncingracoon.cwwu8vfqevzy.eu-west-1.rds.amazonaws.com -U racoon_dev` |
| STAGE | `PGPASSWORD=eVsxshroerc9UJVFAsfBCVDU psql -h ml-db-bouncingracoon.cwwu8vfqevzy.eu-west-1.rds.amazonaws.com -U racoon_stage` |

## Communication

```plantuml

actor client
boundary "ml-external-api"
actor "email-validator.net (RTEV)" as RTEV

client -> Raccoon : validate e-mails
Raccoon -> RTEV : POST api/verify
RTEV -> "ml-external-api" : callback
"ml-external-api" ->o Raccoon : taskId
Raccoon -> RTEV : POST /download.html
Raccoon -> Raccoon : taskResult
Raccoon ->o client : statusMessage
...
Raccoon ->o client : statusMessage
```

### Mocks

`/src/test/http/mock.http`

### RabbitMQ

### External communication

RTEV callbacks `GET /racoon/tasks/callbacks?taskid=<taskId>`

### Swagger
* [LOCAL](http://localhost:8107/swagger-ui.html)
* [DEV](https://bouncing-racoon.dev.magicline.com/swagger-ui.html)
* [STAGE](https://bouncing-racoon.stage.magicline.com/swagger-ui.html)
* [PROD](https://bouncing-racoon.magicline.com/swagger-ui.html)

### Real-Time Email Validation API

https://www.email-validator.net/api.html

#### Limits

* Up to 100K email addresses for validation with a single API request.
* All validation task data will be automatically deleted 14 days after the data has been made available.
* The email validator service caches results for several days, so if an email tested as bad, it will continue to show up as bad for a few days until it is re-evaluated. 

> IMPORTANT: Email addresses marked with `OK - Catch-All Active` 
can still bounce as some mail servers accept mail for any address and create a non-delivery-report later. 
If you are using an external service like MailChimp to send out your emails and want to be absolutely sure 
that all email addresses on your list really exist and are deliverable, 
we strongly recommend that you use only addresses with a `OK - Valid Address` (200) status.

#### A multi-layer checking process

* Syntax verification (IETF/RFC standard conformance)
* DNS validation, including MX record lookup
* Disposable email address detection
* Misspelled domain detection to prevent Typosquatting
* SMTP connection and availability checking
* Temporary unavailability detection
* Mailbox existence checking
* Catch-All testing
* Greylisting detection

> During the entire process, we never send any email to the recipient address.

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