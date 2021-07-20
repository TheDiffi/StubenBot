package StubenBot;

import java.util.ArrayList;

import StubenBot.EngeleBengele.EngeleBengele;
import StubenBot.SportTracker.SportTracker;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class CommandDistributer {

    public static final String prefix = Main.prefix;
    public static final String stickerpref = Main.stickerpref;
    public static ArrayList<Message> toDelete = new ArrayList<>();

    private static boolean russianGrammar = false;

    public static void handleCommands(MessageCreateEvent event) {
        if (event.getMessage().getContent() != null) {
            deleteOldMessages();

            // tests if message starts with prefix
            if (event.getMessage().getContent().startsWith(prefix)) {
                complexCommand(event);
            } else if (event.getMessage().getContent().startsWith(stickerpref)) {
                StickerHandler.stickerEvent(event);
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
        CommandProperties properties = new CommandProperties(event);

        if (properties.command.equalsIgnoreCase(prefix + "EB")) {
            EngeleBengele.handleCommand(event, properties);
        }

        else if (properties.command.equalsIgnoreCase(prefix + "SP")) {
            SportTracker.handleCommand(event, properties);

        }

        else {
            quickCommands(event, properties);
        }

        // found überprüft ob der Command irgentwo gefunden wurde
        /*
         * if (!found) { eventChannel.createEmbed(spec ->
         * spec.setDescription("Command could not be found")).block(); }
         */
    }

    private static void quickCommands(MessageCreateEvent event, CommandProperties props) {
        props.removePrefix();

        switch (props.command.toLowerCase()) {
            case "ping":
                props.eventChannel.createMessage("Pong!").block();
                break;

            // --------------- General --------------------

            case "help":
            case "commands":
                sendCommandsMessage(event, props.eventChannel);
                break;

            case "togglerussiangrammar":
                toggleGlobalRussianGrammar(event, props.eventChannel);
                break;

            // --------------- Sticker --------------------

            case "addsticker":
                StickerHandler.addStickerEvent(event, props);
                break;

            case "deletesticker":
            case "removesticker":
                StickerHandler.deleteStickerEvent(event, props);
                break;

            // --------------- Auth --------------------

            //when nothing added = your auth level, otherwise authlevel of that id
            case "getauthlvl":
                int authlevel = Authorizer.getAuthorizationLevel(event, Main.authorizations);
                Globals.createEmbed(props.eventChannel, Color.MAGENTA, "The Authorizationlevel of " + Authorizer.getUserByID(event) +" is: **"
                        + convertAuthLeveltoLevelName(authlevel) + "** " + "(" + authlevel + ")", "");
                break;

            case "getallauthlvl":
            case "getallauthids":
                getAllAuthLvl(event, props);
                break;

            case "setauthlvl":
            case "changeauthlvl":
                changeAuthLVL(event, props);
                break;

            case "removeauthid":
            case "deleteauthid":
            case "removeauthlvl": // cuz ppl are dumb
                removeAuthID(event, props);
                break;

            default:
                props.eventChannel.createMessage("command not found...").block();

        }

    }

    private static void sendCommandsMessage(MessageCreateEvent event, MessageChannel channel) {

        String mssg = " ---- Sticker ---- ";
        mssg += buildCommandDescription(". ", "", "Listet alle Sticker");
        mssg += buildCommandDescription(".", "<stickername>", "Sendet jenen Sticker");
        mssg += buildCommandDescription(".", "help", "Info & Commands");
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 1) {
            mssg += " \n---- LVL1 ---- ";
            mssg += buildCommandDescription("%", "addSticker <name> <url>", "add a Sticker to the Collection");
            mssg += buildCommandDescription("%", "removeSticker <name>", "remove a Sticker from the Collection");
        }

        mssg += " \n---- General ---- ";
        mssg += buildCommandDescription(prefix, "EB commands", "Engele Bengele!");
        mssg += buildCommandDescription(prefix, "SP commands", "Sport Tracker");
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            mssg += " \n---- LVL2 ---- ";
            mssg += buildCommandDescription("%", "toggleRussianGrammar", "Toggles the Russian Autocorrection");
        }

        mssg += " \n---- Authorization ---- ";
        mssg += buildCommandDescription(prefix, "getAuthLVL", "Either gets your own authorization level or the one of a given ID");
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 1) {
            mssg += buildCommandDescription(prefix, "setAuthLVL", "Sets the Auth. Level of an ID");
            mssg += buildCommandDescription(prefix, "deleteAuthID", "Removes the Auth. Level of an ID");
        }
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            mssg += buildCommandDescription(prefix, "getAllAuthIDs", "Lists all registered authIDs");
        }

        mssg += "\n\nWenn du a coole Idee für a Funktion fürn bot hosch, feel free es oanem von die Mods weiterzuleiten!";

        Globals.createEmbed(channel, Color.BLACK, "", mssg);
    }

    public static String buildCommandDescription(String pref, String command, String description) {
        return "\n" + "`" + pref + command + " :` " + description;
    }

    // toggles global russian grammar
    private static void toggleGlobalRussianGrammar(MessageCreateEvent event, MessageChannel channel) {
        // only me can do this
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            russianGrammar = !russianGrammar;
            channel.createMessage("`RUSSIAN GRAMMAR: " + russianGrammar + "`").block();
        } else {
            Globals.createEmbed(channel, Color.RED, "", "You are not authorized to use this Command");
        }
    }

    private static void reaction(MessageCreateEvent event) {
        Memes(event);
    }

    public static void Memes(MessageCreateEvent event) {
        if (event.getMessage().getContent() != null) {
            // this is not implemented beacuse of performance reasons
            // var eventChannel = event.getMessage().getChannel().block();
            var content = event.getMessage().getContent();

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

    private static void changeAuthLVL(MessageCreateEvent event, CommandProperties props) {
        if (props.params.size() == 2) {
            var id = props.params.get(0);
            try {
                var lvl = Integer.parseInt(props.params.get(1));
                if (lvl >= 0 && lvl <= 10) {
                    if (Authorizer.getAuthorizationLevel(event, Main.authorizations) > lvl) {
                        if (Authorizer.changeAuthorization(id, lvl, Main.authorizations, Main.authFilepath)) {
                            Globals.createEmbed(props.eventChannel, Color.MAGENTA, "",
                                    "Changed the Authorization Level of " + id + " to " + lvl + ".");
                        }
                    } else {
                        Globals.createEmbed(props.eventChannel, Color.RED, "",
                                "Your Authorization Level is not high enough for this Command.");
                    }
                } else {
                    Globals.createEmbed(props.eventChannel, Color.RED, "", "Choose a Level between 0 and 10 ");
                }
            } catch (Exception e) {
                System.out.println("CommandDistributer: changeAuthLVL: Could not Convert to int");
                Globals.createEmbed(props.eventChannel, Color.RED, "",
                        "Unsuccessful: second parameter must be an Integer");
            }
        } else {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "Wrong Syntax");
        }
    }

    private static void removeAuthID(MessageCreateEvent event, CommandProperties props) {
        if (props.params.size() == 1) {
            var id = props.params.get(0);
            if (Authorizer.getAuthorizationLevel(event, Main.authorizations) > Authorizer.getAuthorizationLevel(id,
                    Main.authorizations)) {
                if (Authorizer.deleteAuthorization(id, Main.authorizations, Main.authFilepath)) {
                    Globals.createEmbed(props.eventChannel, Color.MAGENTA, "",
                            "Removed the Authorization Level of " + id + ".");
                }

            } else {
                Globals.createEmbed(props.eventChannel, Color.RED, "",
                        "Your Authorization Level is not high enough for this Command.");
            }
        } else {
            Globals.createEmbed(props.eventChannel, Color.RED, "",
                    "Wrong Syntax, try " + Main.prefix + "removeauthid <id>");
        }
    }

    private static void getAllAuthLvl(MessageCreateEvent event, CommandProperties props) {
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            var mssg = "";
            for (var authID : Main.authorizations) {
                mssg += authID.id + "  :  " + authID.level + "\n";
            }
            Globals.createEmbed(props.eventChannel, Color.MAGENTA, "All registered authIDs:", mssg);
        }
    }

    public static String convertAuthLeveltoLevelName(int authlevel){
        switch (authlevel) {
            case 0:
                return "User";
            case 1:
                return "Moderator";
            case 2:
                return "Admin";
            case 4:
                return "Developer 😎";
            case 10:
                return "Owner";
            default:
                return "Unknown Authorization";
        }
    }

}
