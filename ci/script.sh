#!/usr/bin/env bash

if [ $TRAVIS_TAG ]; then
  echo "Assembling and publishing release"
  ./gradlew publishApkRelease
else
  echo "Running Espresso tests"
  ./gradlew --stacktrace jacocoTestReport
fi
