package org.powerimo.jenkins.nss.steps;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.jvnet.hudson.test.JenkinsRule;
import org.powerimo.common.utils.Utils;

import java.util.UUID;

public class NssSendJobResultStepTest extends BaseTest {
    private NssSendJobResultStep.Execution stepExecution;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void build_success() throws Exception {
        // Создание и настройка Pipeline
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "test-step-execution");

        String script = Utils.readTextResource("NssSendJobResult.groovy");
        job.setDefinition(new CpsFlowDefinition(
                script,
                true));

        // Запуск и ожидание завершения Pipeline
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));

        // Проверка лога на наличие ожидаемого результата
        jenkinsRule.assertLogContains("Finished: SUCCESS", run);
    }

    @Test
    public void send_noParams() throws Exception {
        final String apiKey = UUID.randomUUID() + ":mySecret";
        final NssSendJobResultStep step = new NssSendJobResultStep();
        step.setDryRun(true);
        stepExecution = new NssSendJobResultStep.Execution(step, contextMock);

        // Execute and assert Test.
        var result = stepExecution.run();

        Assertions.assertNotNull(result);
    }

}
