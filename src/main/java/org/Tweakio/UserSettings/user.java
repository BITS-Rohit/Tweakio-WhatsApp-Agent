package org.Tweakio.UserSettings;

import io.github.cdimascio.dotenv.Dotenv;

public final class user {
    private static final String profileName = System.getenv("PROFILE") != null
            ? System.getenv("PROFILE")
            : "dev";

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(profileName + ".env")
            .directory("ENV")
            .load();

    public static final String PROFILE         = dotenv.get("PROFILE");
    public static final String BOT_NAME        = dotenv.get("BOT_NAME");
    public static final String BOT_NUMBER      = dotenv.get("BOT_NUMBER");
    public static final String ADMIN_NUMBER    = dotenv.get("ADMIN_NUMBER");
    public static final String ADMIN_NAME      = dotenv.get("ADMIN_NAME");

    public static final String GH_TOKEN        = dotenv.get("GH_TOKEN");
    public static final String REPO_NAME       = dotenv.get("REPO_NAME");
    public static final String BRANCH_NAME     = dotenv.get("BRANCH_NAME");

    public static final String YOUTUBE_API_KEY = dotenv.get("YOUTUBE_API_KEY");
    public static final String GOOGLE_API_KEY  = dotenv.get("GOOGLE_API_KEY");
    public static final String GROQ_API_KEY    = dotenv.get("GROQ_API_KEY");
    public static final String GEMINI_API_KEY  = dotenv.get("GEMINI_API_KEY");

    public static final String CSE_ID          = dotenv.get("CSE_ID");

    public static final String AGENT_AI_KEY    = dotenv.get("AGENT_AI_KEY");
    public static final String QUANTIFIER      = dotenv.get("QUANTIFIER");
    public static final String AGENT_ID        = dotenv.get("AGENT_ID");
    public static final String WEBHOOK_ID      = dotenv.get("WEBHOOK_ID");
    public static final String BASE_URL        = dotenv.get("BASE_URL");
    public static final String INTRO_IMG_URL   = dotenv.get("INTRO_IMG_URL");

    private user() {
        // prevent instantiation
    }
}
