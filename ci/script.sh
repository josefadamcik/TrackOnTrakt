#!/usr/bin/env bash
./gradlew --stacktrace assemble
./gradlew --stacktrace check
if [ $TRAVIS_TAG ]; then
  echo "Assembling and publishing release"
  ./gradlew publishApkRelease
else
  echo "Running Espresso tests"
  travis_wait 60 ./gradlew --stacktrace jacocoTestReport
fi
