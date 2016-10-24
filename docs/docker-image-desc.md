# Short Description
This API segments patient's sensitive health information using the patient consent.

# Full Description

# Supported Tags and Respective `Dockerfile` Links

[`1.13.0`](https://github.com/bhits/dss-api/blob/master/dss/src/main/docker/Dockerfile),[`latest`](https://github.com/bhits/dss-api/blob/master/dss/src/main/docker/Dockerfile)[(1.13.0/Dockerfile)](https://github.com/bhits/dss-api/blob/master/dss/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits/dss-api).

# What is Document Segmentation Service API?

The Document Segmentation Service (DSS) API is responsible for the segmentation of the patient's sensitive health information using the patient consent. Segmentation invloves the following phase:

Document Validation: DSS uses [Document-Validator](https://github.com/bhits/document-validator) to verify that the document is a valid CCD document.

Fact Model Extraction: DSS extracts the fact model using the consent policy.

Value Set Lookup: For every code and code system in the fact model, it uses Value Set Service to lookup the value set categories which is store in the fact model.

BRMS (Business Rules Management Service) Execution: DSS uses the rule execution service to execute the clinical fact.

Redaction: DSS uses the patient consent and the concept code to redact the document.

Tagging: DSS tag the docment based on the business rules specified in [Guvnor](http://guvnor.jboss.org/).

Clean Up: DSS remove custom element that were added to the document for tracing.

Segmented Document Validation: DSS validate the segmented document before returning it.

For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits.github.io/consent2share/).
# How to use this image


## Start a DSS instance

Be sure to familiarize yourself with the repository's [README.md](https://github.com/bhits/dss-api) file before starting the instance.

`docker run  --name dss -d bhits/dss:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and support level infrastructure. Please refer to the [Consent2Share Deployment Guide](https://github.com/bhits/consent2share/releases/download/2.0.0/c2s-deployment-guide.pdf) for instructions to setup the Consent2Share infrastructure.*


## Configure

This API runs with a [default configuration](https://github.com/bhits/dss-api/blob/master/dss/src/main/resources/application.yml) that is primarily targeted for the development environment.  The Spring profile `docker` is actived by default when building images. [Spring Boot](https://projects.spring.io/spring-boot/) supports several methods to override the default configuration to configure the API for a certain deployment environment. 

Here is example to override default database password:

`docker run -d bhits/dss:latest --spring.datasource.password=strongpassword`

## Using a custom configuration file

To use custom `application.yml`, mount the file to the docker host and set the environment variable `spring.config.location`.

`docker run -v "/path/on/dockerhost/C2S_PROPS/dss/application.yml:/java/C2S_PROPS/dss/application.yml" -d bhits/dss:tag --spring.config.location="file:/java/C2S_PROPS/dss/"`

## Environment Variables

When you start the DSS image, you can edit the configuration of the DSS instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name dss -e JAR_FILE="dss-latest.jar" -v "/path/on/dockerhost/dss-latest.jar:/dss-latest.jar" -d bhits/dss:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name dss -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhits/dss:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup application arugument. The default value of is "--spring.profiles.active=docker".

`docker run --name dss -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=ssl,docker" -d bhits/dss:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.12.1.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](https://github.com/bhits/dss-api) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhits/dss-api](https://github.com/bhits/dss-api) GitHub repository. Be sure to familiarize yourself with the repository's README.md file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits/dss-api/issues).