package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import java.util.Collections;
import java.util.Set;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

public class OnboardingStep extends Step {

    private final String category;

    @DataBoundConstructor
    public OnboardingStep(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new OnboardingStepExecution(this, context);
    }

    @Extension
    @Symbol("onboardingStep")
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "onboardingStep";
        }

        @Override
        public String getDisplayName() {
            return "Onboarding Step";
        }

        public ListBoxModel doFillCategoryItems() {
            ListBoxModel items = new ListBoxModel();
            OnboardingConfiguration globalConfig = GlobalConfiguration.all().get(OnboardingConfiguration.class);
            if (globalConfig != null) {
                for (ListEntry category : globalConfig.getEntries()) {
                    items.add(category.getName(), category.getName());
                }
            }
            return items;
        }
    }
}
