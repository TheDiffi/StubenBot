package StubenBot.Polls;


import java.util.function.Consumer;

import StubenBot.Globals;
import StubenBot.Main;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;


public class Pollinator {

    public static final char[] abc = new char[] {'A','B','C','D','E','F','G','H','I','J','K','L','M',
        'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public static final String[] emoji = new String[] {"🇦","🇧","🇨","🇩","🇪","🇫","🇬","🇭","🇮","🇯","🇰","🇱",
        "🇲","🇳","🇴","🇵","🇶","🇷","🇸","🇹","🇺","🇻","🇼","🇽","🇾","🇿"};



    static String message, title;
    static String[] optionsArray;
    static StringBuilder options = new StringBuilder();
    public static void createPoll(MessageCreateEvent event) throws InterruptedException{
        MessageChannel channel = event.getMessage().getChannel().block();
        message = event.getMessage().getContent().replace("%createpoll","");
        title = message.split(";")[0];
        optionsArray = message.split(";")[1].split("\\|");

            //generates the string with the abc and newline
            options.append("**" + title + "**\n");
            for(int i = 0; i < optionsArray.length; i ++){
                options.append("**" + abc[i]  + ") **" + optionsArray[i].replace("|", "") + " (" + 0 + ")" + "\n");
            }

            //creates the embed and reacts to it with the abc emojis
            Message embed = channel.createMessage(options.toString()).block();
            for(int i = 0; i < optionsArray.length; i++){
                embed.addReaction(ReactionEmoji.unicode(emoji[i])).block();
            }


            //subscirbe to Reaction Event
            Main.client.getEventDispatcher().on(ReactionAddEvent.class)
            .filter(msg -> msg.getMessage().block().getAuthor().get().isBot())
            .subscribe(
                evt -> {
                    try {
                        upddatePoll(evt, embed);
                    } catch (Exception e) {
                        System.out.println("error: " + e);
                    }
                }
            );
        
        }



        //this function updates the poll and writes the percentage beside it
        //somehow it gets slower the more options are in the poll because i have to 
        //loop and get the number for all reactions twice.
        private static void upddatePoll(ReactionAddEvent evt, Message embed){
            ReactionEmoji reactem = evt.getEmoji();
            boolean auth = evt.getUser().block().isBot();
            double allReactions = 0;
            double votes = 0;

            options.delete(0, options.length());
            options.append("**" + title + "**\n");

            for(int j = 0; j < optionsArray.length; j++){
                allReactions += evt.getMessage().block().getReactors(ReactionEmoji.unicode(emoji[j])).collectList().block().size() - 1;
            }

            for(int j = 0; j < optionsArray.length; j ++){
                votes = evt.getMessage().block().getReactors(ReactionEmoji.unicode(emoji[j])).collectList().block().size() - 1; 
                options.append("**" + abc[j]  + ") **" + optionsArray[j].replace("|", "") + " (" + Math.round(((votes)/allReactions)*100d) + "%)" + "\n");
                // System.out.println( abc[j] + ") Votes: " + votes + " allReactions " + (allReactions));
            }
                

            

            Consumer<MessageEditSpec> template = spec -> spec.setContent(options.toString());      
            embed.edit(template).block();

        }
   
}
