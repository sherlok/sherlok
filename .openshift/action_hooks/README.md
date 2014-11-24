## OpenShift

Sherlok can be easily deployed on [OpenShift](http://openshift.redhat.com).



## Create new OpenShift application

    rhc app create sherlok -t diy-0.1 --from-code=https://github.com/renaud/sherlok.git


## Test locally

    export OPENSHIFT_REPO_DIR=`pwd`
    export OPENSHIFT_DIY_PORT='9688'
    export OPENSHIFT_DIY_IP='0.0.0.0'
    export OPENSHIFT_LOG_DIR=`pwd`

    sh .openshift/action_hooks/start
    open http://localhost:9688/index.html
    
Then

    sh .openshift/action_hooks/stop

## OpenShift Logs

1. SSH into my gear rhc ssh
2. Change directory into my logs directory `cd $OPENSHIFT_LOG_DIR`
3. List the contents ls

## Source

The start/stop script have been adapted from: 

* https://github.com/renaud/brainconnectivity-openshift
* https://github.com/giulivo/openshift-hellotornado
* https://github.com/openshift-quickstart/jetty-openshift-quickstart
