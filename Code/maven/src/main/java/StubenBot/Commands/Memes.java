package StubenBot.Commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class Memes {
    private static boolean russianGrammar = false;


    public static void handleMemes(MessageCreateEvent event) {

        if (event.getMessage().getContent() != null) {
            // this is not implemented beacuse of performance reasons
            // var eventChannel = event.getMessage().getChannel().block();
            var content = event.getMessage().getContent();

            if (ReactionMemes.reactionEvent(content, event))
                return;

            if (russianGrammar || event.getMessage().getChannelId().asString().equals("788081498136772669")) {
                if (content.indexOf("I ") != -1 || content.indexOf("I'") != -1) {
                    russianCorrection(event, content);

                } else if (event.getMessage().getContent().indexOf("idk") != -1
                        || event.getMessage().getContent().indexOf("Idk") != -1
                        || event.getMessage().getContent().indexOf("IDK") != -1) {
                    // event.getMessage().delete().block();
                    event.getMessage().getChannel().block()
                            .createMessage(event.getMember().get().getMention() + " **WE** don't know").block();

                } else if (event.getMessage().getContent().indexOf("idc") != -1
                        || event.getMessage().getContent().indexOf("Idc") != -1
                        || event.getMessage().getContent().indexOf("IDC") != -1) {
                    // event.getMessage().delete().block();
                    event.getMessage().getChannel().block()
                            .createMessage(event.getMember().get().getMention() + " **WE** don't care").block();

                }
            }

        }
    }


    private static void russianCorrection(MessageCreateEvent event, String content) {
        event.getMessage().getChannel().block().createMessage(spec -> {
            var mssg = "> ..." + content.substring(content.indexOf("I") / 2);
            var youmeanttosay = content.substring(content.indexOf("I"), content.length());
            while (youmeanttosay.indexOf("me") != -1 || youmeanttosay.indexOf("mine") != -1
                    || youmeanttosay.indexOf("my") != -1 || youmeanttosay.indexOf("I") != -1
                    || youmeanttosay.indexOf("am") != -1) {
                if (youmeanttosay.indexOf("me") != -1) {
                    youmeanttosay = youmeanttosay.replaceAll("me", "us");
                }
                if (youmeanttosay.indexOf("mine") != -1) {
                    youmeanttosay = youmeanttosay.replaceAll("mine", "ours");
                }
                if (youmeanttosay.indexOf("my") != -1) {
                    youmeanttosay = youmeanttosay.replaceAll("my", "our");
                }
                if (youmeanttosay.indexOf("I") != -1) {
                    youmeanttosay = youmeanttosay.replace("I", "WE");
                }
                if (youmeanttosay.indexOf("i") != -1) {
                    youmeanttosay = youmeanttosay.replace(" i ", " we ");
                }
                if (youmeanttosay.indexOf("am") != -1) {
                    youmeanttosay = youmeanttosay.replace("am", "are");
                }
                if (youmeanttosay.indexOf("'m") != -1) {
                    youmeanttosay = youmeanttosay.replace("'m", "'re");
                }
                if (youmeanttosay.indexOf("bin") != -1) {
                    youmeanttosay = youmeanttosay.replace("bin", "are");
                }
            }
            mssg += "\nI think you meant to say: " + youmeanttosay;

            spec.setContent(mssg);
            /*
             * spec.setEmbed(b -> { b.setImage("https://i.imgur.com/8doX74q.jpg");
             * 
             * });
             */
        }).block();
    }

    public static void toggleRussianGrammar() {
        russianGrammar = !russianGrammar;
    }

    public static String printRussGrammar() {
        return russianGrammar ? "ON" : "OFF";
    }

}


