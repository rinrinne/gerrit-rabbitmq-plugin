#!/bin/bash

ROOT=${PWD}
VERSION=$1

# Checkout tag
#cd gerrit
#git checkout -b v${VERSION} refs/tags/v${VERSION}
#cd ..

# Add Symbolic link
#ln -fns ${ROOT} gerrit/plugins/rabbitmq

# Build
cd gerrit
${ROOT}/buck/bin/buck build plugins/rabbitmq:rabbitmq
