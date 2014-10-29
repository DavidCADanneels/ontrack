Automated deployment
====================

## Use cases

### Local acceptance tests

Launched using `localAcceptanceTests`. They just launch a local JVM with `ontrack` running in it, and accessed
 directly on http://localhost:9999. There is no need for a provisioned VM.
 
However, those tests do validate `ontrack` in a real situation, when deployed remotely, behind an `nginx` SSL proxy.

### Remote acceptance tests

We want to create a VM, deploy `ontrack` on it, run some tests and discard the VM. The created VM must be a real
one, with `ontrack` being deployed with the release artifact, and running behind an `nginx` SSL proxy.

### Production deployment

Same as for the _remote acceptance tests_, with the difference that the VM already exists. Additionally, a subset
of the acceptance tests can be run against the production server upon deployment.

## Technologies

### Vagrant + Puppet

Perfect to setup a VM, configure it, and discard it afterwards. The Puppet scripts must be independent from 
Vagrant since they can be used as-is for the production-like deployment. If both use cases use the same
provisioning mechanism, we can guarantee that the acceptance tests validate also the production deployment.

#### Java installation

There is no clean built-in JDK8 image for Vagrant, and setup of the JDK8 using Puppet, even with the official
images remains complex. Switching back to a pure `apt-get` installation.

#### Java installation (bis)

Downloading and installation of the JDK8 is not cached and takes too much time for a local and discardable
installation.

#### nginx installation

The official `nginx` puppet scripts seem to be in infancy stage and very unstable. Reverting also to a pure
`apt-get` installation and configuration.

This can prove to be difficult when dealing with certificates.

#### Vagrant vs. Puppet

We want the Puppet scripts to be independent from Vagrant. For the moment, the Puppet scripts are in the 
`vagrant` folder but should be extracted in their own folder.

### Docker

Docker has a big advantage when used in CI and compared to Vagrant + Puppet: it is tremendously fast!

#### Summary

To run create and run the `ontrack` image, having 8080 exposed on the host:

    ./docker-setup.sh --jar=<path to Ontrack JAR> --run --port=8080

`ontrack` data will be stored in the local `mount` directory. In particular, the log file is available in:

    less -f mount/log/spring.log

In order to upgrade `ontrack` to a new version:
    
    # Stops the running container
    docker stop `cat ontrack.cid`
    # Respawn a container with the new JAR file
    ./docker-setup.sh --jar=<path to new Ontrack JAR> --run --port=8080

That's it! The `ontrack` data in the `mount` directory is not touched.    

#### Installation of ontrack

To build the `ontrack` image:

    ./docker-setup.sh --jar=<path to Ontrack JAR>

This will create na `ontrack` Docker image whose version is computed from the JAR name (it can also
be forced using `--docker-version=<version>`. The image name can be changed using for example
`--docker-image=nemerosa/ontrack`.

This image can be listed using `docker images` and deleted using `docker rmi <image id>`.

To run the created image and connect to it: `docker run -t -i ontrack:<version> /bin/bash`. Warning: this will create
a container that you can list using `docker ps -a` and delete using `docker rm <container id>`.
 
To run the image in _real mode_:

* `docker run -d -P ontrack:<version>` will publish the container's 8080 port on the host 8080 port
* `docker run -d -p <port>:8080 ontrack:<version>` will publish the container's 8080 port on the host `<port>` port

The created docker container can be listed using `docker ps -a` and deleted using `docker rm <container id>`.

#### Implementation details

In order to have a fully running image, one needs the `/opt/ontrack/mount` volume to be mounted:

    docker run \
        -d \
        -P \
        -v `pwd`/mount:/opt/ontrack/mount \
        --cidfile=ontrack.cid \
        ontrack:<version>

This creates a local `ontrack.cid` file that can be used to control the container in an automated way, for example,
to remove it:

    docker rm -f `cat ontrack.cid`
    
Note that the setup sccript can be used to create the image and run it immediately:

    ./docker-setup.sh --jar=<path to Ontrack JAR> --run

Persistent data for Ontrack will be stored in a local `mount` directory (this can be overridden using
`--mount=<dir>`). The created container ID is stored in a local `ontrack.cid` like above. Adding `--bash` to the
command above would _not_ start `ontrack` but will open a Bash session on the container.

When the container is started, you can access `ontrack` by calling:

http://<ip>:<port>

where `<ip>` is the IP of the Docker host, and `<port>` is the forwarded port of the `ontrack` container.

Note that when using [boot2docker](http://boot2docker.io/), you can access the IP by running `boot2docker ip`.

By running the `docker-setup.sh` command above with `--port=<port>`, you will forward the container's 8080 port to
 the indicated `<port>`. So when running with `--port=8080`, you will be able to access Ontrack using http://<ip>:8080.

#### nginx link

*To be continued.*

> The idea is to have `nginx` run as a separate container that links to the `ontrack` container.

## Open points

### Portable acceptance tests

The acceptance tests are easy to run at build time, because of their link to the sources, but they need
actually to run outside of any source context, when run against remote acceptance servers and production.

## Usage

### Vagrant

Use cases:
* remote acceptance tests

Usage:

    # Set-up the machine
    ./vagrant-setup.sh \
        --jar=<path to Ontrack JAR>
    # Launches integration tests
    ./acceptance.sh \
        --jar=<path to Ontrack Acceptance JAR> \
        --ontrack-url=http://localhost:3000
    # Getting rid of the VM (or not)
    cd vagrant-local
    vagrant destroy -f
