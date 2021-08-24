package StubenBot.Sticker;

public class Sticker {

    public String name;
    public String url;

    public Sticker(String n, String u) {
        name = n;
        url = u;
    } 

    public String toString(){
        return "name: " + name + ", url: " + url; 
    }
}