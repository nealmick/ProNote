#!/bin/bash

# Get the current working directory
PROJECT_DIR="$(pwd)"

# Command to start the Spring Boot application
START_CMD="./gradlew bootRun"

# PID of the running Spring Boot application
APP_PID=0

# Function to start the Spring Boot application
start_app() {
    echo "Starting Spring Boot application..."
    $START_CMD &
    APP_PID=$!
    echo "Spring Boot application started with PID $APP_PID"
}

# Function to stop the Spring Boot application
stop_app() {
    if [ $APP_PID -ne 0 ]; then
        echo "Stopping Spring Boot application with PID $APP_PID..."
        kill $APP_PID
        wait $APP_PID 2>/dev/null
        echo "Spring Boot application stopped."
    fi
}

# Initial start of the application
start_app

# Define patterns to exclude from watching
EXCLUDE_PATTERNS=".*\.(class|log)$|.*build/.*|.*\.git.*"

# Watch for changes in the source directories and build.gradle, excluding some patterns
fswatch -o --exclude="$EXCLUDE_PATTERNS" $PROJECT_DIR/src $PROJECT_DIR/build.gradle | while read f
do
    echo "Change detected. Restarting application..."
    stop_app
    start_app
done
