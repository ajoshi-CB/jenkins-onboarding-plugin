package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.io.IOException;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

public class OnboardingTask extends Builder implements SimpleBuildStep {

    private final String category;

    @DataBoundConstructor
    public OnboardingTask(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        listener.getLogger().println("Selected Category is " + category);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

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

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Onboarding Task";
        }
    }
}
