package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class ListEntry extends AbstractDescribableImpl<ListEntry> {

    private String name;
    private String uuid;

    @DataBoundConstructor
    public ListEntry(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
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
