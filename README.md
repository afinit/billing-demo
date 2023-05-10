# billing

## Basic Architecture

- http layer: takes incoming requests forwards them to the service layer
- service layer: process inputs including validation
- repository layer: handles datastore access which is 2 simple Maps at this point

## Data Types

There are currently 3 datatypes with hardcoded rates. The strings shown here are used in JSON representation:
- `storagebytes`: rate of $0.002
- `cpu`: rate of $0.1
- `bandwidthbytes`: rate of $0.001


## Endpoints

### localhost:8080/usage/create

Creates new usage in the system

POST body input:

```
  {
    "date": "2023-05-10",
    "units": "storagebytes",
    "amount": 42.1
  }
```

### localhost:8080/usage/get

Gets usage from the system based on 5 filters. All filters are optional and any filter not provided will be ignored. Multiple filters are combined with `AND`:
- id: Filters on the usage id (will only return 1 or 0 records)
- startDate: inclusive filter on date. all usage on or after this date will be returned
- endDate: inclusive filter on date. all usage on or before this date will be returned
- usageUnits: only usage of the given type will be returned
- isInvoiced: boolean filter to return all invoiced usage or all uninvoiced usage

POST body input:
```
  {
    "id": null,
    "startDate": null,
    "endDate": "2023-05-30",
    "usageUnits": "cpu",
    "isInvoiced": null
  }
```

### localhost:8080/invoice/generate

Generates an invoice based on the usage meeting the filter criteria. All filters are optional and any filter not provided will be ignored. Multiple filters are combined with `AND`:
- startDate: inclusive filter on date. all usage on or after this date will be returned
- endDate: inclusive filter on date. all usage on or before this date will be returned
- usageUnits: only usage of the given type will be returned

POST body input:
```
  {
    "startDate": null,
    "endDate": "2023-05-30",
    "usageUnits": "cpu"
  }
```

### localhost:8080/invoice/{invoiceId}

This is a GET endpoint that allows the user to request another copy of the invoice data for any invoice already generated. This request is built on the invoiceId passsed.
If there are no records for the given invoiceId, this currently returns an empty invoice rather than raising an error like it should.

## How to run

From the root of the project, you can spin up a server with:
```
sbt run
```

Then you can hit any of the endpoints as described above. I've also included a number of curl commands in a shell script called `curlCommands.sh`


## Things that need to be improved or would be changed for a production system

### Account
I didn't include account as part of this yet. Proabably a pretty big missing piece, but I ended up spending my time on other things here like invoice generation.

### Datastore
The datastore is currently in memory, but I've constructed this in a way that should make it easy to create a new Repository class that accesses a database.

### Validation
I would like to use the `Validated` type so that I can do validation across more than one input. There's nothing more frustrating
as a user than having to resubmit multiple times to correct all of the errors in your input.

### Needs more tests
I've tested this a number of ways, but I would like to spend a bit more time with that to be confident I'm not missing anything.
Thinking about those tests can also help understand ways to improve the functionality.

### More dynamic
Things like rates should be dynamic or based on some sort of datastore rather than hardcoded.
