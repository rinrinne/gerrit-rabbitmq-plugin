#!/bin/bash

# Gerrit
git clone https://gerrit.googlesource.com/gerrit

# Buck
git clone https://gerrit.googlesource.com/buck
cd buck
ant
cd ..
