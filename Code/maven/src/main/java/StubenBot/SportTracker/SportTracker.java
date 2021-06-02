package StubenBot.SportTracker;

import java.util.List;
import java.awt.Color;

import StubenBot.CommandDistributer;
import StubenBot.Globals;
import StubenBot.Main;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;

public class SportTracker {

    public static void handleCommand(MessageCreateEvent event, String command, List<String> messageParameters,
            MessageChannel eventChannel) {
        
        command = messageParameters.remove(0);

        if (command.equalsIgnoreCase("test")) {
            eventChannel.createMessage("Sport in Progress").block();
            
        }
        if(command.equalsIgnoreCase("commands") || command.equalsIgnoreCase("help")){
            var mssg = "\n ---- Engele Bengele ---- ";
            mssg += CommandDistributer.buildCommandDescription(Main.prefix, "SP I_did_Sport", "Get your Reward ;)");
            mssg += "\nThis feature is still very much work in progress... ";

            Globals.createEmbed(eventChannel, Color.BLACK, "", mssg);
            
        }
        if (command.equalsIgnoreCase("I_did_Sport")) {
            eventChannel.createEmbed(
                    a -> a.setImage("http://theoldreader.com/kittens/" + ((int) (Math.random() * 700) + 300) + "/"
                            + ((int) (Math.random() * 700) + 300) + "/").setTitle("Good Job! Have A Kitten"))
                    .block();
        }
    }

}