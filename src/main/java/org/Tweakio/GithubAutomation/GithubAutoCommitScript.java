package org.Tweakio.GithubAutomation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.Tweakio.UserSettings.user;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Arrays;

public class GithubAutoCommitScript {
//    public static void main(String[] args) {
//        GithubAutoCommitScript g = new GithubAutoCommitScript();
//        System.out.println(g.commit());
//    }

    static user u = new user();
    private static final String GITHUB_TOKEN = u.Gh_Token;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String repoUrl = u.reponame;
    private static final String branchname = u.branchName;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final boolean debug = true;

    public String commit() {
        try {
            if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
                System.err.println("❌ Please set the GH_TOKEN environment variable.");
                System.exit(1);
            }
            RepoInfo info = RepoInfo.parse();
            return "Parsed repo -> Owner: " + info.owner + ", Repo: " + info.repo + ", Branch: " + info.branch + ", Path: " + info.path + "\n"
                    + run(info, info.path.isEmpty() ? "daily-update.txt" : info.path);
        } catch (IOException | IllegalArgumentException e) {
            return "❌ Failed: " + e.getMessage();
        }
    }

    String run(RepoInfo info, String filePath) throws IOException {
        String existingSha = fetchFileSha(info, filePath);
        String content = "Automated daily update: " + LocalDate.now() + "\n";
        String base64 = Base64.getEncoder().encodeToString(content.getBytes());

        JsonObject payload = new JsonObject();
        payload.addProperty("message", "chore: daily update " + LocalDate.now());
        payload.addProperty("content", base64);
        payload.addProperty("branch", info.branch); // optional but good practice

        if (existingSha != null) {
            payload.addProperty("sha", existingSha); // ✅ correct key
        }

        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + info.owner + "/" + info.repo + "/contents/" + filePath)
                .header("Authorization", "token " + GITHUB_TOKEN)
                .header("Accept", "application/vnd.github.v3+json")
                .put(RequestBody.create(gson.toJson(payload), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return "✅ Commit successful: " + response.code() + "\n" + "Url : " + repoUrl;
            } else {
                assert response.body() != null;
                return "❌ Commit failed: HTTP " + response.code() + " → " + response.body().string();
            }
        }
    }

    private String fetchFileSha(RepoInfo info, String filePath) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + info.owner + "/" + info.repo + "/contents/" + filePath)
                .header("Authorization", "token " + GITHUB_TOKEN)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                return json.has("sha") ? json.get("sha").getAsString() : null;
            } else if (response.code() == 404) {
                return null; // file doesn't exist
            } else {
                System.err.println("⚠️ Unable to fetch SHA: HTTP " + response.code() +
                        " → " + (response.body() != null ? response.body().string() : "no body"));
                return null;
            }
        }
    }

    public static class RepoInfo {
        final String owner;
        final String repo;
        final String branch;
        final String path;

        private RepoInfo(String owner, String repo, String branch, String path) {
            this.owner = owner;
            this.repo = repo;
            this.branch = branch;
            this.path = path;
        }

        static RepoInfo parse() {
            try {
                URI uri = new URI(GithubAutoCommitScript.repoUrl);
                String host = uri.getHost();
                if (!"github.com".equalsIgnoreCase(host)) {
                    throw new IllegalArgumentException("Not a github.com URL");
                }
                String[] parts = uri.getPath().split("/");
                if (parts.length < 3) {
                    throw new IllegalArgumentException("URL path too short: " + uri.getPath());
                }
                String owner = parts[1];
                String repo = parts[2].endsWith(".git")
                        ? parts[2].substring(0, parts[2].length() - 4)
                        : parts[2];
                String branch = branchname;
                String path = "";
                if (parts.length >= 5 && ("tree".equals(parts[3]) || "blob".equals(parts[3]))) {
                    branch = parts[4];
                    if (parts.length > 5) {
                        path = String.join("/", Arrays.copyOfRange(parts, 5, parts.length));
                    }
                }
                return new RepoInfo(owner, repo, branch, path);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL syntax: " + GithubAutoCommitScript.repoUrl, e);
            }
        }
    }
}
