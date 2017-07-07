#!/usr/bin/env bash
git tag
# Output current project version:
sbt version
# Errors out if the dynamically generated project version (based on the git version) is manually overriden sbt 
sbt dynverAssertVersion
