Merit Server
============
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/tafli/MeritServer.svg?branch=master)](https://travis-ci.org/tafli/MeritServer)
[![Coverage Status](https://coveralls.io/repos/github/tafli/MeritServer/badge.svg?branch=master)](https://coveralls.io/github/tafli/MeritServer?branch=master)

A simple Web-Service to track your Merits!

## Synopsis

**MeritServer** is a service to help track your Merits.

## Motivation

We recently started with [Merit Money](https://management30.com/practice/merit-money/) in our Team and were looking for an easy piece of software to help us track our transactions. As there were no suitable projects around
I decided to start my own to do this job. Beside that I could learn [Akka-HTTP](http://doc.akka.io/docs/akka-http/current/scala.html).

## Installation

Download sources an use `sbt run` to compile an run the project. By default the webserver starts listening on port 9000.

## Configuration

- `merit.startAmount`: The amount of merits each user can give each round.
- `merit.payoutThreshold`: The threshold on which random number from 0.0 to it a payout happens.
- `merit.userFile`: File path where the user data is save.
- `merit.transactionFile`: File path where the transaction data is saved.

## API Reference

### Teams

A Team consist of an `id`, a `name` and a `startAmount` of Merits for each round. The `id` and `startAmount` are optional values
and may be not provided when creating a new ressource. In that case, these values are generated as for the `id` or
is taken from the configuration with a base value for `startAmount`.

#### GET `http://<server>:9000/v1/teams`

Returns all teams.

```bash
curl --request GET \
  --url http://localhost:9000/v1/teams
```

```json
[
    {
        "id": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
        "name": "FC Duckburgh"
    }
]
```

#### POST `http://<server>:9000/v1/teams`

Creates a team.

```bash
curl --request POST \
  --url http://localhost:9000/v1/teams \
  --header 'content-type: application/json' \
  --data '{
    "teamId": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
	"name": "FC Duckburgh"
}'
```

```json
[
    {
      "id": "6331929a-7587-43e6-a4d8-3a266f48edcf",
      "name": "FC Duckingburgh",
      "startAmount": 11
    }
]
```

#### PUT `http://<server>/v1/teams/{id}`

Creates a team with a specific ID.
```bash
curl --request PUT \
  --url http://localhost:9000/v1/teams/fcb \
  --header 'content-type: application/json' \
  --data '{
	"name":"FC Duckburgh - The best team ever!",
	"startAmount" : 25
}'
```

```json
{
	"id": "fcd",
	"name": "FC Duckburgh - The best team ever!",
	"startAmount": 25
}
```

### Users

A user does have a `id`, a `teamId`, a `familyName` and `firstName` and a `balance`. The `id` is optinal and is
generated when using a `POST` create a new user. The `teamId` referst to a existing team and must exist in the system.
The `balance` itself is a special value. In case of a payday without payout, the amount of merits earning the last round
is booked onto balance and might be  payd out next payday.

#### GET `http://<server>:9000/v1/users`

Returns all users.

```bash
curl --request GET \
  --url http://localhost:9000/v1/users
```

```json
[
	{
		"id": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"teamId": "177c49cc-aaf7-4873-8ddf-4931fe978c4b",
		"familyName": "Duck",
		"firstName": "Donald",
		"balance": 0
	},
	{
		"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
		"familyName": "Duck",
		"firstName": "Daisy",
		"balance": 0
	}
]
```

#### PUT `http://<server>:9000/v1/users`

Accepts an array of users. Current users are deleted and replaced by the new ones.

```bash
curl --request PUT \
     --url http://localhost:9000/v1/users \
     --header 'content-type: application/json' \
     --data '[{
     "teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
     "familyName": "Duck",
     "firstName": "Donald"
   }, {
     "teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
     "familyName": "Duck",
     "firstName": "Daisy"
   }]'
```

```json
[
	{
		"id": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
		"familyName": "Duck",
		"firstName": "Donald",
		"balance": 0
	},
	{
		"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
		"familyName": "Duck",
		"firstName": "Daisy",
		"balance": 0
	}
]
```

#### POST `http://<server>:9000/v1/users`

Creates a new user. This user is added to the current list of users.

```bash
curl --request POST \
  --url http://localhost:9000/v1/users \
  --header 'content-type: application/json' \
  --data '{
    "teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
	"firstName": "Gustav",
	"familyName": "Duck"
}'
```

```json
{
	"id": "3b25c2e4-00cc-4524-aa71-f400c306c0b0",
	"teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
	"familyName": "Duck",
	"firstName": "Gustav",
	"balance": 0
}
```

#### GET `http://<server>:9000/v1/users/{id}`

Returns a specific user identified by {id}.

```bash
curl --request GET \
  --url http://localhost:9000/v1/users/51492b93-af20-4130-a37b-7b4c97fb9894
```

```json
{
	"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
	"teamId": "e46806ce-2b54-4416-a65f-8a16186b82a4",
	"familyName": "Duck",
	"firstName": "Daisy",
	"balance": 0
}
```

### Transactions

#### GET `http://<server>:9000/v1/transactions`

Returns all transactions.

```bash
curl --request GET \
  --url http://localhost:9000/v1/transactions
```

```json
[
	{
		"booked": false,
		"reason": "You are a beauty!",
		"amount": 1,
		"to": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"id": "3eb595f4-40a1-42bd-bceb-9a39164e0161",
		"date": "2017-01-16",
		"from": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee"
	}
]
```

#### POST `http://<server>:9000/v1/transactions`

Creates a new transaction.

```bash
curl --request POST \
  --url http://localhost:9000/v1/transactions \
  --header 'content-type: application/json' \
  --data '{
	"from": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
	"to": "51492b93-af20-4130-a37b-7b4c97fb9894",
	"amount": 1,
	"reason": "You are a beauty!"
}'
```

```json
{
	"booked": false,
	"reason": "You are a beauty!",
	"amount": 1,
	"to": "51492b93-af20-4130-a37b-7b4c97fb9894",
	"id": "3eb595f4-40a1-42bd-bceb-9a39164e0161",
	"date": "2017-01-16",
	"from": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee"
}
```

#### GET `http://<server>:9000/v1/transactions/{id}`

Returns a specific transaction identified by {id}.

```bash
curl --request GET \
  --url http://localhost:9000/v1/transactions/3eb595f4-40a1-42bd-bceb-9a39164e0161
```

```json
{
	"booked": false,
	"reason": "You are a beauty!",
	"amount": 1,
	"to": "51492b93-af20-4130-a37b-7b4c97fb9894",
	"id": "3eb595f4-40a1-42bd-bceb-9a39164e0161",
	"date": "2017-01-16",
	"from": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee"
}
```

#### DELETE `http://<server>:9000/v1/transactions/{id}`

Deletes a transaction specified by {id}.

```bash
curl --request DELETE \
  --url http://localhost:9000/v1/transactions/3eb595f4-40a1-42bd-bceb-9a39164e0161
```

### Merits

#### GET `http://<server>:9000/v1/merits`

Returns for all users a list of all received merits since last payout.

```bash
curl --request GET \
  --url http://localhost:9000/v1/merits
```

```json
[
	{
		"userId": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"name": "Donald Duck",
		"amount": 0
	},
	{
		"userId": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"name": "Daisy Duck",
		"amount": 42
	}
]
```

#### POST `http://<server>:9000/v1/merits/payday`

Based on a random number a payout is decided. The payout-list for all user is returned.

```bash
curl --request POST \
  --url http://localhost:9000/v1/merits/payday
```
Payday:
```json
[
	{
		"userId": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"name": "Donald Duck",
		"amount": 0
	},
	{
		"userId": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"name": "Daisy Duck",
		"amount": 42
	}
]
```

Nothing yet:
```json
[]
```

In any case all transactions are booked.<br/>
If no payout happened a users balance is increased according its merits to be paid out later.<br/>
In case of a payout, a users balance is set to 0.

## Static Files
Every resource in folder `static` can be access via static route `http://<server-address>/static/<your-resource>`.
This is helpful as you can add a UI to edit or [visualize](https://github.com/MartinGantenbein/MeritJS) the data.

## Tests

To run the test simple use `sbt test`. And yes the coverage is not (yet) that high.

## License

MIT
