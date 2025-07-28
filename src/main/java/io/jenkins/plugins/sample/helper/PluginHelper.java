package io.jenkins.plugins.sample.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isInvalidField(String field, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }
}
