package StubenBot.EngeleBengele;

import discord4j.core.object.entity.User;

public class EBPlayer {
    public User user;
    public String name;
    EBPlayer(String name , User user){
        this.name = name;
        this.user = user;
    }
}