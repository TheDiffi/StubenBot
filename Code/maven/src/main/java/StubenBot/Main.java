package StubenBot;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;

import com.github.cliftonlabs.json_simple.JsonException;

import StubenBot.Authorization.AuthID;
import StubenBot.Authorization.Authorizer;
import StubenBot.Sticker.Sticker;
import StubenBot.Sticker.StickerHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class Main {
    public static final String prefix = "%";
    public static final String stickerpref = ".";
    public static final String authFilepath = "local/authorizedIDs.txt";
    public static TreeMap<String, Sticker> registeredStickers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static ArrayList<AuthID> authorizations = new ArrayList<>();
    public static GatewayDiscordClient client;
    public static void main(String[] args) throws Exception {

        bootup();

        // loads the Bot api
        //Dev2 token: ODY1MjEzNzEzOTc5ODAxNjcy.YPAvEA.rWX55MIMqtEfz2o9Xphvym2twu4
        //original token: ODI3NjEyODE1OTU4NjA1ODM0.YGdkfw.3rQNr0IKh8fpkKlMhrV19N3RRHI
        client = DiscordClientBuilder.create("ODY1MjEzNzEzOTc5ODAxNjcy.YPAvEA.rWX55MIMqtEfz2o9Xphvym2twu4")
            .build()
            .login()
            .block();

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
                                        "avoided critical Exception, pls don't repeat what u did *laughs in pain*")
                                .block();
                        if (event.getGuildId().get().asString().equals("703300248843583569")) {
                            event.getMessage().getChannel().block().createMessage("\n " + ex.toString()).block();
                        }
                    }
                });

        client.getEventDispatcher().on(MessageCreateEvent.class)
        .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
        .subscribe(event ->{
            try {
                CommandDistributer.handleCommands(event);
            } catch (Exception e) {
                Globals.createEmbed(event.getMessage().getChannel().block(), Color.RED, "Critical Error ⚠️", 
                "avoided critical Exception, pls don't repeat what u did *laughs in pain*  😰 \n" +
                "But don't worry a Developer has already been notifed ✉️"
                );

                //dtf does not stand for waht you think it stands for :)
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");  
                LocalDateTime now = LocalDateTime.now();  

                MessageChannel channel = (MessageChannel) client.getChannelById(Snowflake.of("866007421738025020")).block();
                Role dev = client.getRoleById(Snowflake.of("707981224773288009"), Snowflake.of("866254682360119298")).block();

                //channel.createMessage(dev.getMention()).block();
                Globals.createEmbed(channel, Color.GREEN, "Bot ran into an error --- " + dtf.format(now), 
                    e.toString()       
                );

                //this doesnt seem to do anything
                if(event.getGuild().equals("\n 703300248843583569")){
                    event.getMessage().getChannel().block().createMessage(e.toString()).block();
                }

            }
        }
        );
        System.out.println("READY!");
        client.onDisconnect().block();

    }

    public static void bootup() throws IOException, JsonException {
        // reads stickers
        StickerHandler.setOSDependentFilepath();
        StickerHandler.reloadStickers();

        // reads Authorizations
        authorizations = Authorizer.readAuthorizedIDs(authFilepath);

        
    }


}
