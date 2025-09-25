#!/bin/bash
cd JobScout
./mvnw clean package
java -jar target/JobScout-0.0.1-SNAPSHOT.jar
