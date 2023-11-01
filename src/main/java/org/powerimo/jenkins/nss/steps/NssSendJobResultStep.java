package org.powerimo.jenkins.nss.steps;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.powerimo.jenkins.nss.PluginUtils;

import java.io.IOException;
import java.util.Set;

@Getter
public class NssSendJobResultStep extends BaseNssStep {
    private static final long serialVersionUID = 2333099919821775634L;

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet
                    .of(Launcher.class, FilePath.class, Run.class, TaskListener.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "nssSendJobResult";
        }
    }

    @DataBoundConstructor
    public NssSendJobResultStep() {

    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    public static class Execution extends BaseNssExecutor {
        private static final long serialVersionUID = -2482604171209973001L;

        protected Execution(BaseNssStep step, @Nonnull StepContext context) throws InterruptedException, IOException {
            super(step, context);
        }

        private NssSendJobResultStep getMyStep() {
            if (step instanceof NssSendJobResultStep) {
                return (NssSendJobResultStep) step;
            }
            throw new RuntimeException("The step class is not applicable");
        }

        @Override
        protected Object run() throws IOException, InterruptedException {
            final Run<?,?> run = getContext().get(Run.class);
            assert run != null;
            final Job<?, ?> job = run.getParent();
            var request = PluginUtils.createStateRequest(getMyStep(), run, job);
            return sendRequest(request);
        }

    }

}
