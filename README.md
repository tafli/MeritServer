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

- GET `http://<server>:9000/users`: Returns all users.
- PUT `http://<server>:9000/users`: Accepts an array of users. Current users are deleted and replaced by the new ones.
- POST `http://<server>:9000/users`: Creates a user.
- GET `http://<server>:9000/users/{id}`: Returns a specific user identified by {id}.
- GET `http://<server>:9000/transactions`: Returns all transactions.
- PUT `http://<server>:9000/transactions`: Creates a new transaction
- GET `http://<server>:9000/transactions/{id}`: Returns a specific transaction  identified by {id}.

## Tests

To run the test simple use `sbt test`. And yes the coverage is not (yet) that high.

## License

MIT
