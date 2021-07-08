package StubenBot;

import java.util.TreeMap;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {
    public static DiscordClient client;
    public static final String prefix = "%";
    public static final String stickerpref = ".";
    public static TreeMap<String, Sticker> registeredStickers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void main(String[] args) throws Exception {
        
        bootup();

        // loads the Bot api
        client = DiscordClientBuilder.create("Bot Token Here").build();

        // looks at every message and calls "handleCommands"
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    try {
                        CommandDistributer.handleCommands(event);
                    } catch (Exception ex) {
                        
                        ex.printStackTrace();
                        event.getMessage().getChannel().block()
                                .createMessage(
                                        "avoided critical Exception, pls don't repeat what u did *laughs in pain*" + 
                                        "\n " + ex)
                                .block();
                        
                    }
                });
        
        System.out.println("READY!");
        // DO NOT REMOVE
        client.login().block();
        
    }

    public static void bootup() {
        //reads stickers 
        StickersClass.determineOS();
        StickersClass.reloadStickers();
        

    }

}
