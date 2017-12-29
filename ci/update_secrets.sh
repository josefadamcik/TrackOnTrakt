#!/usr/bin/env bash
CIDIR="ci"
SECRETSDIR=${CIDIR}"/secrets"
if [ ! -d "$CIDIR" ]; then
    echo "Run from the root directory as ./ci/update_secrets"
fi

mkdir -p ${SECRETSDIR}
mkdir -p ${SECRETSDIR}"/app"
cp "app/fabric.properties" ${SECRETSDIR}"/app/"
cp "keys.properties" ${SECRETSDIR}

cd ${SECRETSDIR}
tar cvf ../secrets.tar *

travis encrypt-file -f ci/secrets.tar ci/secrets.tar.enc


