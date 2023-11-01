package org.powerimo.jenkins.nss.steps;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.ClassLoaderSanityThreadFactory;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import io.jenkins.cli.shaded.org.slf4j.MDC;
import jakarta.annotation.Nonnull;
import jenkins.model.Jenkins;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.powerimo.http.okhttp.StdPayloadConverter;
import org.powerimo.jenkins.nss.exceptions.NssJenkinsException;
import org.powerimo.jenkins.nss.PluginConst;
import org.powerimo.nss.api.NssRequest;
import org.powerimo.nss.api.client.NssHttpClientLocalConfig;
import org.powerimo.nss.httpclient.NssHttpClient;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class BaseNssExecutor<T> extends StepExecution {
    private static ExecutorService executorService;
    private transient volatile Future<?> task;
    private transient String threadName;
    private transient Throwable stopCause;
    protected transient final TaskListener listener;
    protected transient final Launcher launcher;
    protected transient final BaseNssStep step;
    protected transient final NssHttpClient nssHttpClient;
    protected transient final NssHttpClientLocalConfig config;

    protected BaseNssExecutor(BaseNssStep step, @Nonnull StepContext context) throws InterruptedException, IOException {
        super(context);
        listener = context.get(TaskListener.class);
        launcher = context.get(Launcher.class);
        this.step = step;

        config = NssHttpClientLocalConfig.builder()
                .url("https://app.powerimo.cloud/nss")
                .apiKey(getEffectiveApiKey())
                .build();
        nssHttpClient = new NssHttpClient(config);
        nssHttpClient.setConfig(config);
        nssHttpClient.setPayloadConverter(new StdPayloadConverter());
        nssHttpClient.setHttpClient(new OkHttpClient());
    }

    static synchronized ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool(
                    new NamingThreadFactory(
                            new ClassLoaderSanityThreadFactory(new DaemonThreadFactory()),
                            "org.powerimo.jenkins.nss.BaseNssExecutor"
                    )
            );
        }
        return executorService;
    }

    protected abstract T run() throws Exception;

    @Override
    public boolean start() throws Exception {
        var auth = Jenkins.getAuthentication2();
        AtomicBoolean result = new AtomicBoolean(false);
        task = getExecutorService().submit(() -> {
            threadName = Thread.currentThread().getName();
            try {
                MDC.put("execution.id", UUID.randomUUID().toString());
                T ret;
                try (ACLContext acl = ACL.as2(auth)) {
                    ret = run();
                }
                getContext().onSuccess(ret);
                result.set(true);
            } catch (Throwable ex) {
                if (stopCause == null) {
                    getContext().onFailure(ex);
                } else {
                    stopCause.addSuppressed(ex);
                }
            } finally {
                MDC.clear();
            }
        });
        return false;
    }

    @Override
    public void stop(Throwable cause) throws Exception {
        if (task != null) {
            stopCause = cause;
            task.cancel(true);
        }
        super.stop(cause);
    }

    @Override
    public void onResume() {
        listener.getLogger().println("");
        getContext().onFailure(
                new Exception("Resume after a restart not supported for non-blocking synchronous steps"));
    }

    @Override
    public String getStatus() {
        if (threadName != null) {
            return "running in thread: " + threadName;
        } else {
            return "not yet scheduled";
        }
    }

    protected String getEffectiveApiKey() throws IOException, InterruptedException {
        if (step.getApiKey() != null)
            return step.getApiKey();
        EnvVars envVars = getContext().get(EnvVars.class);
        return envVars.get(PluginConst.ENV_VAR_API_KEY);
    }

    protected UUID getEffectiveAccountId() throws IOException, InterruptedException {
        // get value from arguments
        var s = step.getAccountIdStringFromApiKey();
        if (s != null) {
            try {
                return UUID.fromString(s);
            } catch (Exception ex) {
                throw new IllegalArgumentException("apiKey argument doesn't contains a valid account ID (UUID)" );
            }
        }

        // get value from EnvVars
        EnvVars envVars = getContext().get(EnvVars.class);
        assert envVars != null;

        s = envVars.get(PluginConst.ENV_VAR_API_KEY);
        if (s == null) {
            throw new NssJenkinsException("No effective API Key found (parameter apiKey or environment variable " + PluginConst.ENV_VAR_API_KEY);
        }

        try {
            var accountPart = BaseNssStep.getAccountPartFromApiKey(s);
            var accountId = UUID.fromString(accountPart);
            log.info("extracted accountId: {}", accountId);
            return accountId;
        } catch (Exception ex) {
            throw new IllegalArgumentException(PluginConst.ENV_VAR_API_KEY + " environment variable doesn't contains a valid account ID (UUID)" );
        }
    }

    protected NssRequest sendRequest(NssRequest request) {
        listener.getLogger().println("Request: " + request.toString());
        NssRequest sent;
        if (step.isDryRun()) {
            sent = request;
        } else {
            sent = nssHttpClient.sendRequest(request);
        }
        listener.getLogger().println("Result: " + sent.toString());
        return sent;
    }
}
