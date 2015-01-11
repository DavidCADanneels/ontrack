#!/bin/bash

# Maven environment

if [ "${MAVEN_3_2_X_HOME}" == "" ]
then
    echo "MAVEN_3_2_X_HOME is not set"
    exit 1
fi
export PATH=${MAVEN_3_2_X_HOME}/bin:$PATH
mvn --version

# Environment

REPOSITORY=/var/lib/jenkins/repository/ontrack/2.0

# Cleanup

rm -rf ${WORKSPACE}/*

# Extract the Docker delivery JAR

unzip ${REPOSITORY}/ontrack-delivery-docker-${VERSION_FULL}.jar -d ${WORKSPACE}

# Publication of the release

./publish.py \
    --repository=${REPOSITORY} \
    --version-commit=${VERSION_COMMIT} \
    --version-full=${VERSION_FULL} \
    --version-release=${VERSION_DISPLAY} \
    --ontrack-url=https://ontrack.nemerosa.net \
    --ontrack-branch=${VERSION_BRANCH} \
    --github-user=dcoraboeuf \
    --github-token=${GITHUB_TOKEN} \
    --ossrh-profile=${OSSRH_PROFILE}
