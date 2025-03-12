#!/bin/bash

export JAVA_TOOL_OPTIONS=

# mvn clean
# mvn package
# # cp ./target/*-jar-with-dependencies.jar connector-jars/source-connector.jar
# cp target/*-jar-with-dependencies.jar connector-jars/source-connector.jar

./gradlew build
rm -rf connector-jars
mkdir connector-jars
cp build/distributions/*.zip connector-jars
unzip connector-jars/*.zip -d connector-jars