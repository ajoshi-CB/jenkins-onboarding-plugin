package io.jenkins.plugins.sample;

import hudson.model.TaskListener;
import java.io.Serial;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

public class OnboardingStepExecution extends SynchronousNonBlockingStepExecution<Void> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient OnboardingStep step;

    protected OnboardingStepExecution(OnboardingStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);
        listener.getLogger().println("Executing My Task with category: " + step.getCategory());
        return null;
    }
}
