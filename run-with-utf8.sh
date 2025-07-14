#!/bin/bash

echo "Setting up UTF-8 encoding for Spring Boot application..."

# Set environment variables for UTF-8 encoding
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export JAVA_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Ho_Chi_Minh -Djava.awt.headless=true"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"

echo "Locale settings:"
echo "LANG: $LANG"
echo "LC_ALL: $LC_ALL"
echo "Java encoding options: $JAVA_OPTS"
echo "Maven encoding options: $MAVEN_OPTS"

echo ""
echo "Starting Spring Boot application with UTF-8 encoding..."
mvn spring-boot:run
