# ![SlipDb](docs/slipdb.png)

[![Build Status](https://travis-ci.com/slipsoft/slipdb.svg?branch=master)](https://travis-ci.com/slipsoft/slipdb)
[![Coverage Status](https://coveralls.io/repos/github/slipsoft/slipdb/badge.svg?branch=master)](https://coveralls.io/github/slipsoft/slipdb?branch=master)

A distributed, index based, search engine.


## Getting Started

These instructions will get you a copy of the project up and running on your
local machine for development and testing purposes. See deployment for notes on
how to deploy the project on a live system.

### Prerequisites

-   JDK >= 8.x
-   Maven

### Installing

1.  Install dependencies

    ```
    mvn install
    ```

2.  Run the server

    ```
    mvn jetty:run
    ```
You can now connect to the API at http://localhost:8080/.
A working `get` request could be [`/db/tables`](http://localhost:8080/db/tables).

You can also find the API documentation of your running instance at http://localhost:8080/docs/

## Running the tests

JUnit is used for the tests. You can run it with Maven:

```
mvn test
```

## Deployment

_not deployable yet..._

## Documentation

### API

The API documentation [can be found online](https://slipsoft.github.io/slipdb/). Or if you want to consult you local instance documentation you can access http://localhost:8080/docs/ while the server is running.

## Built With

-   [Maven](https://maven.apache.org/) - Dependency Management
-   [Jetty](https://www.eclipse.org/jetty/) - HTTP Server
-   [JBoss RestEasy](https://resteasy.github.io/) - RestFull Framework
-   [swagger-maven-plugin](https://github.com/kongchen/swagger-maven-plugin) -
    OpenApi doc generation
-   [swagger-ui](https://github.com/swagger-api/swagger-ui) - Web view for the
    API documentation

## Authors

-   **Sylvain JOUBE** - [SylvainLune](https://github.com/SylvainLune)
-   **Etienne LELOUËT** - [etienne-lelouet](https://github.com/etienne-lelouet)
-   **Nicolas PEUGNET** - [n-peugnet](https://github.com/n-peugnet)

See also the list of [contributors](https://github.com/slipsoft/slipdb/contributors)
who participated in this project.
