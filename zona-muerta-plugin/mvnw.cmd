#!/bin/sh
# Maven Wrapper startup script

MAVEN_PROJECTBASEDIR="${BASH_SOURCE[0]%%\$(*.sh)}"
MAVEN_PROJECTBASEDIR=$( cd "$( dirname "$MAVEN_PROJECTBASEDIR" )" && pwd )

exec java -jar "$(dirname "$MAVEN_PROJECTBASEDIR")/maven-wrapper.jar" "$@"
