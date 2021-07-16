package StubenBot;

import java.util.ArrayList;
import java.util.TreeMap;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;


public class Main {
    public static final String prefix = "%";
    public static final String stickerpref = ".";
    public static final String authFilepath = "local/authorizedIDs.txt";
    public static TreeMap<String, Sticker> registeredStickers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static ArrayList<AuthID> authorizations = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        bootup();

        // loads the Bot api
        //Dev2 token: ODY1MjEzNzEzOTc5ODAxNjcy.YPAvEA.rWX55MIMqtEfz2o9Xphvym2twu4
        //original token: ODI3NjEyODE1OTU4NjA1ODM0.YGdkfw.3rQNr0IKh8fpkKlMhrV19N3RRHI
        GatewayDiscordClient client = DiscordClientBuilder.create("ODY1MjEzNzEzOTc5ODAxNjcy.YPAvEA.rWX55MIMqtEfz2o9Xphvym2twu4")
            .build()
            .login()
            .block();

        // looks at every message and calls "handleCommands"
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    try {
                        final User self = event.getSelf();
                        System.out.println(String.format(
                            "Logged in as %s#%s", self.getUsername(), self.getDiscriminator()
                        ));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Ran into a critical error while starting!");
                    }
                });
        
        String a = null;
        client.getEventDispatcher().on(MessageCreateEvent.class)
        .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
        .subscribe(event ->{
            try {
                CommandDistributer.handleCommands(event);
                System.out.println(a.length());
            } catch (Exception e) {
                event.getMessage().getChannel().block().createMessage(
                    "avoided critical Exception, pls don't repeat what u did *laughs in pain*  😰"
                ).block();

                if(event.getGuild().equals("\n 703300248843583569")){
                    event.getMessage().getChannel().block().createMessage(e.toString()).block();
                }
            }
        }
        );
        System.out.println("READY!");

        client.onDisconnect().block();

    }

    public static void bootup() {
        // reads stickers
        StickerHandler.setOSDependentFilepath();
        StickerHandler.reloadStickers();

        // reads Authorizations
        authorizations = Authorizer.readAuthorizedIDs(authFilepath);

    }

}
