package com.test;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("github-activity ");

        Scanner sc = new Scanner(System.in);
        String username = sc.nextLine();

        if (username.isEmpty()) {
            System.out.println("Usage: java github-activity <username>");
            System.exit(1);
        }

        String apiUrl = "https://api.github.com/users/" + username + "/events";

        try{
            String res = fetchGithubActivity(apiUrl);
            displayActivity(res);
        } catch (Exception e) {
            System.err.println("Error fetching GitHub activity: " + e.getMessage());
            System.exit(1);
        }

    }

    private static String fetchGithubActivity(String apiUrl)
            throws IOException {

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch data: HTTP error code " + responseCode);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();
        return response.toString();
    }

    private static void displayActivity(String response) {
        JSONArray activity = new JSONArray(response);

        if (activity.isEmpty()) {
            System.out.println("Not Found User!");
            return;
        }

        for (int i = 0; i < activity.length(); i++) {
            JSONObject event = activity.getJSONObject(i);
            String eventType = event.getString("type");
            String repoName = event.getJSONObject("repo").getString("name");

            switch (eventType) {
                case "PushEvent":
                    int commitCount = event.getJSONObject("payload").getJSONArray("commits").length();
                    System.out.println("- Pushed " + commitCount + " commits to " + repoName);
                    break;
                case "IssuesEvent":
                    String action = event.getJSONObject("payload").getString("action");
                    System.out.println("- " + action.substring(0, 1).toUpperCase() + action.substring(1) + " an issue in " + repoName);
                    break;
                case "WatchEvent":
                    System.out.println("- Starred " + repoName);
                    break;
                case "PullRequestEvent":
                    String prAction = event.getJSONObject("payload").getString("action");
                    System.out.println("- " + prAction.substring(0, 1).toUpperCase() + prAction.substring(1) + " a pull request in " + repoName);
                    break;
                default:
                    System.out.println("- Performed " + eventType + " in " + repoName);
                    break;
            }
        }
    }
}