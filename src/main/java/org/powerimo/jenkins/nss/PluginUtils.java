package org.powerimo.jenkins.nss;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import org.powerimo.jenkins.nss.steps.BaseNssStep;
import org.powerimo.nss.api.NssRequest;

public class PluginUtils {
    public static NssRequest createStateRequest(BaseNssStep step, Run<?, ?> run, Job<?, ?> job) {
        if (job == null) {
            return createStateRequestRun(step, run);
        } else {
            return createStateRequestRunJob(step, run, job);
        }
    }

    public static NssRequest createStateRequestRun(BaseNssStep step, Run<?, ?> run) {
        var name = run.getFullDisplayName();
        var duration = run.getDurationString();
        var stateIcon = getStateIcon(run.getResult());
        var caption = stateIcon + " The job has been completed";
        var result = run.getResult() != null ? run.getResult().toString() : "no result";

        StringBuilder sb = new StringBuilder()
                .append("The job completed with the result: " + result + "\n")
                .append("name: <b>").append(name).append("</b>\n")
                .append("duration: <b>").append(duration).append("</b>\n")
                .append("build number: <b>").append(run.getNumber()).append("</b>\n")
                .append("URL: ").append(run.getUrl()).append("\n");

        return NssRequest.builder()
                .caption(caption)
                .typeNotification(step.getTypeNotification())
                .recipients(step.getRecipients())
                .transports(step.getTransports())
                .groups(step.getGroups())
                .message(sb.toString())
                .build();
    }

    public static NssRequest createStateRequestRunJob(BaseNssStep step, Run<?,?> run, Job<?, ?> job) {
        var name = job.getDisplayName();
        var duration = run.getDuration();
        var stateIcon = getStateIcon(run.getResult());
        var caption = stateIcon + " The job has been completed";
        var result = run.getResult() != null ? run.getResult().toString() : "no result";

        StringBuilder sb = new StringBuilder()
                .append("The job completed with the result: " + result + "\n")
                .append("name: <b>").append(name).append("</b>\n")
                .append("duration: <b>").append(duration).append("</b>\n")
                .append("build number: <b>").append(run.getNumber()).append("</b>\n")
                .append("URL: ").append(run.getUrl()).append("\n")
                ;

        return NssRequest.builder()
                .caption(caption)
                .typeNotification(step.getTypeNotification())
                .recipients(step.getRecipients())
                .transports(step.getTransports())
                .groups(step.getGroups())
                .message(sb.toString())
                .build();
    }

    public static String getStateIcon(Result result) {
        if (result == null) {
            return PluginConst.ICON_QUESTION;
        } else if (result.ordinal == Result.FAILURE.ordinal) {
            return PluginConst.ICON_RED_CIRCLE;
        } else if (result.ordinal == Result.ABORTED.ordinal) {
            return PluginConst.ICON_STOP;
        } else if (result.ordinal == Result.NOT_BUILT.ordinal) {
            return PluginConst.ICON_WARNING;
        } else if (result.ordinal == Result.SUCCESS.ordinal) {
            return PluginConst.ICON_OK;
        } else {
            return PluginConst.ICON_QUESTION;
        }
    }


}
