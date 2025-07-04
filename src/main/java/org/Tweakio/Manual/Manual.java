package org.Tweakio.Manual;

public class Manual {

    /**
     * Returns the help text for a given command name.
     * @param name The command name (e.g., "ai", "pause_on").
     * @return The manual/help string for that command, or a default message if not found.
     */
    public String ManualHandler(String name) {
        return switch (name.toLowerCase()) {
            case "pause_on"     -> pause_on();
            case "pause_off"    -> pause_off();
            case "pause_show"   -> pause_show();
            case "showq"        -> showq();
            case "help"         -> help();
            case "setmaxchat"   -> setmaxchat();
            case "showmaxchat"  -> showmaxchat();
            case "setgc"        -> setgc();
            case "showgc"       -> showgc();
            case "github"       -> github();
            case "gpt"          -> gpt();
            case "ai"           -> ai();
            case "personalai"   -> personalai();
            case "img_url"      -> img_url();
            case "imgd"         -> imgd();
            case "google"       -> google();
            case "yts"          -> yts();
            case "ytd"          -> ytd();
//            case "send"         -> send(); // [ Testing purposes Not for Public usage]
            case "setq"         -> setq();
            case "manual"       -> manual();
            default               -> "[Error] No manual entry found for '" + name + "'. Use 'help' to list all commands.";
        };
    }

    public String pause_on() {
        return """
            [Admin Command: pause_on]
            Pause the bot temporarily; only admin commands work.
            - Syntax: pause_on
            - To resume: pause_off
            """;
    }

    public String pause_off() {
        return """
            [Admin Command: pause_off]
            Resume the bot if paused.
            - Syntax: pause_off
            - To pause again: pause_on
            """;
    }

    public String pause_show() {
        return """
            [Admin Command: pause_show]
            Check if the bot is paused.
            - Syntax: pause_show
            """;
    }

    public String showq() {
        return """
            [Admin Command: showq]
            Display the current command prefix (quantifier).
            - Syntax: showq
            """;
    }

    public String help() {
        return """
            [Admin Command: help]
            Show the main help menu.
            - Syntax: ...help
            """;
    }



    public String setmaxchat() {
        return """
            [Bot Command: setmaxchat]
            Set how many recent chats to consider for context.
            - Syntax: // setmaxchat [number]
            - Example: // setmaxchat 5
            """;
    }

    public String showmaxchat() {
        return """
            [Bot Command: showmaxchat]
            View the current max chat history setting.
            - Syntax: // showmaxchat
            """;
    }

    public String setgc() {
        return """
            [Bot Command: setgc]
            Enable or disable global access for all users.
            - Syntax: // setgc on | // setgc off
            """;
    }

    public String showgc() {
        return """
            [Bot Command: showgc]
            Display current global access status.
            - Syntax: // showgc
            """;
    }

    public String github() {
        return """
            [Bot Command: github]
            Perform an auto commit to the linked GitHub repo.
            - Syntax: // github
            """;
    }

    public String gpt() {
        return """
            [Bot Command: gpt]
            Chat with the GPT model.
            - Syntax: // gpt [message]
            - Example: // gpt How are you?
            """;
    }

    public String ai() {
        return """
            [Bot Command: ai]
            General AI chatbot.
            - Syntax: // ai [message]
            """;
    }

    public String personalai() {
        return """
            [Bot Command: personalai]
            Private conversation with AI.
            - Syntax: // personalai [message]
            """;
    }

    public String img_url() {
        return """
            [Bot Command: img_url]
            Generate an AI image from text prompt.
            - Syntax: img_url [prompt]
            - Example: // img_url Sunset over mountains
            """;
    }

    public String imgd() {
        return """
            [Bot Command: imgd]
            Download a previously generated image by its alias.
            - Syntax: imgd [alias]
            - Example: // imgd sunset123
            """;
    }

    public String google() {
        return """
            [Bot Command: google]
            Search Google for information.
            - Syntax: // google [query]
            - Example: // google Java tutorials
            """;
    }

    public String yts() {
        return """
            [Bot Command: yts]
            Search YouTube for videos.
            - Syntax: // yts [query]
            - Example: // yts relaxing music
            """;
    }

    public String ytd() {
        return """
            [Bot Command: ytd]
            Download YouTube video as MP3.
            - Syntax: // ytd [YouTube URL]
            - Example: // ytd https://youtu.be/abc123
            """;
    }

    public String send() {
        return """
            [Bot Command: send]
            Send a direct message to any number.
            - Syntax: send [number] [message]
            - Example: // send 1234567890 Hello!
            """;
    }

    public String setq() {
        return """
            [Bot Command: setq]
            Change the command prefix (quantifier).
            - Syntax: // setq [newPrefix]
            - Example: // setq !
            """;
    }

    public String manual() {
        return """
            [Bot Command: manual]
            Show details for a specific command.
            - Syntax: // manual [commandName]
            - Example: // manual img_url
            """;
    }
}
