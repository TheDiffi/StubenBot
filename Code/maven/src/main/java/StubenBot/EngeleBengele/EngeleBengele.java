package StubenBot.EngeleBengele;

import java.util.ArrayList;
import java.util.HashMap;

import StubenBot.CommandDistributer;
import StubenBot.CommandProperties;
import StubenBot.Globals;
import StubenBot.Main;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;

public class EngeleBengele {
    public static HashMap<Snowflake, EBPlayer> mapJoined = new HashMap<>();
    public static String prefix = Main.prefix + "EB";

    public static void handleCommand(MessageCreateEvent event, CommandProperties props) {

        // sets the command correctly
        props.command = props.params.remove(0);
        var commandType = props.command.toLowerCase();

        if (commandType.equals("commands") || commandType.equals("help")) {
            String helpMessage = buildHelpMessage(props);
            Globals.createEmbed(props.eventChannel, Color.BLACK, "", helpMessage);

        }
        if (commandType.equals("join")) {
            // gets the name
            var name = "";

            if (props.params.size() == 0) {
                name = props.author.getUsername();
            } else {
                for (String string : props.params) {
                    name += string + " ";
                }
            }
            // creates new bengele
            var player = new EBPlayer(name, props.author);

            // fills map
            if (mapJoined != null && !mapJoined.containsKey(props.author.getId())) {
                mapJoined.put(props.author.getId(), player);
            }
            props.eventChannel.createMessage("You're in!").block();

            var concatenatedNames = mapJoined.values().stream().map(a -> a.name).reduce("", (a, b) -> a + "\n" + b);
            props.eventChannel.createMessage("Currently joined: " + mapJoined.size() + concatenatedNames).block();

        }

        if (commandType.equals("start")) {
            boolean a = false;
            while (!a) {
                // assignes bengeles & sends messages
                a = setBengeles(mapJoined);
            }
            props.eventChannel.createMessage("DONE!").block();
        }

        if (commandType.equals("reset")) {
            reset(event, mapJoined);
            props.eventChannel.createMessage("Reset Players").block();

        }

        if (commandType.equals("list")) {
            var mssg = "";
            for (var player : mapJoined.values()) {
                mssg += "\n" + player.name;
            }
            props.eventChannel.createMessage("Joined: " + mapJoined.size() + mssg).block();
        }

        if (commandType.equals("remove")) {
            // gets the name
            var name = "";
            for (String string : props.params) {
                name += string;
            }

            // finds matching bengele
            for (var a : mapJoined.entrySet()) {
                if (a.getValue().name.replaceAll("\\s+", "").equalsIgnoreCase(name)) {
                    mapJoined.remove(a.getKey());
                    props.eventChannel.createMessage("Removed.").block();
                } else {
                    props.eventChannel
                            .createMessage("Name not found. Use " + Main.prefix + "EB list to view all playernames.")
                            .block();
                }
            }

        }
    }

    private static String buildHelpMessage(CommandProperties props) {
        var title = "\n ---- Engele Bengele ---- ";
        var commands = new String[3][5];
        commands[0] = new String[] { prefix, prefix, prefix, prefix, prefix, prefix };
        commands[1] = new String[] { "join", "remove", "start", "list", "reset" };
        commands[2] = new String[] { "Trete dem Spiel bei", "Entferne einen Spieler",
                "Starte die zufällige Verteilung!",
                "Zähle beigetretene Spieler auf", "Lösche alle beigetretene Spieler" };

        var description = CommandDistributer.buildMultipleCommandsDescription(commands[0], commands[1], commands[2]);

        return title + description;
    }

    private static boolean setBengeles(HashMap<Snowflake, EBPlayer> mapJoined) {
        // Key = Engele; Value = Bengele
        var resultMap = new HashMap<EBPlayer, EBPlayer>();
        var tempList = new ArrayList<EBPlayer>();
        for (var a : mapJoined.values()) {
            tempList.add(a);
        }

        // determines the bengele of every engele
        for (EBPlayer engele : mapJoined.values()) {
            int rand;
            EBPlayer bengele;
            int failsave = 0;

            if (tempList != null && tempList.size() == 1 && engele.user.getId().equals(tempList.get(0).user.getId())) {
                System.out.println("Restarting...");
                return false;
            }

            do {
                rand = (int) (Math.random() * tempList.size());
                bengele = tempList.get(rand);
                failsave++;

            } while (engele == bengele || failsave == 30);

            if (failsave < 30) {

                resultMap.put(engele, bengele);
                tempList.remove(rand);

            } else {
                System.out.println("Restarting...");
                return false;
            }
        }

        // prints results to private chats
        for (EBPlayer engele : resultMap.keySet()) {
            engele.user.getPrivateChannel().block().createEmbed(a -> {
                a.setTitle("Dein Bengele isch...  " + resultMap.get(engele).name);
            }).block();

        }
        return true;

    }

    private static void reset(MessageCreateEvent event, HashMap<Snowflake, EBPlayer> mapJoined) {
        mapJoined.clear();
        event.getMessage().addReaction(ReactionEmoji.unicode("U+1F44C"));
    }

}