package StubenBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.awt.Color;

import StubenBot.EngeleBengele.EngeleBengele;
import StubenBot.SportTracker.SportTracker;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;

public class CommandDistributer {

    public static final String prefix = Main.prefix;
    public static final String stickerpref = Main.stickerpref;
    public static ArrayList<Message> toDelete = new ArrayList<>();

    private static boolean russianGrammar = false;

    public static void handleCommands(MessageCreateEvent event) {
        if (event.getMessage().getContent().isPresent()) {
            deleteOldMessages();

            test();

            // tests if message starts with prefix
            if (event.getMessage().getContent().get().startsWith(prefix)) {
                complexCommand(event);
            } else if (event.getMessage().getContent().get().startsWith(stickerpref)) {
                StickersClass.stickerEvent(event);
            } else {
                reaction(event);
            }

        }
    }

    // deletes messages whitch have been put in the delete queue
    private static void deleteOldMessages() {
        for (Message msgObj : toDelete) {
            msgObj.delete().block();
        }
        toDelete.clear();
    }

    // overcomplicated, just use quickCommands
    private static void complexCommand(MessageCreateEvent event) {
        var eventChannel = event.getMessage().getChannel().block();
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> messagePieces = new LinkedList<>(Arrays.asList(messageContent.split(" ")));
        var messagePrefix = messagePieces.remove(0);
        var command = messagePrefix.substring(1);

        if (messagePrefix.equalsIgnoreCase(prefix + "EB")) {
            EngeleBengele.handleCommand(event, command, messagePieces, eventChannel);
        }

        else if (messagePrefix.equalsIgnoreCase(prefix + "SP")) {
            SportTracker.handleCommand(event, command, messagePieces, eventChannel);

        }

        else {
            quickCommands(event, command, messagePieces, eventChannel);
        }

        // found überprüft ob der Command irgentwo gefunden wurde
        /*
         * if (!found) { eventChannel.createEmbed(spec ->
         * spec.setDescription("Command could not be found")).block(); }
         */
    }

    private static void quickCommands(MessageCreateEvent event, String command, List<String> messagePieces,
            MessageChannel eventChannel) {
        switch (command) {
            case "ping":
            case "Ping":
                eventChannel.createMessage("Pong!").block();
                break;
            case "help":
            case "commands":
                sendCommandsMessage(event, eventChannel);
                break;

            case "toggleRussianGrammar":
                toggleRussianGrammar(event, eventChannel);
                break;

            case "addSticker":
                StickersClass.addStickerEvent(event, messagePieces, eventChannel);
                break;
            case "deleteSticker":
            case "removeSticker":
                StickersClass.deleteSticker(event, messagePieces, eventChannel);
                break;

            default:

        }

    }

    

    private static void sendCommandsMessage(MessageCreateEvent event, MessageChannel channel) {
        String mssg = " ---- Sticker ---- ";
        mssg += buildCommandDescription(". ", "", "Listet alle Sticker");
        mssg += buildCommandDescription(".", "<stickername>", "Sendet jenen Sticker");

        mssg += " \n---- General ---- ";
        mssg += buildCommandDescription(prefix, "EB commands", "Engele Bengele!");
        mssg += buildCommandDescription(prefix, "SP commands", "Sport Tracker");

        mssg += "\nWenn du a coole Idee für a Funktion fürn bot hosch, feel free es oanem von die Mods weiterzuleiten!";

        Globals.createEmbed(channel, Color.BLACK, "", mssg);
    }

    public static String buildCommandDescription(String pref, String command, String description) {
        return "\n" + "`" + pref + command + " :` " + description;
    }

    private static void toggleRussianGrammar(MessageCreateEvent event, MessageChannel channel) {
        if (event.getMessage().getAuthor().get().getId().asString().equals("317716883077988354")) {
            russianGrammar = !russianGrammar;
            channel.createMessage("`RUSSIAN GRAMMAR: " + russianGrammar + "`").block();
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

            if (russianGrammar || event.getMessage().getChannelId().asString().equals("788081498136772669")) {
                if (content.indexOf("I ") != -1 || content.indexOf("I'") != -1) {
                    russianCorrection(event, content);

                } else if (event.getMessage().getContent().get().indexOf("idk") != -1
                        || event.getMessage().getContent().get().indexOf("Idk") != -1
                        || event.getMessage().getContent().get().indexOf("IDK") != -1) {
                    // event.getMessage().delete().block();
                    event.getMessage().getChannel().block()
                            .createMessage(event.getMember().get().getMention() + " **WE** don't know").block();

                } else if (event.getMessage().getContent().get().indexOf("idc") != -1
                        || event.getMessage().getContent().get().indexOf("Idc") != -1
                        || event.getMessage().getContent().get().indexOf("IDC") != -1) {
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

    private static void test() {

    }

}
