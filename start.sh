#!/bin/bash
cd JobScout
mvn clean package
java -jar target/JobScout-0.0.1-SNAPSHOT.jar
