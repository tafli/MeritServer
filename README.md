Merit Server
============
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/tafli/MeritServer.svg?branch=master)](https://travis-ci.org/tafli/MeritServer)

A simple Web-Service to track your Merits!

## Synopsis

**MeritServer** is a service to help track your Merits.

## Motivation

We recently started with [Merit Money](https://management30.com/practice/merit-money/) in our Team and were looking for an easy piece of software to help us track our transactions. As there were no suitable projects around
I decided to start my own to do this job. Beside that I could learn [Akka-HTTP](http://doc.akka.io/docs/akka-http/current/scala.html).

## Installation

Download sources an use `sbt run` to compile an run the project. By default the webserver starts listening on port 9000.

## API Reference

### GET `http://<server>:9000/v1/users`

Returns all users.

```bash
curl --request GET \
  --url http://localhost:9000/v1/users
```

```json
[
	{
		"id": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"familyName": "Duck",
		"firstName": "Donald",
		"balance": 0
	},
	{
		"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"familyName": "Duck",
		"firstName": "Daisy",
		"balance": 0
	}
]
```

### PUT `http://<server>:9000/v1/users`

Accepts an array of users. Current users are deleted and replaced by the new ones.

```bash
curl --request PUT \
     --url http://localhost:9000/v1/users \
     --header 'content-type: application/json' \
     --data '[{
     "familyName": "Duck",
     "firstName": "Donald"
   }, {
     "familyName": "Duck",
     "firstName": "Daisy"
   }]'
```

```json
[
	{
		"id": "8a7dedd2-a946-4dff-b8c7-fb392a7627ee",
		"familyName": "Duck",
		"firstName": "Donald",
		"balance": 0
	},
	{
		"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
		"familyName": "Duck",
		"firstName": "Daisy",
		"balance": 0
	}
]
```

### POST `http://<server>:9000/v1/users`

Creates a new user. This user is added to the current users.

```bash
curl --request POST \
  --url http://localhost:9000/v1/users \
  --header 'content-type: application/json' \
  --data '{
	"firstName": "Gustav",
	"familyName": "Duck"
}'
```

```json
{
	"id": "3b25c2e4-00cc-4524-aa71-f400c306c0b0",
	"familyName": "Duck",
	"firstName": "Gustav",
	"balance": 0
}
```

### GET `http://<server>:9000/v1/users/{id}`

Returns a specific user identified by {id}.

```bash
curl --request GET \
  --url http://localhost:9000/v1/users/51492b93-af20-4130-a37b-7b4c97fb9894
```

```json
{
	"id": "51492b93-af20-4130-a37b-7b4c97fb9894",
	"familyName": "Duck",
	"firstName": "Daisy",
	"balance": 0
}
```

### GET `http://<server>:9000/v1/transactions`

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

### POST `http://<server>:9000/v1/transactions`

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

### GET `http://<server>:9000/v1/transactions/{id}`

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

### DELETE `http://<server>:9000/v1/transactions/{id}`

Deletes a transaction specified by {id}.

```bash
curl --request DELETE \
  --url http://localhost:9000/v1/transactions/3eb595f4-40a1-42bd-bceb-9a39164e0161
```

## Tests

To run the test simple use `sbt test`. And yes the coverage is not (yet) that high.

## License

MIT
