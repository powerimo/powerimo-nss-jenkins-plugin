package org.powerimo.jenkins.nss.steps;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powerimo.jenkins.nss.TestVirtualChannel;

import java.io.IOException;
import java.io.PrintStream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Base Test Class.
 *
 * @author Naresh Rayapati
 */
public class BaseTest {

    @Mock
    TaskListener taskListenerMock;
    @Mock
    Run<?, ?> runMock;
    @Mock
    EnvVars envVarsMock;
    @Mock
    PrintStream printStreamMock;
    @Mock
    StepContext contextMock;
    @Mock
    Launcher launcherMock;

    private AutoCloseable closeable;

    @Before
    public void setUpBase() throws IOException, InterruptedException {

        closeable = MockitoAnnotations.openMocks(this);

        when(runMock.getCauses()).thenReturn(null);
        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();
        when(launcherMock.getChannel()).thenReturn(new TestVirtualChannel());
        when(contextMock.get(Run.class)).thenReturn(runMock);
        when(contextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
        when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
        when(contextMock.get(Launcher.class)).thenReturn(launcherMock);
    }

    @After
    public void tearUpBase() throws Exception {
        closeable.close();
    }
}
