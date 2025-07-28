package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.sample.util.PluginHelper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.HttpURLConnection;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.verb.POST;

@Extension
public class OnboardingConfiguration extends GlobalConfiguration {

    private static final String PLUGIN_NAME_REGEX_PATTERN = "^[a-zA-Z ]*$";
    private static final String USERNAME_REGEX_PATTERN = "^[a-zA-Z]*$";
    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;

    public OnboardingConfiguration() {
        load();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        save();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        save();
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(Secret password) {
        this.password = password;
        save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        save();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public FormValidation doCheckUsername(@QueryParameter String value) {
        if (value.isEmpty()) {
            return FormValidation.error("Please enter your Username");
        } else {
            if (PluginHelper.isInvalidField(value, USERNAME_REGEX_PATTERN)) {
                return FormValidation.error("Invalid Username format");
            }
            return FormValidation.ok();
        }
    }

    public FormValidation doCheckName(@QueryParameter String value) {
        if (value.isEmpty()) {
            return FormValidation.error("Please enter your name");
        } else {
            if (PluginHelper.isInvalidField(value, PLUGIN_NAME_REGEX_PATTERN)) {
                return FormValidation.error("Invalid name format");
            }
            return FormValidation.ok();
        }
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
        String pluginName = json.getString("name");
        String userName = json.getString("username");

        if (pluginName != null || userName != null) {
            if (PluginHelper.isInvalidField(pluginName, PLUGIN_NAME_REGEX_PATTERN)) {
                return false;
            }
            if (PluginHelper.isInvalidField(userName, PLUGIN_NAME_REGEX_PATTERN)) {
                return false;
            }
            req.bindJSON(this, json);
            return true;
        } else {
            return false;
        }
    }

    @POST
    public FormValidation doTestConnection(
            @QueryParameter("url") final String url,
            @QueryParameter("username") final String username,
            @QueryParameter("password") final String password)
            throws IOException, ServletException {
        try {
            if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
                return FormValidation.error("Mandatory parameters cannot be null or empty");
            }
            int statusCode = PluginHelper.httpPostBasicAuth(url, username, password);
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return FormValidation.error("Sever error : " + statusCode);
            } else {
                return FormValidation.ok("Input Validated");
            }
        } catch (IOException | InterruptedException e) {
            return FormValidation.error("Server Error Occured with status code 500 ");
        }
    }
}
