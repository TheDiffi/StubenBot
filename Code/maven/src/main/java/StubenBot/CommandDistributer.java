package StubenBot;

import java.util.ArrayList;

import StubenBot.Authorization.Authorizer;
import StubenBot.Commands.Memes;
import StubenBot.Authorization.AuthID;
import StubenBot.EngeleBengele.EngeleBengele;
import StubenBot.Polls.Pollinator;
import StubenBot.SportTracker.SportTracker;
import StubenBot.Sticker.StickerHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import StubenBot.Main;

public class CommandDistributer {

    public static final String prefix = Main.prefix;
    public static final String stickerpref = Main.stickerpref;
    public static ArrayList<Message> toDelete = new ArrayList<>();

    public static void handleCommands(MessageCreateEvent event) throws InterruptedException {
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
    private static void complexCommand(MessageCreateEvent event) throws InterruptedException {
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

    private static void quickCommands(MessageCreateEvent event, CommandProperties props) throws InterruptedException {
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

            // --------------- Polls --------------------

            case "createpoll":
                Pollinator.createPoll(event);
                break;
            case "stoppoll":
                // Pollinator.stopPoll
                break;

            // --------------- Auth --------------------

            // when nothing added = your auth level, otherwise authlevel of that id
            case "getauthlvl":
                sendAuthLvlMessage(event, props);
                break;

            case "getallauthlvl":
            case "getallauthids":
                Authorizer.getAllAuthLvl(event, props);
                break;

            case "setauthlvl":
            case "changeauthlvl":
                Authorizer.changeAuthLVL(event, props);
                break;

            case "removeauthid":
            case "deleteauthid":
            case "removeauthlvl": // cuz ppl are dumb
                Authorizer.removeAuthID(event, props);
                break;

            default:
                props.eventChannel.createMessage("command not found...").block();

        }

    }

    private static void sendAuthLvlMessage(MessageCreateEvent event, CommandProperties props) {
        int authlevel = Authorizer.getAuthorizationLevel(event, Main.authorizations);
        Globals.createEmbed(props.eventChannel, Color.MAGENTA,
                "The Authorizationlevel of " + Authorizer.getUserByID(event) + " is: **"
                        + Authorizer.convertAuthLeveltoLevelName(authlevel) + "** " + "(" + authlevel + ")",
                "");
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
        mssg += buildCommandDescription(prefix, "getAuthLVL",
                "Either gets your own authorization level or the one of a given ID");
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 1) {
            mssg += buildCommandDescription(prefix, "setAuthLVL", "Sets the Auth. Level of an ID");
            mssg += buildCommandDescription(prefix, "deleteAuthID", "Removes the Auth. Level of an ID");
        }
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            mssg += buildCommandDescription(prefix, "getAllAuthIDs", "Lists all registered authIDs");
        }

        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 2) {
            mssg += " \n---- Polls ----";
            mssg += buildCommandDescription(prefix, "createpoll <Title>; Option1 | Option2 | ...", "Create a poll");
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
            Memes.toggleRussianGrammar();
            channel.createMessage("`RUSSIAN GRAMMAR: " + Memes.printRussGrammar() + "`").block();
        } else {
            Globals.createEmbed(channel, Color.RED, "", "You are not authorized to use this Command");
        }
    }

    private static void reaction(MessageCreateEvent event) {
        Memes.handleMemes(event);
    }

}
