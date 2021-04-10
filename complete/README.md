# 10x Banking Technical Exercise
Author: Tsz Shun Chow (Jason)

## How to build
```
gradle build
```

## How to run
```
gradle bootRun
```

## Exposed API
```
PUT /transactions/transaction/{id}
```
where {id} is an UUID of the transaction.

## How to call the API
The simplest way will be the use of commmand `curl`
```
curl --verbose \
  --header "Content-Type: application/json" \
  --request PUT \
  --data '{"sourceAccountId":"ba38d6a7-2a48-418b-9e9b-3a190134e8f4","targetAccountId":"2fc7aebd-5044-49b9-a85d-06d6b4e65dd3","amount":"20.2","currency":"EUR"}' \
  http://localhost:8080/transactions/transaction/1a9855f4-15d1-49af-9850-b00095494e65
```

There are two test accounts created at the startup for testing purpose. The account IDs can be found in the console 
logging. The log message below is an example:
```
Two test accounts were created to facilitate exploratory testing: first=Account(id=AccountId(id=ba38d6a7-2a48-418b-9e9b-3a190134e8f4), balance=EUR 100.00, createdAt=2021-04-10T20:31:03.338035Z), second=Account(id=AccountId(id=2fc7aebd-5044-49b9-a85d-06d6b4e65dd3), balance=EUR 100.00, createdAt=2021-04-10T20:31:03.338234Z)
```

## How to verify result
There is a log message each time when an account balance has changed. The log message below is an example:
```
process=set_account, previous=null, account=Account(id=AccountId(id=ba38d6a7-2a48-418b-9e9b-3a190134e8f4), balance=EUR 100.00, createdAt=2021-04-10T20:31:03.338035Z)
```

## Assumptions
* Cross-concurrency transfer is not supported.
* There is no requirement to store the transaction under the scope of this exercise.
* There is no requirement to achieve idempotency of transaction under the scope of this exercise.
* There is no requirement to expose an API to create or query an account under the scope of this exercise.
* There is no requirement to address the concern of serialisability and transactional behaviour under the scope of this 
  exercise.
* There is no requirement to address any security concern.
* There is no requirement to address the concern of observability.

## Notes
* A *Test Double* was used for the account repository for this exercise.
* Two test Accounts were created at startup to facilitate exploratory testing. The log messages are sufficient in 
  observing the service behaviour while testing.
* *Full TDD approach* was used in developing it.  
* *Hexagonal architecture* (Ports and Adapters) was applied to separate the concerns of core logic and technical 
  integration.
* *CQRS* has been applied in this exercise to segregate command, event and query.
* *Event-driven* approach was applied in this exercise. As long as we replace Spring Event with actual event store, it 
  will become event-sourced.
* Idempotency of transactions can be achieved by ensuring the transactions in the event store is unique by the given ID.
* The original example of Spring Boot (*"HelloController"*) was kept for troubleshooting if the service is up.
