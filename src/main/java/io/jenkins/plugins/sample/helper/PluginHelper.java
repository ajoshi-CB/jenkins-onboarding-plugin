package io.jenkins.plugins.sample.helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class having static method for common functions which will be use across plugin
 */
public class PluginHelper {

    public static int httpPostBasicAuth(String url, String username, String password)
            throws IOException, InterruptedException {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + encodedCredentials;
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authHeaderValue);
        connection.setDoOutput(true);
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
    public static boolean isMatch(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
