package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.ElementHandle;
import org.Tweakio.UserSettings.user;

public class MenuBar {
    user u ;
    public MenuBar(){
    }
    __ d = new __();
    String AdminName = d.a_name;
    String AdminNumber = d.a_num;

    public String menu(ElementHandle chat, ElementHandle target, String t, String s, String botNumber) {

        return """
                🌟 *Welcome to Your Smart Assistant! Tweakio!!!* 🌟
                _______________
                Admin Name  : """ + AdminName + "\n" +
                "Admin Number: " + AdminNumber + "\n \n" +
                "Bot Owner  : " + user.botName + "\n" +
                "Bot Number : " + botNumber + "\n" +

                """
                        _______________
                        _Your all-in-one intelligent companion!_
                        
                        ━━━━━━━━━━━━━━━━━━━━
                        🧾 *How to Use Commands:*
                        ➤ *Format:* `Quantifier ➜ Command ➜ Input`
                        ➤ _Example:_ `// ai ➜ Who is Elon Musk?`
                        
                        ━━━━━━━━━━━━━━━━━━━━
                        💬 *AI & Chat Tools*
                        ┌─ 🧠 `ai ➜ your question`
                        ├─ 👤 `personalai ➜ your private chat`
                        └─ ❓ `help ➜ Show this menu again`
                        
                        🎨 *Image Tools (New!)*
                        ┌─ 🖼️ `img_url ➜ <your image prompt>` → _Generate AI image + get shareable link_
                        └─ 📥 `imgd ➜ <alias>` → _Download image by alias you gave to img_url_
                        
                        🔍 *Search & Download*
                        ┌─ 🔎 `google ➜ your search query`
                        ├─ 🎥 `yts ➜ search YouTube`
                        └─ 📥 `ytd ➜ YouTube link to download`
                        
                        👨‍🎓 *Student & GitHub Tools*
                        └─ 🔧 `github ➜ Perform daily commit to the linked repo`
                        
                        🛠️ *Group & Bot Management*
                        ┌─ 📄 `showq ➜ Display current quantifier`
                        ├─ ⏸️ `pause_on ➜ Pause the bot`
                        ├─ 🔍 `pause_show ➜ Show pause status`
                        ├─ ▶️ `pause_off ➜ Resume the bot`
                        ├─ 📝 `setgc ➜ Enable group-wide commands`
                        ├─ 📋 `showgc ➜ Check group command status`
                        ├─ 📌 `setq ➜ Set your custom quantifier`
                        ├─ ⚙️ `setmaxchat ➜ Set max chats to scan`
                        └─ 📊 `showmaxchat ➜ View current scan count`
                        
                        ✉️ *Messaging Utility*
                        └─ 💌 `send ➜ <number> <your message>` (DM any number)
                        
                        ━━━━━━━━━━━━━━━━━━━━
                        🔔 *Need Help?*
                        ➤ Type `Quantifier ➜ showmenu` or just `...help`
                        
                        💡 *Pro Tips:*
                        ✔️ Use short, clear inputs for faster replies.
                        ✔️ After a reply, wait 3–5 seconds to avoid spam triggers.
                        
                        🛡️ *Safety Notice:*
                        _If the Agent takes time to respond, it's protecting your account from being flagged!_
                        
                        🤖 *Always here to assist you!* 🤖
                        """;
    }

}
