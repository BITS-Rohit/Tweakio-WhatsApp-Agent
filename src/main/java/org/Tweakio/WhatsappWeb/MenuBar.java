package org.Tweakio.WhatsappWeb;

import org.Tweakio.UserSettings.user;

public class MenuBar {
    user u ;
    public MenuBar(){
    }
    __ d = new __();
    String AdminName = d.a_name;
    String AdminNumber = d.a_num;

    public String Menu() {
        return """
        🌟 *Welcome to Your Smart Assistant — Tweakio!* 🌟
        ───────────────────────────────────────
        👑 Admin Name   :\s""" + AdminName + "\n" +
                "📞 Admin Number : " + AdminNumber + "\n" +
                "🤖 Bot Owner    : " + user.BOT_NAME + "\n" +
                "📲 Bot Number   : " + user.BOT_NUMBER + "\n\n" +
                """
                ───────────────────────────────────────
                🧾 *Usage*
                _Format:_ `Quantifier ➜ Command ➜ Input`
                _Example:_ `// ai ➜ Who is Elon Musk?`
       \s
                ───────────────────────────────────────
                💬 *AI & Chat* \s
                • `ai`          → General AI chat \s
                • `personalai`  → Private, personal chat \s
                • `gpt`         → GPT‑powered chat \s
       \s
                🖥 *Agent Control* \s
                • `help`        → Show this menu \s
                • `manual`      → Detailed command guide
       \s
                ───────────────────────────────────────
                🛠️ *Admin Commands* \s
                • `showq`       → Show current prefix \s
                • `setq`        → Set a new prefix \s
                • `pause_on`    → Pause the bot \s
                • `pause_off`   → Resume the bot \s
                • `pause_show`  → Check pause status \s
                • `h_restart`   → Hard restart (admin only)
       \s
                ───────────────────────────────────────
                🔍 *Search & Media* \s
                • `google`      → Google search \s
                • `yts`         → YouTube search \s
                • `ytd`         → Download YouTube MP3 \s
                • `img_url`     → Generate AI image \s
                • `imgd`        → Download generated image \s
       \s
                👨‍🎓 *Dev & Student* \s
                • `github`      → Auto GitHub commit \s
                • `setmaxchat`  → Limit chat history scan \s
                • `showgc`      → Show group‑wide mode \s
                • `setgc`       → Toggle group‑wide mode \s
       \s
               \s
                ───────────────────────────────────────
                💡 *Pro Tips* \s
                – Use **clear, concise** inputs for speed \s
                – Wait **3–5 sec** between commands to avoid rate limits \s
       \s
                🛡️ *Safety* \s
                Slow response = account protection in action \s
       \s
                🤖 *Tweakio is always here to help!* 🤖
               \s""";
    }


}
