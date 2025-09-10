package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import jenkins.model.Jenkins;

@Extension
public class JobRenameListener extends ItemListener {

    @Override
    public void onRenamed(Item item, String oldName, String newName) {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            if (item instanceof Job) {
                try {
                    ((Job<?, ?>) item).renameTo(newName);
                    renameJobInFile(oldName, newName);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to rename job: " + e.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("Jenkins instance is not available");
        }
    }

    private synchronized void renameJobInFile(String oldJobName, String newJobname) {

        String latestJobPerCategory = Jenkins.get().getRootDir().getAbsolutePath() + "/latestCategoryJobs.txt";
        Path categoryFilePath = Paths.get(latestJobPerCategory);

        Map<String, String> categoryMap = new HashMap<>();

        if (Files.exists(categoryFilePath)) {
            try (Stream<String> lines = Files.lines(categoryFilePath)) {
                lines.forEach(line -> {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        if (parts[1].trim().equals(oldJobName)) {
                            categoryMap.put(parts[0].trim(), newJobname);
                        } else {
                            categoryMap.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Exception in renameJobInFile : " + e.getMessage());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(categoryFilePath)) {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                writer.write(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception in renameJobInFile : " + e.getMessage());
        }
    }
}
