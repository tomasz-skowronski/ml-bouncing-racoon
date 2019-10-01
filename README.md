# ML Bouncing Racoon

![](logo.png)

## Bounced Emails

http://docs.aws.amazon.com/ses/latest/DeveloperGuide/best-practices-bounces-complaints.html

## API

Callback `GET /?taskid=<taskId>`

## 3rd-party API

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