#!/bin/bash

export JAVA_TOOL_OPTIONS=

# mvn clean package
# cp ./target/source-connector*-jar-with-dependencies.jar connector-jars/source-connector.jar

export JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n
$KAFKA_HOME/bin/connect-standalone.sh ./config/Connect.properties ./config/MySourceConnector.properties