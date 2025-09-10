package io.jenkins.plugins.sample;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundSetter;
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
    private static final String UUID_DEFAULT_DISPLAY_TEXT = "UUID will be generated while saving";
    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;
    private ListEntry[] entries;
    private String latestBuildsFilePath;

    public OnboardingConfiguration() {
        load();
        this.entries = initList();
    }

    private void clearListEntries() {
        this.entries = new ListEntry[0];
    }

    private ListEntry[] initList() {
        return new ListEntry[] {};
    }

    public String getLatestBuildsFilePath() {
        return latestBuildsFilePath;
    }

    @DataBoundSetter
    public void setLatestBuildsFilePath(String latestBuildsFilePath) {
        this.latestBuildsFilePath = latestBuildsFilePath;
        save();
    }

    public void setEntries(ListEntry[] entries) {
        if (entries != null) {
            this.entries = Arrays.stream(entries)
                    .map(listEntry -> {
                        if (UUID_DEFAULT_DISPLAY_TEXT.equalsIgnoreCase(Util.fixEmptyAndTrim(listEntry.getUuid()))) {
                            String uuid = UUID.randomUUID().toString();
                            return new ListEntry(listEntry.getName(), uuid);
                        } else {
                            return listEntry;
                        }
                    })
                    .toArray(ListEntry[]::new);
        }
    }

    public ListEntry[] getEntries() {
        return entries;
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
    private static int httpPostBasicAuth(String url, String username, String password, String data) throws IOException {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + encodedCredentials;
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authHeaderValue);
        if (Util.fixEmptyAndTrim(data) != null) {
            connection.setDoOutput(true);
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postData, 0, postData.length);
        }
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
        clearListEntries();

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
     */
    @POST
    public FormValidation doTestConnection(
            @QueryParameter("url") final String url,
            @QueryParameter("username") final String username,
            @QueryParameter("password") final String password) {
        try {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if (Util.fixEmptyAndTrim(url) == null
                    || Util.fixEmptyAndTrim(username) == null
                    || Util.fixEmptyAndTrim(password) == null) {
                return FormValidation.error("url or username or password must not be null or empty ");
            }
            int statusCode = httpPostBasicAuth(url, username, password, null);
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return FormValidation.error("Server error : " + statusCode);
            } else {
                return FormValidation.ok("Input Validated");
            }
        } catch (IOException iox) {
            return FormValidation.error(iox.getMessage());
        }
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
        return new StandardListBoxModel()
                .includeEmptyValue()
                .includeMatchingAs(
                        ACL.SYSTEM2,
                        Objects.requireNonNull(Jenkins.getInstanceOrNull()),
                        StringCredentials.class,
                        Collections.<DomainRequirement>emptyList(),
                        CredentialsMatchers.always())
                .includeCurrentValue(credentialsId);
    }

    @POST
    public FormValidation doSubmitCredential(@QueryParameter String credentialsId) throws IOException {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (Util.fixEmptyAndTrim(credentialsId) == null) {
            return FormValidation.error("credentialsId must not be null or empty ");
        }
        StandardCredentials credential = lookupCredentials(credentialsId);
        if (credential == null) {
            return FormValidation.error("No credential found with id: " + credentialsId);
        }
        if (credential instanceof StringCredentials stringCredentials) {
            if (stringCredentials.getSecret() == null
                    || stringCredentials.getSecret().getPlainText().isEmpty()) {
                return FormValidation.error("The credential secret must not be null or empty");
            }
            httpPostBasicAuth(
                    getUrl(),
                    getUsername(),
                    getPassword().getPlainText(),
                    stringCredentials.getSecret().getPlainText());
            return FormValidation.ok();
        } else {
            return FormValidation.error("Only StringCredentials are supported");
        }
    }

    public String getBuildLinks() {
        Path latestBuildFilepath;
        if (latestBuildsFilePath == null || latestBuildsFilePath.isEmpty()) {
            latestBuildFilepath = Paths.get(Jenkins.get().getRootDir().getAbsolutePath() + "/latestBuilds.txt");
        } else {
            latestBuildFilepath = Paths.get(latestBuildsFilePath);
        }
        if (!Files.exists(latestBuildFilepath)) {
            return "There are no build records to show.";
        }

        try (Stream<String> lines = Files.lines(latestBuildFilepath)) {
            return lines.map(this::generateBuildLink).collect(Collectors.joining("<br>"));
        } catch (IOException e) {
            return "Error reading build records.";
        }
    }

    private String generateBuildLink(String buildInfo) {
        String[] parts = buildInfo.split(", ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid build info format");
        }
        String jobName = parts[0].split(": ")[1];
        String buildNumber = parts[1].split(": ")[1];
        String url = Jenkins.get().getRootUrl() + "job" + "/" + jobName + "/" + buildNumber + "/";
        return String.format("<a href=\"%s\">%s #%s</a>", url, jobName, buildNumber);
    }

    private static StandardCredentials lookupCredentials(String credentialId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentialsInItem(StandardCredentials.class, null, ACL.SYSTEM2, null),
                CredentialsMatchers.withId(credentialId));
    }

    public String getLatestJobPerCategory() {

        String perCategoryJobNames = Jenkins.get().getRootDir().getAbsolutePath() + "/latestCategoryJobs.txt";
        Path perCategoryJobNamesFilepath = Paths.get(perCategoryJobNames);
        Map<String, String> categoryMap = new HashMap<>();
        StringBuilder jobsPerCategory = new StringBuilder();

        if (Files.exists(perCategoryJobNamesFilepath)) {
            try (Stream<String> lines = Files.lines(perCategoryJobNamesFilepath)) {
                lines.forEach(line -> {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        categoryMap.put(parts[0].trim(), parts[1].trim());
                    }
                });
            } catch (IOException e) {
                return "Error reading category job records.";
            }
        } else {
            return "The file does not exist or cannot be accessed.";
        }

        if (categoryMap.isEmpty()) {
            return "No job names found for any category.";
        } else {
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                jobsPerCategory
                        .append("Category: ")
                        .append(entry.getKey())
                        .append(", Latest Job: ")
                        .append(entry.getValue())
                        .append("<br/>");
            }
        }
        return jobsPerCategory.toString();
    }

    @POST
    public FormValidation doRenameJob(
            @QueryParameter("oldJobName") final String oldJobName,
            @QueryParameter("newJobName") final String newJobName) {
        try {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            String jenkinsBaseUrl = System.getProperty("JENKINS_BASE_URL");
            String username = System.getProperty("JENKINS_USERNAME");
            String apiToken = System.getProperty("JENKINS_API_TOKEN");
            if (Util.fixEmptyAndTrim(jenkinsBaseUrl) == null
                    || Util.fixEmptyAndTrim(apiToken) == null
                    || Util.fixEmptyAndTrim(username) == null
                    || Util.fixEmptyAndTrim(oldJobName) == null
                    || Util.fixEmptyAndTrim(newJobName) == null) {
                return FormValidation.error("oldJobName or newJobName must not be null or empty ");
            }
            String queryString = URLEncoder.encode(newJobName.trim(), StandardCharsets.UTF_8);
            String renameUrl =
                    String.format("%s/job/%s/doRename?newName=%s", jenkinsBaseUrl, oldJobName.trim(), queryString);
            int statusCode = httpPostBasicAuth(renameUrl, username, apiToken, null);
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return FormValidation.error("Server error : " + statusCode);
            } else {
                renameJobInFile(oldJobName, newJobName);
                return FormValidation.ok("Job Renamed Successfully");
            }
        } catch (IOException iox) {
            return FormValidation.error(iox.getMessage());
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
