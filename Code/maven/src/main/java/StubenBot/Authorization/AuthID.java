package StubenBot.Authorization;


public class AuthID {
    public String id;
    public int level;

    AuthID(String id, int level){
        this.id = id;
        this.level = level;
    }

    public String toString(){
        var str = id + ":" + level + ";"; 
        return str;
    }



}
