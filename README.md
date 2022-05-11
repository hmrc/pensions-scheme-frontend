# Pensions Scheme Frontend 

## Info

This service allows a pensions administrator, either an individual or an organisation, to register a new pension scheme and update an existing pension scheme (variation).

This service has a corresponding back-end service, namely pensions-scheme.

### Dependencies
 
|Service                |Link                                           |
|-----------------------|-----------------------------------------------|
|Pensions-scheme        |https://github.com/hmrc/pensions-scheme        |
|Pension-administrator  |https://github.com/hmrc/pension-administrator  |
|address-lookup         |https://github.com/hmrc/address-lookup         |
|email                  |https://github.com/hmrc/email                  |
|auth                   |https://github.com/hmrc/auth                   |

### Endpoints used   

|Service | HTTP Method | Route | Purpose
|-----------------------|-------|-------------------------------------------|------------------|
|Pensions-scheme        | GET  | /pensions-scheme/scheme                    | Returns details of scheme |
|Pensions-scheme        | POST  | /pensions-scheme/update-scheme            | Update scheme details |
|Pensions-scheme        | GET   | /pensions-scheme/is-psa-associated        | Check for associated schemes for a PSA | 
|Pension-administrator  | GET   | /pension-administrator/get-email          | Returns email address for a PSA | 
|Pension-administrator  | GET   | /pension-administrator/get-name           | Returns name of a PSA | 
|Pension-administrator  | GET   | /pension-administrator/get-minimal-psa    | Returns minimal PSA details from DES | 
|address-lookup         | GET   | /v2/uk/addresses                          | Returns a list of addresses that match a given postcode | 
|email                  | POST  | /hmrc/email                               | Sends an email to an email address | 

## Running the service

Service Manager: PODS_ALL

Port: 8200

Link: http://localhost:8200/register-pension-scheme

Enrolment key: HMRC-PODS-ORG

Identifier name: PsaID

Example PSA ID: A2100005

## Tests and prototype

[View the prototype here](https://pods-prototype.herokuapp.com/page-list/page-list-scheme)

|Repositories     |Link                                                                   |
|-----------------|-----------------------------------------------------------------------|
|Journey tests    |https://github.com/hmrc/pods-journey-tests       |
|Prototype        |https://pods-prototype.herokuapp.com/page-list/page-list-scheme                    |

## Note on terminology
The terms scheme reference number and submission reference number (SRN) are interchangeable within the PODS codebase; some downstream APIs use scheme reference number, some use submission reference number, probably because of oversight on part of the technical teams who developed these APIs. This detail means the same thing, the reference number that was returned from ETMP when the scheme details were submitted.
