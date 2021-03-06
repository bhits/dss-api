# Document Segmentation Service API

The Document Segmentation Service (DSS) API is responsible for the segmentation of the patient's sensitive health information using the privacy settings selected in the patient's consent. The segmentation process involves the following phases:

1. Document Validation: The DSS uses the [Document-Validator](https://github.com/bhits/document-validator) to verify that the original document is a valid CCD document.
2. Fact Model Extraction: The DSS extracts a fact model, which is essentially based on the coded concepts in a CCD document.
3. Value Set Lookup: For every code and code system in the fact model, the DSS uses the Value Set Service (VSS) API to lookup the value set categories. The value set categories are also stored in the fact model for future use in the *Redaction* and *Tagging* phases.
4. BRMS (Business Rules Management Service) Execution: The DSS retrieves the business rules that are stored in a *[JBoss Guvnor](http://guvnor.jboss.org/)* instance and executes them with the fact model. The business rule execution response might contain directives regarding the *Tagging* phase.
5. Redaction: The DSS redacts the health information that the patient does not want to share according to the privacy settings in the patient's consent. *NOTE: Additional Redaction Handlers are also being configured for other clean-up purposes.*
6. Tagging: The DSS tags the document based on the business rule execution response from the *BRMS Execution* step.
7. Clean Up: The DSS removes the custom elements that were added to the document for tracing purposes.
8. Segmented Document Validation: The DSS validates the final segmented document before returning it to ensure the output of DSS is still a valid CCD document.
9. Auditing: If enabled, the DSS also audits the segmentation process using *Logback Audit* server.

## Build

### Prerequisite
 
+ [Oracle Java JDK 8 with Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [Docker Engine](https://docs.docker.com/engine/installation/) (for building a Docker image from the project)
+ [Logback-Audit](http://audit.qos.ch/)
+ [JBoss Drools Guvnor](https://docs.jboss.org/drools/release/5.5.0.Final/drools-guvnor-docs/html_single/)

### Commands

This is a Maven project and requires [Apache Maven](https://maven.apache.org/) 3.3.3 or greater to build it. It is recommended to use the *Maven Wrapper* scripts provided with this project. *Maven Wrapper* requires an internet connection to download Maven and project dependencies for the very first build.

To build the project, navigate to the folder that contains `pom.xml` file using the terminal/command line.

+ To build a JAR:
    + For Windows, run `mvnw.cmd clean install`
    + For *nix systems, run `mvnw clean install`
+ To build a Docker Image (this will create an image with `bhits/dss:latest` tag):
    + For Windows, run `mvnw.cmd clean package docker:build`
    + For *nix systems, run `mvnw clean package docker:build`

## Run
### Prerequisite
In order to run DSS successfully, Logback-Audit and Guvnor Servers are need to be stood up first. Please refer the deployment instruction links below

+ [Logback-Audit deployment instruction](https://github.com/bhits/logback-audit)
+ [JBoss Drools Guvnor deployment instruction](https://github.com/bhits/dockerized-drools-guvnor)

After the two servers are up, the hostname (currently is localhost) in the [default configuration](https://github.com/bhits/dss-api/blob/master/dss/src/main/resources/application.yml) need to be replaced with the real server name where those two Apps are running

Logback-Audit configuration section

```yml
...
    audit-service:
      host: localhost
      port: 9630
...
```

Guvnor configuration section
```yml
...
c2s:
  brms:
    guvnor:
      endpointAddress: http://localhost/guvnor-5.5.0.Final-tomcat-6.0/rest/packages/AnnotationRules/source
      serviceUsername: admin
      servicePassword: admin
...
```


### Commands

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project and serves the API via an embedded Tomcat instance. Therefore, there is no need for a separate application server to run this service.
+ Run as a JAR file: `java -jar dss-x.x.x-SNAPSHOT.jar <additional program arguments>`
+ Run as a Docker Container: `docker run -d bhits/dss:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in the C2S application, it is required to setup the dependency microservices and support level infrastructure. Please refer to the [C2S Deployment Guide](https://github.com/bhits/consent2share/releases/download/2.1.0/c2s-deployment-guide.pdf) for instructions to setup the C2S infrastructure.*

## Configure

This API utilizes [`Configuration Server`](https://github.com/bhits/config-server) which is based on [Spring Cloud Config](https://github.com/spring-cloud/spring-cloud-config) to manage externalized configuration, which is stored in a `Configuration Data Git Repository`. We provide a [`Default Configuration Data Git Repository`]( https://github.com/bhits/c2s-config-data).

This API can run with the default configuration, which is targeted for a local development environment. Default configuration data is from three places: `bootstrap.yml`, `application.yml`, and the data which `Configuration Server` reads from `Configuration Data Git Repository`. Both `bootstrap.yml` and `application.yml` files are located in the `resources` folder of this source code.
  		  
We **recommend** overriding the configuration as needed in the `Configuration Data Git Repository`, which is used by the `Configuration Server`.
  		  
Also, please refer to [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) to see how the config server works, [Spring Boot Externalized Configuration](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) documentation to see how Spring Boot applies the order to load the properties, and [Spring Boot Common Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html) documentation to see the common properties used by Spring Boot.

### Examples for Overriding a Configuration in Spring Boot

#### Override a Configuration Using Program Arguments While Running as a JAR:

+ `java -jar dss-x.x.x-SNAPSHOT.jar --server.port=80`

#### Override a Configuration Using Program Arguments While Running as a Docker Container:

+ `docker run -d bhits/dss:latest --server.port=80 `

+ In a `docker-compose.yml`, this can be provided as:
```yml
version: '2'
services:
...
  dss.c2s.com:
    image: "bhits/dss:latest"
    command: ["--server.port=80"]
...
```
*NOTE: Please note that these additional arguments will be appended to the default `ENTRYPOINT` specified in the `Dockerfile` unless the `ENTRYPOINT` is overridden.*

### Enable SSL

For simplicity in development and testing environments, SSL is **NOT** enabled by default configuration. SSL can easily be enabled following the examples below:

#### Enable SSL While Running as a JAR

+ `java -jar dss-x.x.x-SNAPSHOT.jar --spring.profiles.active=ssl --server.ssl.key-store=/path/to/ssl_keystore.keystore --server.ssl.key-store-password=strongkeystorepassword`

#### Enable SSL While Running as a Docker Container

+ `docker run -d -v "/path/on/dockerhost/ssl_keystore.keystore:/path/to/ssl_keystore.keystore" bhits/dss:latest --spring.profiles.active=ssl --server.ssl.key-store=/path/to/ssl_keystore.keystore --server.ssl.key-store-password=strongkeystorepassword`
+ In a `docker-compose.yml`, this can be provided as:
```yml
version: '2'
services:
...
  dss.c2s.com:
    image: "bhits/dss:latest"
    command: ["--spring.profiles.active=ssl","--server.ssl.key-store=/path/to/ssl_keystore.keystore", "--server.ssl.key-store-password=strongkeystorepassword"]
    volumes:
      - /path/on/dockerhost/ssl_keystore.keystore:/path/to/ssl_keystore.keystore
...
```

*NOTE: As seen in the examples above, `/path/to/ssl_keystore.keystore` is made available to the container via a volume mounted from the Docker host running this container.*

### Override Java CA Certificates Store In Docker Environment

Java has a default CA Certificates Store that allows it to trust well-known certificate authorities. For development and testing purposes, one might want to trust additional self-signed certificates. In order to override the default Java CA Certificates Store in a Docker container, one can mount a custom `cacerts` file over the default one in the Docker image as follows: `docker run -d -v "/path/on/dockerhost/to/custom/cacerts:/etc/ssl/certs/java/cacerts" bhits/dss:latest`

*NOTE: The `cacerts` references given regarding volume mapping above are files, not directories.*

[//]: # (## API Documentation)

[//]: # (## Notes)

[//]: # (## Contribute)

## License
View [license](https://github.com/bhits/dss-api/blob/master/LICENSE) information for the software contained in this repository.

## Contact

If you have any questions, comments, or concerns please see [Consent2Share](https://bhits.github.io/consent2share/) project site.

## Report Issues

Please use [GitHub Issues](https://github.com/bhits/dss-api/issues) page to report issues.

[//]: # (License)
