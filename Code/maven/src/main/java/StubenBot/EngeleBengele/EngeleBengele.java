package StubenBot.EngeleBengele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import StubenBot.Main;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;

public class EngeleBengele {
    public static HashMap<Snowflake, EBPlayer> mapJoined = new HashMap<>();

    public static void handleCommand(MessageCreateEvent event, String command, List<String> messageParameters,
            MessageChannel eventChannel) {

        var author = event.getMessage().getAuthor().get();
        command = messageParameters.remove(0);

        if (command.equalsIgnoreCase("join")) {
            // gets the name
            var name = "";

            if (messageParameters.size() == 0) {
                name = author.getUsername();
            } else {
                for (String string : messageParameters) {
                    name += string + " ";
                }
            }
            // creates new bengele
            var player = new EBPlayer(name, author);

            // fills map
            if (mapJoined != null && !mapJoined.containsKey(author.getId())) {
                mapJoined.put(author.getId(), player);
            }
            eventChannel.createMessage("You're in!").block();

        }

        if (command.equalsIgnoreCase("start")) {
            boolean a = false;
            while (!a) {
                // assignes bengeles & sends messages
                a = setBengeles(mapJoined);
            }
            eventChannel.createMessage("DONE!").block();
        }

        if (command.equalsIgnoreCase("reset")) {
            reset(event, mapJoined);
        }

        if (command.equalsIgnoreCase("list")) {
            var mssg = "";
            for (var player : mapJoined.values()) {
                mssg += "\n" + player.name;
            }
            eventChannel.createMessage("Joined: " + mapJoined.size() + mssg).block();
        }

        if (command.equalsIgnoreCase("remove")) {
            // gets the name
            var name = "";
            for (String string : messageParameters) {
                name += string;
            }

            // finds matching bengele
            for (var a : mapJoined.entrySet()) {
                if (a.getValue().name.replaceAll("\\s+", "").equalsIgnoreCase(name)) {
                    mapJoined.remove(a.getKey());
                    eventChannel.createMessage("Removed.").block();
                } else {
                    eventChannel
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