package StubenBot;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;

public class StickersClass {

    private static String stickerJsonFilepath = "stickers/stickers.json";

    private static final int distance = 20;

    public static void determineOS() {
        String os = System.getProperty("os.name");
        System.out.println("Starting on: " + os);
        if (os.startsWith("Windows")) {
            stickerJsonFilepath = "stickers\\stickers.json";
        }

    }

    public static void stickerEvent(MessageCreateEvent event) {
        var eventChannel = event.getMessage().getChannel().block();
        String messageContent = event.getMessage().getContent().orElse("");
        var command = messageContent.replace(".", "");

        // sends a list of all available stickers
        if (messageContent.equals(".")) {
            var msg = "**All Stickers:**\n";
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
            CommandDistributer.toDelete.add(eventChannel.createMessage(msg).block());

            if (event.getGuildId().isPresent()) {
                CommandDistributer.toDelete.add(event.getMessage());
            }

        } else if (messageContent.equals(".help")) {
            String mssg = " ---- Sticker ---- ";
            mssg += buildCommandDescription(". ", "Listet alle Sticker");
            mssg += buildCommandDescription(".<stickername>", "Sendet jenen Sticker");
            if (event.getMessage().getAuthor().get().getId().asString().equals("317716883077988354")) {
                mssg += buildCommandDescription("%addSticker <name> <url>", "füge einen Sticker hinzu");
            }
            CommandDistributer.toDelete
                    .add(Globals.createEmbed(event.getMessage().getChannel().block(), Color.LIGHT_GRAY, "", mssg));

        }
        // if the stickername is registered, it gets send
        else if (Main.registeredStickers.get(command) != null) {
            String username = event.getMember().isPresent() ? event.getMember().get().getUsername()
                    : event.getMessage().getAuthor().get().getUsername();
            // Globals.createEmbed(eventChannel, Color.BLACK, username, "",
            // Main.registeredStickers.get(command).url);

            // eventChannel.createMessage(username + ": ").block();
            eventChannel.createMessage(Main.registeredStickers.get(command).url).block();

        }

        // deletes the users message

        /*
         * if (event.getGuildId().isPresent()) { event.getMessage().delete().block(); }
         */

    }

    public static String buildCommandDescription(String command, String description) {
        return "\n" + "`" + command + ":` " + description;
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

            System.out.println(Main.registeredStickers.toString());

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
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

    public static void addStickerEvent(MessageCreateEvent event, List<String> messagePieces,
            MessageChannel eventChannel) {
        if (messagePieces.size() >= 2) {
            if (messagePieces.get(1).startsWith("http")) {
                Sticker newSticker = new Sticker(messagePieces.get(0), messagePieces.get(1));
                var succsses = addSticker(newSticker);

                if (succsses) {
                    eventChannel.createMessage("Adding Sticker: " + event.getMember().get().getUsername() + "\nname: "
                            + messagePieces.get(0) + "\nurl: " + messagePieces.get(1)).block();
                    // eventChannel.createMessage("Please confirm with **" + Main.prefix+ "confirm
                    // **").block();

                } else {
                    eventChannel.createMessage("Failed!").block();
                }
            } else {
                Globals.createEmbed(eventChannel, Color.RED, "", "Unsuccsessful: No link detected!");
            }
        }

    }

    public static void deleteSticker(MessageCreateEvent event, List<String> messagePieces,
            MessageChannel eventChannel) {
        if (messagePieces.size() == 1) {
            var succsses = deleteSticker(messagePieces.get(0));

            if (succsses) {
                eventChannel.createMessage(
                        "Deleted Sticker: " + event.getMember().get().getUsername() + "\nname: " + messagePieces.get(0))
                        .block();
                // eventChannel.createMessage("Please confirm with **" + Main.prefix+ "confirm
                // **").block();

            } else {
                eventChannel.createMessage("Failed! (No such sticker)").block();
            }
        }
    }

}
