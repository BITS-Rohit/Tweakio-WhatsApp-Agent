package org.Tweakio.UserSettings;

import io.github.cdimascio.dotenv.Dotenv;

public final class user {
    private static final String profileName =
            System.getenv("PROFILE") != null ? System.getenv("PROFILE") : "dev";

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(profileName + ".env")
            .directory("ENV")
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private static String get(String key) {
        String sysEnv = System.getenv(key);
        return sysEnv != null ? sysEnv : dotenv.get(key);
    }

    public static final String PROFILE         = get("PROFILE");
    public static final String BOT_NAME        = get("BOT_NAME");
    public static final String BOT_NUMBER      = get("BOT_NUMBER");
    public static final String ADMIN_NUMBER    = get("ADMIN_NUMBER");
    public static final String ADMIN_NAME      = get("ADMIN_NAME");

    public static final String GH_TOKEN        = get("GH_TOKEN");
    public static final String REPO_NAME       = get("REPO_NAME");
    public static final String BRANCH_NAME     = get("BRANCH_NAME");

    public static final String YOUTUBE_API_KEY = get("YOUTUBE_API_KEY");
    public static final String GOOGLE_API_KEY  = get("GOOGLE_API_KEY");
    public static final String GROQ_API_KEY    = get("GROQ_API_KEY");
    public static final String GEMINI_API_KEY  = get("GEMINI_API_KEY");

    public static final String CSE_ID          = get("CSE_ID");

    public static final String AGENT_AI_KEY    = get("AGENT_AI_KEY");
    public static final String QUANTIFIER      = get("QUANTIFIER");
    public static final String AGENT_ID        = get("AGENT_ID");
    public static final String WEBHOOK_ID      = get("WEBHOOK_ID");
    public static final String BASE_URL        = get("BASE_URL");
    public static final String INTRO_IMG_URL   = get("INTRO_IMG_URL");

    private user() {
        // prevent instantiation
    }
}
