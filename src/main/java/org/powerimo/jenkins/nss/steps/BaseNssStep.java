package org.powerimo.jenkins.nss.steps;

import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseNssStep extends Step implements Serializable {
    private static final long serialVersionUID = -3835372645565591493L;

    @DataBoundSetter
    @Getter
    @Setter
    private boolean failOnError = true;

    @DataBoundSetter
    @Getter
    @Setter
    private boolean dryRun = false;

    @DataBoundSetter
    @Getter
    @Setter
    private String apiKey;

    @DataBoundSetter
    @Getter
    @Setter
    private String typeNotification = "DEPLOY";

    @DataBoundSetter
    @Getter
    @Setter
    private String transports = "EMAIL;TELEGRAM";

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
    private String caption = "The job has been completed successfully";

    protected String getAccountIdStringFromApiKey() {
        return getAccountPartFromApiKey(apiKey);
    }

    public static String getAccountPartFromApiKey(String s) {
        if (null == s || !s.contains(":")) {
            return null;
        }
        return s.substring(0, s.indexOf(":"));
    }
}
