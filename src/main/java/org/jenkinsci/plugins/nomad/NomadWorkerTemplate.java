package org.jenkinsci.plugins.nomad;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.util.Secret;
import jenkins.model.Jenkins;

public class NomadWorkerTemplate implements Describable<NomadWorkerTemplate> {

    private static final String SLAVE_PREFIX = "jenkins";

    // persistent fields
    private final String prefix;
    private final int idleTerminationInMinutes;
    private final boolean reusable;
    private final int numExecutors;
    private final String labels;
    private final String jobTemplate;

    // legacy fields (we have to keep them for backward compatibility)
    private transient String region;
    private transient int cpu;
    private transient int memory;
    private transient int disk;
    private transient int priority;
    private transient List<? extends NomadConstraintTemplate> constraints;
    private transient String remoteFs;
    private transient Boolean useRawExec;
    private transient String image;
    private transient Boolean privileged;
    private transient String network;
    private transient String username;
    private transient Secret password;
    private transient String prefixCmd;
    private transient Boolean forcePull;
    private transient String hostVolumes;
    private transient String switchUser;
    private transient Node.Mode mode;
    private transient List<? extends NomadPortTemplate> ports;
    private transient String extraHosts;
    private transient String dnsServers;
    private transient String securityOpt;
    private transient String capAdd;
    private transient String capDrop;
    private transient String datacenters;
    private transient String vaultPolicies;
    private transient Set<LabelAtom> labelSet;
    private transient List<? extends NomadDevicePluginTemplate> devicePlugins;
    private transient String driver;

    @DataBoundConstructor
    public NomadWorkerTemplate(
            String prefix,
            String labels,
            int idleTerminationInMinutes,
            boolean reusable,
            int numExecutors,
            String jobTemplate
    ) {
        this.prefix = prefix.isEmpty() ? SLAVE_PREFIX : prefix;
        this.idleTerminationInMinutes = idleTerminationInMinutes;
        this.reusable = reusable;
        this.numExecutors = numExecutors;
        this.labels = Util.fixNull(labels);
        this.jobTemplate = jobTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Descriptor<NomadWorkerTemplate> getDescriptor() {
        return Jenkins.get().getDescriptor(getClass());
    }

    public String createWorkerName() {
        return prefix + "-" + Long.toHexString(System.nanoTime());
    }

    public String getPrefix() {
        return prefix;
    }

    public int getIdleTerminationInMinutes() {
        return idleTerminationInMinutes;
    }

    public boolean isReusable() {
        return reusable;
    }

    public int getNumExecutors() {
        return numExecutors;
    }

    public String getLabels() {
        return labels;
    }

    public String getJobTemplate() {
        return jobTemplate;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<NomadWorkerTemplate> {
        public static final String defaultJobTemplate = loadDefaultJobTemplate();

        private static String loadDefaultJobTemplate() {
            try {
                return IOUtils.toString(DescriptorImpl.class.getResource("/org/jenkinsci/plugins/nomad/jobTemplate.json"), UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
