package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.verb.POST;

/**
 *  The class representing Onboarding Plugin section on the System configuration page
 */
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

    /**
     * This method makes Http Post Request to the URL passed a san argument with base 64 encoded username and password values
     * @param url
     *       url to make http post request to
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private static int httpPostBasicAuth(String url, String username, String password) throws IOException {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + encodedCredentials;
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authHeaderValue);
        return connection.getResponseCode();
    }

    /**
     * Checks if the value provided matches the regex pattern provided
     * @param value
     *        the value to check for against teh regex
     * @param regex
     *        the regex against which to validate value
     * @return
     *       boolean true if value matches regex
     *               false if value doesn't match the regex
     */
    private static boolean isMatch(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
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

    /**
     * A field validator for username field on the optional view on the Onboarding Plugin Section
     * @param username
     *       username field username to validate
     * @return
     *       FormValidation object
     */
    public FormValidation doCheckUsername(@QueryParameter String username) {
        if (username.isEmpty()) {
            return FormValidation.error("Please enter your Username");
        } else {
            if (!isMatch(username, USERNAME_REGEX_PATTERN)) {
                return FormValidation.error("Invalid Username format");
            }
            return FormValidation.ok();
        }
    }

    /**
     * A field validator for name field (plugin name) on the optional view on the Onboarding Plugin Section
     * @param name
     *        name field name (plugin name) to validate
     * @return
     *       FormValidation object
     */
    public FormValidation doCheckName(@QueryParameter String name) {
        if (name.isEmpty()) {
            return FormValidation.error("Please enter your name");
        } else {
            if (!isMatch(name, PLUGIN_NAME_REGEX_PATTERN)) {
                return FormValidation.error("Invalid name format");
            }
            return FormValidation.ok();
        }
    }

    /**
     * This method validates the name and username parameters based on validation regexes
     * {@link #PLUGIN_NAME_REGEX_PATTERN} and {@link #USERNAME_REGEX_PATTERN}
     * It just overrides the {@link jenkins.model.GlobalConfiguration#configure(StaplerRequest2, JSONObject)}} method
     * to validate name (plugin name) and username fields when the Save or Apply Button is clicked on System Configuration page
     * @param req
     *      The stapler request object
     * @param json
     *      The JSON object that captures the configuration data for this {@link hudson.model.Descriptor}.
     *      See <a href="https://www.jenkins.io/doc/developer/forms/structured-form-submission/">the developer documentation</a>.
     * @return boolean If validation for the fields (name and username) is successfull, return true else return false
     * @throws FormException
     */
    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
        String pluginName = json.getString("name");
        String userName = json.getString("username");

        if (pluginName != null || userName != null) {
            if (!isMatch(pluginName, PLUGIN_NAME_REGEX_PATTERN)) {
                return false;
            }
            if (!isMatch(userName, USERNAME_REGEX_PATTERN)) {
                return false;
            }
            req.bindJSON(this, json);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method validates the url , username and password fields in the optional view on the Onboarding Plugin Section
     * @param url
     *      url to send username and password to
     * @param username
     *      username field
     * @param password
     *      password field
     * @return
     *      FormValidation object
     * @throws IOException
     * @throws ServletException
     */
    @POST
    public FormValidation doTestConnection(
            @QueryParameter("url") final String url,
            @QueryParameter("username") final String username,
            @QueryParameter("password") final String password)
            throws IOException, ServletException {
        try {
            if (Util.fixEmptyAndTrim(url) == null
                    || Util.fixEmptyAndTrim(username) == null
                    || Util.fixEmptyAndTrim(password) == null) {
                return FormValidation.error("url or username or password must not be null or empty ");
            }
            int statusCode = httpPostBasicAuth(url, username, password);
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return FormValidation.error("Server error : " + statusCode);
            } else {
                return FormValidation.ok("Input Validated");
            }
        } catch (IOException iox) {
            return FormValidation.error("Server Error Occured with status code 500 ");
        }
    }
}
