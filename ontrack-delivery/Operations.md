Operations
==========

### Preparing Digital Ocean environment

Details of using Digital Ocean as a Vagrant provided are given in the
[vagrant-digitalocean](see https://github.com/smdahlen/vagrant-digitalocean) plug-in documentation.

* install the Digital Ocean plug-in for Vagrant:

    vagrant plugin install vagrant-digitalocean

* create a Personal Access Token in Digital Ocean


### Connecting to a droplet at Digital Ocean

When the machine has been created in DO, you can connect to it as `root` using:

    ssh root@<ip>

where IP is the assigned one to the droplet.

### Docker management

Once on the Digital Ocean droplet, list the containers:

    docker ps -a

Two _ontrack_ containers must be there:

* `ontrack` - the main Ontrack container, which runs the application.
* `ontrack-data` - the data Ontrack container, which contains the working files for Ontrack (database, key, working
files). This container is stopped - it is normal, it hosts only the volume.

#### Connecting to the Ontrack container

To connect _inside_ the Ontrack manager:

    docker exec -it ontrack /bin/bash

This will open a _bash_ session inside the _ontrack_ container.

You can leave it using the _exit_ command.

#### Back up of Ontrack data

**TODO**

#### Looking at the logs live

**TODO**
