#!/bin/bash

echo "Building WorldCopyPaste Plugin for CraftBukkit..."
echo

echo "Cleaning previous build..."
mvn clean

echo
echo "Building plugin..."
mvn package

echo
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Plugin JAR file created in target/ directory"
    echo "File: target/myplugin-1.0-SNAPSHOT.jar"
else
    echo "Build failed! Check the error messages above."
fi

echo
read -p "Press Enter to continue..."
