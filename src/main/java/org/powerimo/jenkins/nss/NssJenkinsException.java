package org.powerimo.jenkins.nss;

public class NssJenkinsException extends RuntimeException {
    public NssJenkinsException() {
        super();
    }

    public NssJenkinsException(String message) {
        super(message);
    }

    public NssJenkinsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NssJenkinsException(Throwable cause) {
        super(cause);
    }
}
