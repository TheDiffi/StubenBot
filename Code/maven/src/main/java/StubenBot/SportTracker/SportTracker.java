package StubenBot.SportTracker;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;

public class SportTracker {

    public static void handleCommand(MessageCreateEvent event, String command, List<String> messageParameters,
            MessageChannel eventChannel) {

        if (command.equalsIgnoreCase("test")) {
            eventChannel.createMessage("Sport in Progress").block();
            
        }
        if (command.equalsIgnoreCase("I_did_Sport")) {
            eventChannel.createEmbed(
                    a -> a.setImage("http://theoldreader.com/kittens/" + ((int) (Math.random() * 700) + 300) + "/"
                            + ((int) (Math.random() * 700) + 300) + "/").setTitle("Good Job! Have A Kitten"))
                    .block();
        }
    }

}