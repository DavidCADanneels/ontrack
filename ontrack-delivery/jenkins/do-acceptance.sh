# Environment

REPOSITORY=/var/lib/jenkins/repository/ontrack/2.0

# Cleanup

rm -rf ${WORKSPACE}/*

# Extract the Docker delivery JAR

unzip ${REPOSITORY}/ontrack-delivery-docker-${ONTRACK_VERSION_FULL}.jar -d ${WORKSPACE}

# Nginx preparation of files

./nginx.sh \
    --host=ontrack \
    --proxy-name=\$host

# export VAGRANT_LOG=debug

./vagrant-install.sh \
    --vagrant-host="ontrack-acceptance-${ONTRACK_VERSION_FULL}" \
    --vagrant=${WORKSPACE}/vagrant \
    --vagrant-provider=digital_ocean \
    --image=nemerosa/ontrack:${ONTRACK_VERSION_FULL} \
    --authorized-key=/var/lib/jenkins/repository/ontrack/keys/mac.key \
    --nginx-certs=${WORKSPACE}/build/certs \
    --nginx-sites-enabled=${WORKSPACE}/build/sites-enabled \
    --do-image="docker" \
    --do-token=${DO_TOKEN} \
    --do-region=ams2 \
    --do-size=512mb

# Gets the IP of the droplet

DROPLET_IP=`python digitalocean-ip.py --name ontrack-acceptance-${ONTRACK_VERSION_FULL} --token ${DO_TOKEN}`

echo "Droplet IP to test: ${DROPLET_IP}"

# Running the acceptance tests

cd ${WORKSPACE}
${WORKSPACE}/acceptance.sh \
    --jar=${REPOSITORY}/ontrack-acceptance-${ONTRACK_VERSION_FULL}.jar \
    --no-ssl \
    --ontrack-url=https://${DROPLET_IP}

