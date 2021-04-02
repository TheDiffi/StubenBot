package StubenBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import StubenBot.EngeleBengele.EngeleBengele;
import StubenBot.SportTracker.SportTracker;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;

public class CommandDistributer {

    public static final String prefix = Main.prefix;
    public static final String stickerpref = Main.stickerpref;
    public static ArrayList<Message> toDelete = new ArrayList<>();

    public static void handleCommands(MessageCreateEvent event) {
        if (event.getMessage().getContent().isPresent()) {
            deleteOldMessages();

            test();

            if (event.getMessage().getContent().get().startsWith(prefix)) {
                complexEvent(event);
            } else if (event.getMessage().getContent().get().startsWith(stickerpref)) {
                StickersClass.stickerEvent(event);
            } else {
                reaction(event);
            }

        }
    }

    private static void deleteOldMessages() {
        for (Message msgObj : toDelete) {
            msgObj.delete().block();
        }
        toDelete.clear();
    }

    private static void complexEvent(MessageCreateEvent event) {
        var eventChannel = event.getMessage().getChannel().block();
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> messagePieces = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var messagePrefix = messagePieces.remove(0);
        var command = messagePrefix.substring(1);

        // TODO: Save Stube/general & read into main channel
        /*
         * if (eventChannel == null) { eventChannel = mainChannel; }
         */

        if (messagePrefix.equalsIgnoreCase(prefix + "EB")) {
            EngeleBengele.handleCommand(event, command, messagePieces, eventChannel);
        }

        else if (messagePrefix.equalsIgnoreCase(prefix + "SP")) {
            SportTracker.handleCommand(event, command, messagePieces, eventChannel);

        }

        else {
            generalCommands(event, command, messagePieces, eventChannel);
        }

        // found überprüft ob der Command irgentwo gefunden wurde
        /*
         * if (!found) { eventChannel.createEmbed(spec ->
         * spec.setDescription("Command could not be found")).block(); }
         */
    }

    private static void generalCommands(MessageCreateEvent event, String command, List<String> messagePieces,
            MessageChannel eventChannel) {
        switch (command) {
        case "addSticker":
            StickersClass.addStickerEvent(event, messagePieces, eventChannel);
            break;
        case "deleteSticker":
            StickersClass.deleteSticker(event, messagePieces, eventChannel);
            break;
        case "removeSticker":
            StickersClass.deleteSticker(event, messagePieces, eventChannel);
            break;
        case "help":
            eventChannel.createMessage("Coming soon").block();
            //TODO: inplement design from wwbot
            break;
        case "commands":
            eventChannel.createMessage("Not available yet").block();

            break;
        default:

        }

    }

    private static void reaction(MessageCreateEvent event) {
        Memes(event);
    }

    public static void Memes(MessageCreateEvent event) {
        if (event.getMessage().getContent().isPresent()) {
            // this is not implemented beacuse of performance reasons
            // var eventChannel = event.getMessage().getChannel().block();
            var content = event.getMessage().getContent().get();

            if (content.equalsIgnoreCase("f")) {
                event.getMessage().getChannel().block().createEmbed(spec -> {
                    spec.setImage("https://i.imgur.com/9aJeWxK.jpg");

                }).block();

            }

            if (content.equalsIgnoreCase("Hello There")) {
                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/grevious-general-kenobi-star-wars-gif-11406339").block();

            }

            if (content.equalsIgnoreCase("no u")) {
                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/uno-no-u-reverse-card-reflect-glitch-gif-14951171")
                        .block();

            }

            if (content.equalsIgnoreCase("I am speed")) {
                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/racing-speeding-switchinglanes-drivingcrazy-gif-8850377")
                        .block();

            }

            if (content.equalsIgnoreCase("Howdy")) {
                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/aaa-cowboy-aaa-cowboy-music-video-gif-15142935").block();

            }

            if (content.toLowerCase().contains("comerade") || content.toLowerCase().contains("kamerade")) {
                event.getMessage().getChannel().block().createMessage(
                        "https://tenor.com/view/russian-soldiers-russian-soldiers-soviet-russia-dance-gif-10348779")
                        .block();
            }

            if (event.getMessage().getChannelId().asString().equals("788081498136772669")
                    && (content.indexOf("I ") != -1 || content.indexOf("I'") != -1)) {

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

            } else if (event.getMessage().getContent().get().indexOf("idk") != -1) {
                event.getMessage().delete().block();
                event.getMessage().getChannel().block()
                        .createMessage(event.getMember().get().getMention() + " **WE** don't know").block();
            } else if (event.getMessage().getContent().get().indexOf("idc") != -1) {
                event.getMessage().delete().block();
                event.getMessage().getChannel().block()
                        .createMessage(event.getMember().get().getMention() + " **WE** don't care").block();
            }
        }
    }

    private static void test() {

    }

}
