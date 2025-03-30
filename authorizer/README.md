# STCP Realtime Data Ingestor Authorizer

A [Quarkus](https://quarkus.io) based application, build to run as an [AWS Lambda](https://aws.amazon.com/lambda/).
This project uses [Quarkus AWS Lambda Extension](https://quarkus.io/guides/aws-lambda).

More specifically, this module is designed to run as an [API Gateway Lambda Authorizer](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html)

The output is a **zip** file, by default called **function.zip** which can be produced
to run in a JVM Runtime or a native runtime.

## Prerequisites

1. Java 21
2. Maven 3.6.3+
3. [Docker](https://docs.docker.com/get-docker/)

## Linting

Perform ktlint validation: `mvn antrun:run@ktlint`

Fix linting violations: `mvn antrun:run@ktlint-format`

**Note**: Not all linting violation are automatically fixable. Some might require manual fixing.

## Running the application locally in dev mode

In order to run the application locally in dev mode you first need to
start up the docker container that will stub external services like AWS or the OAuth Authorization Server.

`docker-compose -f infrastructure/docker-compose.yaml up -d`

Next copy the arn provided after running the `docker compose up command` and
replace the value of the `secrets.arn` property

You can then run `mvn quarkus:dev`

## Packaging

### JVM

`mvn package`

### Native
`mvn package -Pnative -Dquarkus.native.container-build=true`


## Run tests

### Unit tests
`mvn test`

### Integration tests
`mvn verify`
