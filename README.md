# slipdb

Distributed index based search engine.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

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
You can now connect to the API at http://localhost/api/test

## Running the tests

JUnit is used for the tests. You can run it with Maven:

```
mvn test
```

## Deployment

_not deployable yet..._

## Built With

-   [Maven](https://maven.apache.org/) - Dependency Management
-   [Jetty](https://www.eclipse.org/jetty/) - HTTP Server
-   [JBoss RestEasy](https://resteasy.github.io/) - RestFull Framework

## Authors

-   **Sylvain JOUBE** - [SylvainLune](https://github.com/SylvainLune)
-   **Etienne LELOUËt** - [etienne-lelouet](https://github.com/etienne-lelouet)
-   **Nicolas PEUGNET** - [n-peugnet](https://github.com/n-peugnet)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.
