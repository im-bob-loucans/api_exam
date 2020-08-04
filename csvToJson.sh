#!/bin/bash -e

inputPath=$1
outputPath=$2
errorPath=$3

java -jar dist/csvToJson-1.0-SNAPSHOT.jar
