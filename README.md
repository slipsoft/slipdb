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

#### Install dependencies

    mvn install

#### Copy & edit the config file

    cp config.dist.json config.json

#### Run the server

    mvn jetty:run

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

## Manual testing of the API

While [the server is running](#run-the-server), here are some queries you can run to test the API:

1.  [`PUT /db/tables`](http://localhost:8080/docs/#/db/createTables) with the following body:
    ```json
    [
        {
            "name": "taxi2newYork",
            "allColumns": [
                {"name": "vendor_id", "type": "Byte"},
                {"name": "tpep_pickup_datetime", "type": "Date"},
                {"name": "tpep_dropoff_datetime", "type": "Date"},
                {"name": "passenger_count", "type": "Byte"},
                {"name": "trip_distance", "type": "Float"},
                {"name": "pickup_longitude", "type": "Double"},
                {"name": "pickup_latitude", "type": "Double"},
                {"name": "rate_code_id", "type": "Byte"},
                {"name": "store_and_fwd_flag", "type": "String", "size": "1"},
                {"name": "dropoff_longitude", "type": "Double"},
                {"name": "dropoff_latitude", "type": "Double"},
                {"name": "payment_type", "type": "Byte"},
                {"name": "fare_amount", "type": "Float"},
                {"name": "extra", "type": "Float"},
                {"name": "mta_tax", "type": "Float"},
                {"name": "tip_amount", "type": "Float"},
                {"name": "tolls_amount", "type": "Float"},
                {"name": "improvment_surcharge", "type": "Float"},
                {"name": "total_amount", "type": "Float"}
            ]
        }
    ]
    ```

2.  [`GET /db/tables`](http://localhost:8080/docs/#/db/getTables) should return the table we just created.

3.  [`POST /table/{tableName}/load`](http://localhost:8080/docs/#/table/loadCSV) with `taxi2newYork` as tableName and the content of [`testdata/SMALL_100_000_yellow_tripdata_2015-04.csv`](testdata/SMALL_100_000_yellow_tripdata_2015-04.csv) as the body.

4.  [`PUT /table/{tableName}/index`](http://localhost:8080/docs/#/table/addIndex) with `taxi2newYork` as tableName and the following body:
    ```json
    {
        "name": "index_vendor_id",
        "columnsToIndex": [
            "vendor_id"
        ],
        "type": "dichotomy"
    }
    ```

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
