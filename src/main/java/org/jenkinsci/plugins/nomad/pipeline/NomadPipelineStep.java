package org.jenkinsci.plugins.nomad.pipeline;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.model.TaskListener;

/**
 * Provides the 'nomad' method for scripted pipelines.<br>
 * Parameters:<br>
 * <ul>
 *     <li>jobTemplate - Nomad Job Template (required, either in JSON or HCL)</li>
 *     <li>cloud - Name of the Nomad cloud configuration you want to use (Default: null, which means the first cloud is used)</li>
 *     <li>prefix - Node name is prefixed with this value (Default: jenkins-dt)</li>
 *     <li>remoteFS - Absolute path to the working directory of the remote agent (Default: null, which means hudson.model.slave.workspaceDir
 *     or JENKINS_HOME is used)</li>
 * </ul>
 */
public class NomadPipelineStep extends Step implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String jobTemplate;
    private String cloud;
    private String prefix;
    private String remoteFS;

    @DataBoundConstructor
    public NomadPipelineStep(String jobTemplate) {
        this.jobTemplate = jobTemplate;
        this.prefix = "jenkins-dt";
    }

    public String getJobTemplate() {
        return jobTemplate;
    }

    public String getCloud() {
        return cloud;
    }

    public String getRemoteFS() {
        return remoteFS;
    }

    public String getPrefix() {
        return prefix;
    }

    // === optional parameters start ===

    @DataBoundSetter
    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    @DataBoundSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @DataBoundSetter
    public void setRemoteFS(String remoteFS) {
        this.remoteFS = remoteFS;
    }

    // === optional parameters end ===

    @Override
    public StepExecution start(StepContext stepContext) {
        return new NomadStepExecution(stepContext, this);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "nomad";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }
}
