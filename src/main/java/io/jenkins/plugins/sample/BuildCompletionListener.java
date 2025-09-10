package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import jenkins.model.Jenkins;

@Extension
public class BuildCompletionListener extends RunListener<Run> {

    private static final String LATEST_BUILDS_FILEPATH =
            Jenkins.get().getRootDir().getAbsolutePath() + "/latestBuilds.txt";
    private static final int MAX_RECORDS = 5;

    @Override
    public synchronized void onCompleted(Run run, hudson.model.TaskListener listener) {
        OnboardingConfiguration config = OnboardingConfiguration.all().get(OnboardingConfiguration.class);
        Path RECORDS_FILE_PATH;
        if (config == null
                || config.getLatestBuildsFilePath() == null
                || config.getLatestBuildsFilePath().isEmpty()) {
            listener.error("Build records file path is not configured :");
            RECORDS_FILE_PATH = Path.of(LATEST_BUILDS_FILEPATH);
        } else {
            RECORDS_FILE_PATH = Path.of(config.getLatestBuildsFilePath());
        }
        BuildCategoryAction action = run.getAction(BuildCategoryAction.class);
        String category = (action != null) ? action.getCategory() : "";

        String buildInfo = String.format(
                "Job: %s, Build: %s, Category: %s, Result: %s",
                run.getParent().getFullName(), run.getNumber(), category, run.getResult());
        listener.getLogger().println("Build completed with Listener executed : " + buildInfo);

        LinkedList<String> latestFiveBuilds = new LinkedList<>();

        if (Files.exists(RECORDS_FILE_PATH)) {
            List<String> existingRecords = null;
            try {
                existingRecords = Files.readAllLines(RECORDS_FILE_PATH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            latestFiveBuilds.addAll(existingRecords);
        }

        if (latestFiveBuilds.size() >= MAX_RECORDS) {
            latestFiveBuilds.removeFirst();
        }
        latestFiveBuilds.add(buildInfo);

        try (FileWriter writer = new FileWriter(RECORDS_FILE_PATH.toString(), Charset.defaultCharset())) {
            for (String build : latestFiveBuilds) {
                writer.write(build + System.lineSeparator());
            }
        } catch (IOException e) {
            listener.getLogger().println("Exception in BuildCompletionListener : " + e.getMessage());
        }
    }
}
