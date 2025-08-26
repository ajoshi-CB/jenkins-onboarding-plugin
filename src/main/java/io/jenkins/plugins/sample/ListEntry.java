package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.util.UUID;
import org.kohsuke.stapler.DataBoundConstructor;

public class ListEntry extends AbstractDescribableImpl<ListEntry> {

    private final String name;
    private final UUID uuid;

    @DataBoundConstructor
    public ListEntry(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ListEntry> {
        @Override
        public String getDisplayName() {
            return "Simple Entry";
        }
    }
}
