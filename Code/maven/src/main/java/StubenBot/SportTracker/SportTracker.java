package StubenBot.SportTracker;


import StubenBot.CommandDistributer;
import StubenBot.CommandProperties;
import StubenBot.Globals;
import StubenBot.Main;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;

public class SportTracker {

    public static void handleCommand(MessageCreateEvent event, CommandProperties props) {
        //sets the command correctly
        props.command = props.params.remove(0);

        if (props.command.equalsIgnoreCase("test")) {
            props.eventChannel.createMessage("Sport in Progress").block();
            
        }
        if(props.command.equalsIgnoreCase("commands") || props.command.equalsIgnoreCase("help")){
            var mssg = "\n ---- Engele Bengele ---- ";
            mssg += CommandDistributer.buildCommandDescription(Main.prefix, "SP I_did_Sport", "Get your Reward ;)");
            mssg += "\nThis feature is still very much work in progress... ";

            Globals.createEmbed(props.eventChannel, Color.BLACK, "", mssg);
            
        }
        if (props.command.equalsIgnoreCase("I_did_Sport")) {
            props.eventChannel.createEmbed(
                    a -> a.setImage("http://theoldreader.com/kittens/" + ((int) (Math.random() * 700) + 300) + "/"
                            + ((int) (Math.random() * 700) + 300) + "/").setTitle("Good Job! Have A Kitten"))
                    .block();
        }
    }

}