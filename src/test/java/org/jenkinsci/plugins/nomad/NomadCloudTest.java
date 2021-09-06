package org.jenkinsci.plugins.nomad;

import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProvisioner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class NomadCloudTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    private NomadWorkerTemplate workerTemplate;
    private LabelAtom label;
    private NomadCloud nomadCloud;

    @Before
    public void setup() {
        label = new LabelAtom(UUID.randomUUID().toString());
        workerTemplate = new NomadWorkerTemplate(
                "jenkins",
                label.getName(),
                1,
                true,
                1,
                NomadWorkerTemplate.DescriptorImpl.defaultJobTemplate);

        nomadCloud = new NomadCloud(
                "nomad",
                "nomadUrl",
                false,
                null,
                null,
                null,
                null,
                1,
                "",
                false,
                Collections.singletonList(workerTemplate));
    }

    @Test
    public void testCanProvision() {
        Assert.assertTrue(nomadCloud.canProvision(label));
    }

    @Test
    public void testProvision() {
        int workload = 3;
        Collection<NodeProvisioner.PlannedNode> plannedNodes = nomadCloud.provision(label, workload);

        Assert.assertEquals(plannedNodes.size(), workload);
    }

}
