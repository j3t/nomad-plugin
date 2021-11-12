package org.jenkinsci.plugins.nomad.pipeline;

import java.util.ArrayList;

import org.jenkinsci.plugins.nomad.NomadCloud;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.Result;

public class NomadPipelineStepTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Test
    public void testNomadCloudDoesNotExist() throws Exception {
        // GIVEN

        // WHEN
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("nomad (jobTemplate: '', cloud: 'aaaa') {\n" +
                "  node (JOB_LABEL) {\n" +
                "    sh hostname\n" +
                "  }\n" +
                "}", true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(b);

        // THEN
        j.assertBuildStatus(Result.FAILURE, b);
        j.assertLogContains("ERROR: Nomad cloud does not exist: aaaa", b);
    }

    @Test
    public void testNoNomadCloudFound() throws Exception {
        // GIVEN

        // WHEN
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("nomad (jobTemplate: '') {\n" +
                "  node (JOB_LABEL) {\n" +
                "    sh hostname\n" +
                "  }\n" +
                "}", true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(b);

        // THEN
        j.assertBuildStatus(Result.FAILURE, b);
        j.assertLogContains("ERROR: No Nomad cloud was found. Please configure at least one!", b);
    }

    @Test
    public void testDefaultNomadCloudFound() throws Exception {
        // GIVEN
        NomadCloud cloud = new NomadCloud(
                "aaa",
                "http://localhost:4646",
                false,
                null,
                null,
                null,
                null,
                1,
                null,
                false,
                new ArrayList<>()
        );
        j.jenkins.clouds.add(cloud);
        j.jenkins.save();

        // WHEN
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("nomad (jobTemplate: '') {\n" +
                "  print \"${JOB_LABEL == null ? 'JOB_LABEL null' : 'JOB_LABEL not null'}\" \n" +
                "}", true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(b);

        // THEN
        j.assertBuildStatusSuccess(b);
        j.assertLogContains("JOB_LABEL not null", b);
    }

    @Test
    public void testNamedNomadCloudFound() throws Exception {
        // GIVEN
        NomadCloud cloud = new NomadCloud(
                "aaa",
                "http://localhost:4646",
                false,
                null,
                null,
                null,
                null,
                1,
                null,
                false,
                new ArrayList<>()
        );
        j.jenkins.clouds.add(cloud);
        j.jenkins.save();

        // WHEN
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("nomad (jobTemplate: '', cloud: 'aaa') {\n" +
                "  print \"${JOB_LABEL == null ? 'JOB_LABEL null' : 'JOB_LABEL not null'}\" \n" +
                "}", true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(b);

        // THEN
        j.assertBuildStatusSuccess(b);
        j.assertLogContains("JOB_LABEL not null", b);
    }
}