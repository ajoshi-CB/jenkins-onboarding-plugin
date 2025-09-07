package io.jenkins.plugins.sample;

import hudson.model.InvisibleAction;

public class BuildCategoryAction extends InvisibleAction {

    private final String category;

    public BuildCategoryAction(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
