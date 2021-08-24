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
        GatewayDiscordClient client = DiscordClientBuilder
                .create("NzgzNzg2NzU4MzgyMTU3ODQ1.X8f0TQ.DI7I4Qe4nnWow28MQ-qYMxlaXyc").build().login().block();

        // waits for login
        client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
            try {
                final User self = event.getSelf();
                System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Ran into a critical error while starting!");
            }
        });

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

        // finalizes login
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
