Jenkins Nomad Cloud Plugin
==========================

This plugin uses HashiCorp's [Nomad scheduler](https://www.nomadproject.io/) to 
provision new build workers based on workload.

All documentation is available under the Jenkins [wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Nomad+Plugin) for this plugin

**Community contributions are very welcome!**

## TLS Support

To connect to a TLS-enabled Nomad cluster:

* Configure the *Nomad URL* field with a HTTPS URL, for example: `https://nomad.service.consul:4646`
* Tick the *Enable TLS* checkbox
  - If the Nomad cluster authenticates clients, configure the path to the PKCS12
    certificate and, if needed, the password to access the PKCS12 certificate.

  - Specify a custom PKCS12 certificate to authenticate the Nomad cluster, if
    it can't be verified by the default truststore used by the Jenkins
    controller.

Note that, in each case, the certificates:

* Must be files reachable by the Jenkins controller.
* Must be in the [PKCS12 format](https://en.wikipedia.org/wiki/PKCS_12).

## Pipeline support
For pipelines, nodes can be started by using the label which is assigned to a predefined job template.
```
node ('my-label') {
}
```
Or the template can be provided directly (either in JSON or HCL).
```
nomad (jobTemplate:'''
job "%WORKER_NAME%" {
  region = "global"
  type = "batch"
  datacenters = ["dc1"]
  group "jenkins-worker-taskgroup" {
    count = 1
    restart {
      attempts = 0
      mode = "fail"
    }
    task "jenkins-worker" {
      driver = "docker"
      config {
        image = "jenkins/inbound-agent"
        force_pull = true
      }
      env {
        JENKINS_URL = "https://jenkins.service.consul"
        JENKINS_AGENT_NAME = "%WORKER_NAME%"
        JENKINS_SECRET = "%WORKER_SECRET%"
        JENKINS_WEB_SOCKET = "true"
      }
      resources {
        cpu = 500
        memory = 256
      }
    }
  }
}
''') {
  node (JOB_LABEL) {
    sh 'hostname'
  }
}
```
Note: The `JOB_LABEL` environment variable is defined by the `nomad` pipeline step with the corresponding label of the `dynamic template`.   

Via `readFile/readTrusted`, the `jobTemplate` can also be referenced from the file system or from the repository.
```
nomad (jobTemplate: readTrusted('src/test/resources/nomad-templates/build.yaml')) {
  node (JOB_LABEL) {
    sh 'hostname'
  }
}
```

Optional parameters:
* `cloud` - Name of the Nomad cloud configuration you want to use (Default: `null` which means the first Nomad cloud is selected automatically)
* `remoteFS` - Absolute path to the working directory of the remote agent (Default: `null` which means the environment variable `hudson.model.slave.workspaceDir` or `JENKINS_HOME` is used instead)
* `prefix` - Node name is prefixed with this value (Default: `jenkins-dt`)
