FROM java:8
VOLUME /java
ADD dss-1.1.0-SNAPSHOT.jar /dss.jar
ENTRYPOINT ["java","-Djavax.net.ssl.trustStore=/java/keystore/mhc.keystore","-Djavax.net.ssl.trustStorePassword=changeit","-jar","/dss.jar"]