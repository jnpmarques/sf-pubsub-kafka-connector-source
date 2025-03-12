Welcome to your new Kafka Connect connector!

# Running in development

Personalize the files on the config folder:
* Connect.properties
* MySourceConnector.properties

Then to run in standalone:
```
./compile.sh
./relaunch.sh
```

Note: You will have to have the KAFKA_HOME environment variable configured.

If you intend to install the connector in Confluent Cloud you will have to compile it in JAVA 11.
