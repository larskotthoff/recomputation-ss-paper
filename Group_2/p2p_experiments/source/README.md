# P2P Experiments Source
The Experiment is written in Java and uses Maven build tool.

In order to run the experiment, [Maven](https://maven.apache.org/download.cgi) must be installed.
To run the experiment, execute: 
    
    mvn clean test -Dtest=ChordExperiment -Dexperiment.host_names=host1,host2,host3

The host names must be specified in a comma separated string via `experiment.host_names` System property.
Alternatively, host names can be specified in [pom.xml](https://github.com/larskotthoff/recomputation-ss-paper/blob/master/Group_2/p2p_experiments/source/pom.xml#L13) file, in which case there is no need for inline setting of host name parameter.

For each given host, the experiment uploads necessary files via SSH and deploys a Chord node.
By default the experiment uses `~/.ssh/id_rsa` public key for ssh authentication, and assumes the SSH private key is not encrypted.

The SSH authentication method is customisable via [ChordExperiment.java](https://github.com/larskotthoff/recomputation-ss-paper/blob/master/Group_2/p2p_experiments/source/src/test/java/uk/ac/standrews/cs/emcsr2014/group_2/ChordExperiment.java#L79).

The port number for the deployed Chord nodes is chosen automatically.


A shell script can be found [here](https://github.com/larskotthoff/recomputation-ss-paper/blob/master/Group_2/p2p_experiments/source/src/main/scripts/ubuntu_14_setup.sh) that installs required software on an Ubuntu 14.1 VM in order to run this experiment.
