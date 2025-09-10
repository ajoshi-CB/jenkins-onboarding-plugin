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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class OnboardingTask extends Builder implements SimpleBuildStep {

    private static final String LATEST_JOB_PER_CATEGORY_FILEPATH =
            Jenkins.get().getRootDir().getAbsolutePath() + "/latestCategoryJobs.txt";
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
        run.addAction(new BuildCategoryAction(category));
        updateLatestJobForCategory(run.getParent().getFullName(), category, listener);
    }

    private synchronized void updateLatestJobForCategory(String jobName, String category, TaskListener listener) {
        Path categoryFilePath = Paths.get(LATEST_JOB_PER_CATEGORY_FILEPATH);

        Map<String, String> categoryMap = new HashMap<>();

        if (Files.exists(categoryFilePath)) {
            try (Stream<String> lines = Files.lines(categoryFilePath)) {
                lines.forEach(line -> {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        categoryMap.put(parts[0].trim(), parts[1].trim());
                    } else {
                        listener.error("records are not stored correctly in file :" + LATEST_JOB_PER_CATEGORY_FILEPATH);
                    }
                });
            } catch (IOException e) {
                listener.error("Exception in updateLatestJobForCategory while reading from File : "
                        + LATEST_JOB_PER_CATEGORY_FILEPATH + " , Exception is : " + e.getMessage());
            }
        }
        categoryMap.put(category, jobName);

        try (BufferedWriter writer = Files.newBufferedWriter(categoryFilePath)) {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                writer.write(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            listener.error("Exception in updateLatestJobForCategory while writing to File : "
                    + LATEST_JOB_PER_CATEGORY_FILEPATH + " , Exception is : " + e.getMessage());
        }
    }

    @Extension
    @Symbol("onboardingTask")
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
