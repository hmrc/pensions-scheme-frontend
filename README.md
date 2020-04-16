# Pensions Scheme Frontend 

## Info

This service allows a pensions administrator, either an individual or an organisation, to register a new - and update an existing - pension scheme.

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

|Service        |HTTP Method |Route                                  |Purpose |
|---------------|--- |----------------|----------------------------------|
|Tai            |GET |/tai/${nino}/tax-account/${year} /expenses/flat-rate-expenses| Returns details of a users tax account specifically that of IABD 57 |
|Tai            |POST|/tai/${nino}/tax-account/${year} /expenses/flat-rate-expenses| Updates a users tax account specifically that of IABD 57  |
|Citizen Details|GET |/citizen-details/${nino}/etag|retrieves the users etag which is added to their update request to NPS to ensure optimistic locking|

|Service | HTTP Method | Route | Purpose
|--------------|-------|------------------|------------------|
|Pensions-scheme | POST | /pensions-scheme/scheme | |
|Pensions-scheme | POST | /pensions-scheme/update-scheme | 
|Pensions-scheme  |GET | /pensions-scheme/is-psa-associated  | | 
|Pension-administrator  |GET  |/pension-administrator/get-email  | | 
|Pension-administrator  |GET |/pension-administrator/get-name | | 
|Pension-administrator  |GET |/pension-administrator/get-minimal-psa  | | 
|address-lookup  |GET |/v2/uk/addresses  | | 
|email           |POST |/hmrc/email  | | 



## Running the service

Service Manager: PSUBS_ALL

Port: 9335

Link: http://localhost:9335/professional-subscriptions

NINOs: `LL111111A` & `AB216913B` (local and Staging environments only)

## Tests and prototype

[View the prototype here](https://employee-expenses.herokuapp.com/)

|Repositories     |Link                                                                   |
|-----------------|-----------------------------------------------------------------------|
|Journey tests    |https://github.com/hmrc/professional-subscriptions-journey-tests       |
|Prototype        |https://github.com/hmrc/employee-expenses-prototype                    |