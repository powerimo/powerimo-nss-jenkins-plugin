package org.powerimo.jenkins.nss;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.powerimo.nss.api.NssRequest;

import java.io.IOException;
import java.util.Set;

@Getter
public class NssSendTextStep extends BaseNssStep {
    private static final long serialVersionUID = -1644096475144059203L;

    @Getter
    private final String text;

    @DataBoundSetter
    @Getter
    @Setter
    private String typeNotification;

    @DataBoundSetter
    @Getter
    @Setter
    private String transports;

    @DataBoundSetter
    @Getter
    @Setter
    private String groups;

    @DataBoundSetter
    @Getter
    @Setter
    private String recipients;

    @DataBoundSetter
    @Getter
    @Setter
    private String caption;

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet
                    .of(Launcher.class, FilePath.class, Run.class, TaskListener.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "nssSendText";
        }
    }

    @DataBoundConstructor
    public NssSendTextStep(String text) {
        this.text = text;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new NssSendTextStepExecutor(this, context);
    }

    public static class NssSendTextStepExecutor extends BaseNssExecutor {
        private static final long serialVersionUID = -7371513693684974467L;

        protected NssSendTextStepExecutor(BaseNssStep step, @Nonnull StepContext context) throws InterruptedException, IOException {
            super(step, context);
        }

        private NssSendTextStep getMyStep() {
            if (step instanceof NssSendTextStep) {
                return (NssSendTextStep) step;
            }
            throw new RuntimeException("The step class is not applicable");
        }

        @Override
        protected Object run() {
            var text = getMyStep().getText();

            NssRequest sent;
            NssRequest prepared = NssRequest.builder()
                    .caption(getMyStep().getCaption())
                    .typeNotification(getMyStep().getTypeNotification())
                    .recipients(getMyStep().getRecipients())
                    .transports(getMyStep().getTransports())
                    .groups(getMyStep().getGroups())
                    .message(text)
                    .build();
            listener.getLogger().println("Request: " + prepared.toString());
            if (step.isDryRun()) {
                listener.getLogger().println("Dry Run: text for sending: " + text);
                sent = prepared;
            } else {
                sent = nssHttpClient.sendRequest(prepared);
            }

            listener.getLogger().println("Result: " + sent.toString());
            return sent;
        }
    }

}
