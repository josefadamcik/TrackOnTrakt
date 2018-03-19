#!/usr/bin/env bash

if [ $TRAVIS_TAG ]; then
  echo "Assembling and publishing release"
  ./gradlew --profile --stacktrace  publishApkRelease
else
  echo "Running Espresso tests"
  ./gradlew --profile --stacktrace jacocoTestReport
fi
