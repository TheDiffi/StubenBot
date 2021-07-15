package StubenBot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class CommandProperties {
    public MessageChannel eventChannel;
    public String content;
    public List<String> params;
    public String command;
    public User author;
    
    
    public CommandProperties(MessageCreateEvent event){
        eventChannel = event.getMessage().getChannel().block();
        content = event.getMessage().getContent();
        params = new LinkedList<>(Arrays.asList(content.split(" ")));
        command = params.remove(0);
        author = event.getMessage().getAuthor().get();
    }

    public void removePrefix(){
        command = command.substring(1);
    }
    
}
