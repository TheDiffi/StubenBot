package StubenBot.EngeleBengele;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import StubenBot.CommandDistributer;
import StubenBot.CommandProperties;
import StubenBot.Globals;
import StubenBot.Main;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;

public class EngeleBengele {
    public static HashMap<Snowflake, EBPlayer> mapJoined = new HashMap<>();

    public static void handleCommand(MessageCreateEvent event, CommandProperties props) {
  
        //sets the command correctly
        props.command = props.params.remove(0);


        if(props.command.equalsIgnoreCase("commands") || props.command.equalsIgnoreCase("help")){
            var mssg = "\n ---- Engele Bengele ---- ";
            mssg += CommandDistributer.buildCommandDescription(Main.prefix, "EB join <name>", "Trete dem Spiel bei");
            mssg += CommandDistributer.buildCommandDescription(Main.prefix, "EB remove <name>", "Entferne einen Spieler");
            mssg += CommandDistributer.buildCommandDescription(Main.prefix, "EB start", "Starte die zufällige Verteilung!");

            Globals.createEmbed(props.eventChannel, Color.BLACK, "", mssg);
        }
        if (props.command.equalsIgnoreCase("join")) {
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

        }

        if (props.command.equalsIgnoreCase("start")) {
            boolean a = false;
            while (!a) {
                // assignes bengeles & sends messages
                a = setBengeles(mapJoined);
            }
            props.eventChannel.createMessage("DONE!").block();
        }

        if (props.command.equalsIgnoreCase("reset")) {
            reset(event, mapJoined);
        }

        if (props.command.equalsIgnoreCase("list")) {
            var mssg = "";
            for (var player : mapJoined.values()) {
                mssg += "\n" + player.name;
            }
            props.eventChannel.createMessage("Joined: " + mapJoined.size() + mssg).block();
        }

        if (props.command.equalsIgnoreCase("remove")) {
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