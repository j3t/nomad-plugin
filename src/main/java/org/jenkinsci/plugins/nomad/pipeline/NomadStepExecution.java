package org.jenkinsci.plugins.nomad.pipeline;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.jenkinsci.plugins.nomad.NomadCloud;
import org.jenkinsci.plugins.nomad.NomadWorkerTemplate;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import hudson.AbortException;
import jenkins.model.Jenkins;

/**
 * Executes a given 'nomad' step for scripted pipelines. How it works:<br>
 * <ol>
 *     <li>Resolve nomad cloud by either the specified name or use the first Nomad cloud</li>
 *     <li>Add the given job template with the cloud and assign a unique label to that template</li>
 *     <li>Define the environment variable JOB_LABEL with the corresponding label</li>
 *     <li>Execute the nomad step</li>
 *     <li>Remove the job template from the cloud</li>
 * </ol>
 * Note: The job templates added by this step are not editable, and they are also not visible via cloud management.
 */
public class NomadStepExecution extends StepExecution {

    private final NomadPipelineStep step;

    public NomadStepExecution(StepContext stepContext, NomadPipelineStep step) {
        super(stepContext);
        this.step = step;
    }

    @Override
    public boolean start() throws IOException, InterruptedException {
        NomadCloud cloud = resolveCloud();
        NomadWorkerTemplate template = new NomadWorkerTemplate(
                step.getPrefix(),
                UUID.randomUUID().toString(),
                0,
                false,
                1,
                step.getRemoteFS(),
                step.getJobTemplate()
        );

        cloud.addDynamicTemplate(template);

        getContext().newBodyInvoker()
                .withCallback(new RemoveTemplateCallBack(template.getLabels()))
                .withContext(EnvironmentExpander.merge(
                        getContext().get(EnvironmentExpander.class),
                        EnvironmentExpander.constant(Collections.singletonMap("JOB_LABEL", template.getLabels()))
                ))
                .start();

        return false;
    }

    private NomadCloud resolveCloud() throws AbortException {
        if (step.getCloud() == null) {
            return Optional.ofNullable(Jenkins.get().clouds.get(NomadCloud.class))
                    .orElseThrow(() -> new AbortException("No Nomad cloud was found. Please configure at least one!"));
        }
        return Optional.ofNullable(Jenkins.get().getCloud(step.getCloud()))
                .filter(cloud -> cloud instanceof NomadCloud)
                .map(cloud -> (NomadCloud) cloud)
                .orElseThrow(() -> new AbortException(String.format("Nomad cloud does not exist: %s", step.getCloud())));
    }

    private class RemoveTemplateCallBack extends BodyExecutionCallback.TailCall {
        private final String label;

        public RemoveTemplateCallBack(String label) {
            this.label = label;
        }

        @Override
        protected void finished(StepContext stepContext) throws AbortException {
            NomadCloud cloud = resolveCloud();
            cloud.removeDynamicTemplate(label);
        }
    }
}
