package io.jenkins.plugins.sample;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class PlainTextCredentials extends BaseStandardCredentials implements StringCredentials {

    private final String secret;

    public PlainTextCredentials(String id, String description, String secret) {
        super(id, description);
        this.secret = secret;
    }

    @NonNull
    @Override
    public Secret getSecret() {
        return Secret.fromString(secret);
    }
}
