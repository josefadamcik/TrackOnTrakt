#!/usr/bin/env bash
mkdir -p artifacts
build_dirs=(
"outputs"
"reports"
"jacoco"
"test-results"
"spoon-output"
)

for i in "${build_dirs[@]}"
do
  tar cvjf artifacts/${i}.tar.bz2 app/build/${i}
done
