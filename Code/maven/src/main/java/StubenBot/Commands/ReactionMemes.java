package StubenBot.Commands;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class ReactionMemes {

    public static boolean reactionEvent(String content, MessageCreateEvent event) {
        for (ReactionMeme meme : reactionMemes) {
            if (meme.check(content)) {
                meme.send(event.getMessage().getChannel().block());
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ReactionMeme> reactionMemes = new ArrayList<>() {
        {
            add(new ReactionMeme("f", "https://i.imgur.com/9aJeWxK.jpg", matchIgnoreCase(), sendEmbed()));
            add(new ReactionMeme("hello there", "https://tenor.com/view/grevious-general-kenobi-star-wars-gif-11406339",
                    matchIgnoreCase(), sendEmbed()));
            add(new ReactionMeme("i am speed",
                    "https://tenor.com/view/racing-speeding-switchinglanes-drivingcrazy-gif-8850377", matchIgnoreCase(),
                    sendEmbed()));
            add(new ReactionMeme("no u", "https://tenor.com/view/uno-no-u-reverse-card-reflect-glitch-gif-14951171",
                    matchIgnoreCase(), sendEmbed()));
            add(new ReactionMeme("howdy", "https://tenor.com/view/aaa-cowboy-aaa-cowboy-music-video-gif-15142935",
                    matchIgnoreCase(), sendEmbed()));
            add(new ReactionMeme("comerade",
                    "https://tenor.com/view/russian-soldiers-russian-soldiers-soviet-russia-dance-gif-10348779",
                    matchIgnoreCase(), sendEmbed()));
            add(new ReactionMeme("kamerade",
                    "https://tenor.com/view/russian-soldiers-russian-soldiers-soviet-russia-dance-gif-10348779",
                    matchIgnoreCase(), sendEmbed()));

        }
    };

    private static React<String, String, Boolean> matchIgnoreCase() {
        return (content, id) -> content.equalsIgnoreCase(id);
    }

    private static React<String, String, Boolean> containsIgnoreCase() {
        return (content, id) -> content.toLowerCase().contains(id.toLowerCase());
    }

    private static React<MessageChannel, String, Void> sendEmbed() {
        return (channel, url) -> {
            channel.createEmbed(spec -> {
                spec.setImage(url);
            }).block();
            return null;
        };
    }

    private static React<MessageChannel, String, Void> sendMsg() {
        return (channel, url) -> {
            channel.createMessage(url)
                    .block();
            return null;
        };
    }
}

class ReactionMeme {
    String url;
    String identifier;
    private React<String, String, Boolean> check;
    private React<MessageChannel, String, Void> send;

    ReactionMeme(String identifier, String url, React<String, String, Boolean> check,
            React<MessageChannel, String, Void> send) {
        this.url = url;
        this.identifier = identifier;
        this.check = check;
        this.send = send;
    }

    boolean check(String content) {
        return check.run(content, this.identifier);
    }

    void send(MessageChannel channel) {
        send.run(channel, this.url);
    }

}

interface React<X, Y, R> {
    R run(X t, Y r);
}
