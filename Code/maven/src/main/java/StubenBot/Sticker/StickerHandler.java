package StubenBot.Sticker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import StubenBot.CommandDistributer;
import StubenBot.CommandProperties;
import StubenBot.Globals;
import StubenBot.Main;
import StubenBot.Authorization.Authorizer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class StickerHandler {
    private static final int distance = 20;

    private static String stickerJsonFilepath = "stickers/stickers.json";

    public static void setOSDependentFilepath() {
        String os = System.getProperty("os.name");
        System.out.println("Starting on: " + os);
        if (os.startsWith("Windows")) {
            stickerJsonFilepath = "stickers\\stickers.json";
        }
    }

    // , Command
    public static void stickerEvent(MessageCreateEvent event) {
        var eventChannel = event.getMessage().getChannel().block();
        String messageContent = event.getMessage().getContent();
        var command = messageContent.replace(".", "");

        // sends a list of all available stickers
        if (messageContent.equals(".")) {
            printAvailableStickers(event, eventChannel);

            // .help Command ------------------------------------
        } else if (messageContent.equals(".help")) {
            stickerHelpEvent(event);

        }

        // if the stickername is registered, it gets send
        // ------------------------------------
        else if (Main.registeredStickers.get(command) != null) {
            eventChannel.createMessage(Main.registeredStickers.get(command).url).block();

            /*
             * Sends the Sticker as an Embed String username = event.getMember().isPresent()
             * ? event.getMember().get().getUsername() :
             * event.getMessage().getAuthor().get().getUsername();
             * Globals.createEmbed(eventChannel, Color.BLACK, username, "",
             * Main.registeredStickers.get(command).url);
             * 
             * Sends the Username; works best if the original message is then deleted
             * eventChannel.createMessage(username + ": ").block();
             */

        }

        /*
         * deletes the users message if (event.getGuildId().isPresent()) {
         * event.getMessage().delete().block(); }
         */

    }

    private static void printAvailableStickers(MessageCreateEvent event, MessageChannel eventChannel) {
        var msg = "**All Stickers:**\n";
        var stickerList = Main.registeredStickers.values();
        // ---------------
        // message version
        int i = 1;
        for (Sticker sticker : Main.registeredStickers.values()) {
            msg += "." + sticker.name;

            if (i % 2 == 1) {
                int spaces = distance - sticker.name.length();
                for (int j = 0; j < spaces; j += 1) {
                    msg += "  ";
                }
                if (spaces <= 0) {
                    msg += "-";
                }
            } else {
                msg += "\n";
            }
            i++;
        }
        msg += "\n";
        // CommandDistributer.toDelete.add(eventChannel.createMessage(msg).block());
        // ----------------
        // embed version
        var firstMsgCol = "";
        var secondMsgCol = "";
        int j = 0;
        for (Sticker sticker : stickerList) {
            if (j < (stickerList.size() / 2)) {
                firstMsgCol += "." + sticker.name + "\n";
            } else {
                secondMsgCol += "." + sticker.name + "\n";
            }

            j++;
        }
        ArrayList<String> fields = new ArrayList<>();
        fields.add(firstMsgCol);
        fields.add(secondMsgCol);

        var emb = Globals.createEmbed(eventChannel, Color.RUST, "Stickers", fields);
        CommandDistributer.toDelete.add(emb);
        // ----------------------------
        // deletes the '.'
        if (event.getGuildId().isPresent()) {
            CommandDistributer.toDelete.add(event.getMessage());
        }
    }

    private static void stickerHelpEvent(MessageCreateEvent event) {
        String mssg = " ---- Sticker ---- ";
        mssg += buildCommandDescription(". ", "Listet alle Sticker");
        mssg += buildCommandDescription(".<stickername>", "Sendet jenen Sticker");

        // so it deletes after
        CommandDistributer.toDelete
                .add(Globals.createEmbed(event.getMessage().getChannel().block(), Color.LIGHT_GRAY, "", mssg));

        if (event.getGuildId().isPresent()) {
            CommandDistributer.toDelete.add(event.getMessage());
        }
    }

    public static String buildCommandDescription(String command, String description) {
        return "\n" + "`" + command + ":` " + description;
    }

    public static void addStickerEvent(MessageCreateEvent event, CommandProperties props) {
        if (!(Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 1)) {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "You are not authorized to use this Command");
            return;
        }
        if (!(props.params.size() >= 2)) {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "Unsuccsessful: Wrong Syntax");
            return;
        }
        if (props.params.get(1).startsWith("http")) {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "Unsuccsessful: No link detected!");
            return;
        }

        Sticker newSticker = new Sticker(props.params.get(0), props.params.get(1));
        var succsses = addSticker(newSticker);

        if (succsses) {
            props.eventChannel.createMessage("Adding Sticker: " + event.getMember().get().getUsername()
                    + "\nname: " + props.params.get(0) + "\nurl: " + props.params.get(1)).block();
            // eventChannel.createMessage("Please confirm with **" + Main.prefix+ "confirm
            // **").block();

        } else {
            props.eventChannel.createMessage("Failed!").block();
        }

    }

    private static boolean addSticker(Sticker newSticker) {
        try {

            JsonArray stickerArray = getStickerArray();

            // create a writer stickers\\stickers.json
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(stickerJsonFilepath));

            // appends it to the existing stickers
            JsonObject sticker = new JsonObject();
            sticker.put("name", newSticker.name);
            sticker.put("url", newSticker.url);
            stickerArray.add(sticker);

            // puts the array in the stickers object
            JsonObject stickerObject = new JsonObject();
            stickerObject.put("sticker", stickerArray);

            // writes the stickers object
            Jsoner.serialize(stickerObject, writer);

            // close the writer
            writer.close();

            reloadStickers();
            backupStickers(stickerObject);
            // success
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();

        } catch (JsonException e) {
            e.printStackTrace();
        }
        // failure
        return false;
    }

    public static void deleteStickerEvent(MessageCreateEvent event, CommandProperties props) {
        if (Authorizer.getAuthorizationLevel(event, Main.authorizations) >= 1) {
            if (props.params.size() == 1) {
                var succsses = deleteSticker(props.params.get(0));

                if (succsses) {
                    props.eventChannel.createMessage("Deleted Sticker: " + event.getMember().get().getUsername()
                            + "\nname: " + props.params.get(0)).block();
                    // eventChannel.createMessage("Please confirm with **" + Main.prefix+ "confirm
                    // **").block();

                } else {
                    props.eventChannel.createMessage("Unsuccsessful: No such sticker found").block();
                }
            } else {
                Globals.createEmbed(props.eventChannel, Color.RED, "", "Unsuccsessful: Wrong Syntax");
            }
        } else {
            Globals.createEmbed(props.eventChannel, Color.RED, "", "You are not authorized to use this Command");
        }

    }

    private static boolean deleteSticker(String name) {
        if (Main.registeredStickers.get(name) != null) {
            try {

                JsonArray stickerArray = getStickerArray();
                ArrayList<Object> foundSticker = new ArrayList<>();
                // create a writer
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(stickerJsonFilepath));

                // removes the sticker from the array (spaghetti)
                stickerArray.forEach(entry -> {
                    // creates a sticker object for every element and puts it into the map
                    JsonObject sticker = (JsonObject) entry;
                    if (((String) sticker.get("name")).equals(name)) {
                        foundSticker.add(entry);
                    }

                });
                if (foundSticker.size() > 0) {
                    for (Object object : foundSticker) {
                        stickerArray.remove(object);
                    }
                }

                // puts the array in the stickers object
                JsonObject stickerObject = new JsonObject();
                stickerObject.put("sticker", stickerArray);

                // writes the stickers object
                Jsoner.serialize(stickerObject, writer);

                // close the writer
                writer.close();

                reloadStickers();
                // success
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();

            } catch (JsonException e) {
                e.printStackTrace();
            }
        }
        // failure
        return false;
    }

    public static void reloadStickers() {
        Main.registeredStickers.clear();
        try {
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(stickerJsonFilepath));

            // create parser
            JsonObject parser = (JsonObject) Jsoner.deserialize(reader);

            JsonArray stickerArray = (JsonArray) parser.get("sticker");

            stickerArray.forEach(entry -> {
                // creates a sticker object for every element and puts it into the map
                JsonObject sticker = (JsonObject) entry;
                Sticker stickerobj = new Sticker((String) sticker.get("name"), (String) sticker.get("url"));
                Main.registeredStickers.put(stickerobj.name, stickerobj);

            });

            // System.out.println(Main.registeredStickers.toString());

            // close reader
            reader.close();

        } catch (IOException ex) {
            System.out.println("No File found, creating stickers.json...");
            createFile(stickerJsonFilepath);
            try {
                JsonArray stickerArray = new JsonArray();
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(stickerJsonFilepath));
                JsonObject stickerObject = new JsonObject();
                stickerObject.put("sticker", stickerArray);
                Jsoner.serialize(stickerObject, writer);
                writer.close();
                System.out.println("Retrying...");
                reloadStickers();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    public static void createFile(String filepath) {
        try {
            File myObj = new File(filepath);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void backupStickers(JsonObject stickers) {

        try {
            BufferedWriter failsaveWriter = Files.newBufferedWriter(Paths.get("stickers\\stickersBackup.json"));
            Jsoner.serialize(stickers, failsaveWriter);
            failsaveWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static JsonArray getStickerArray() throws IOException, JsonException {
        // read the array
        Reader reader = Files.newBufferedReader(Paths.get(stickerJsonFilepath));
        JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
        JsonArray stickerArray = (JsonArray) parser.get("sticker");
        // close reader
        reader.close();
        return stickerArray;
    }

}
