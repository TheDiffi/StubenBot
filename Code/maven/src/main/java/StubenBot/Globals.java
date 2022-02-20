package StubenBot;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParser;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;




//In dieser Klasse werden alle Global nützliche Methoden geseichert
public class Globals {

	public static Message createEmbed(MessageChannel channel, Color color, String title, String description) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(Color.GRAY).setTitle(title).setDescription(description);
		}).block();
		return embed;
	}

	public static Message createEmbed(MessageChannel channel, Color color, String title, String description,
			String imageUrl) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description).setImage(imageUrl);

		}).block();
		return embed;
	}

	public static Message createEmbed(MessageChannel channel, Color color, String title, String description,
			String imageUrl, String url) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description).setImage(imageUrl).setUrl(url);

		}).block();
		return embed;
	}
	
	public static Message createEmbed(MessageChannel channel, Color color, String title, String description, ArrayList<String> fields) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(Color.GRAY).setTitle(title).setDescription(description);
			for (String field : fields) {
				emb.addField("\u200B", field, true);
			}
		}).block();
		return embed;
	}
	
	public static Message createEmbed(MessageChannel channel, Color color, String title, 
			ArrayList<String> fields) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(Color.GRAY).setTitle(title);
			for (String field : fields) {
				emb.addField("\u200B", field, true);
			}
		}).block();
		return embed;
	}

	public static Message createMessage(MessageChannel channel, String message) {
		var mssg = channel.createMessage(messageSpec -> {
			messageSpec.setContent(message);
		}).block();
		return mssg;
	}

	public static Message createMessage(MessageChannel channel, String message, boolean ifTTS) {
		var mssg = channel.createMessage(messageSpec -> {
			messageSpec.setContent(message).setTts(ifTTS);
		}).block();
		return mssg;
	}

	public static String removeDash(String rawName) {
		var name = rawName.replaceAll("-", " ");
		return name;
	}

	public static void setMuteAllPlayers(List<User> listUsers, boolean isMuted, Snowflake serverId) {
		for (var user : listUsers) {
			try {
				user.asMember(serverId).block().edit(a -> {
					a.setMute(isMuted).setDeafen(false);
				}).block();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static List<User> getUsersFromJoinedVoicechannel(Snowflake serverId, MessageCreateEvent event) {
		var voiceStates = event.getMessage().getAuthor().get().asMember(serverId).block().getVoiceState().block()
				.getChannel().block().getVoiceStates().collectList().block();

		List<User> listUsers = new ArrayList<>();
		for (VoiceState voiceState : voiceStates) {
			listUsers.add(voiceState.getUser().block());
		}

		return listUsers;
	}

	public static boolean appendToTxt(String filename, String content) {
		try {
			FileWriter myWriter = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(myWriter);
			bw.write(content);
			bw.close();
			System.out.println("WARNING: Global: Successfully appended to the file " + filename + ".");
			return true;
		} catch (IOException e) {
			System.out.println("WARNING: Global: An error occurred: could not appended to file " + filename + "!");
			e.printStackTrace();
			return false;
		}

	}

	public static boolean writeToTxt(String filename, String content) {
		try {
			FileWriter myWriter = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(myWriter);
			bw.write(content);
			bw.close();
			System.out.println("WARNING: Global: Successfully wrote to the file " + filename + ".");
			return true;
		} catch (IOException e) {
			System.out.println("WARNING: Global: An error occurred: could not write to file " + filename + "!");
			e.printStackTrace();
			return false;
		}

	}

	public static String readTxt(String filename) throws Exception{
		// first reads the contend
		var fileContent = "";
		try {
			File myObj = new File(filename);
			Scanner fileReader = new Scanner(myObj);
			while (fileReader.hasNextLine()) {
				fileContent += fileReader.nextLine();
			}
			fileReader.close();
			return fileContent;

		} catch (FileNotFoundException e) {
			System.out.println("WARNING: Global: An error occurred while reading " + filename);
			e.printStackTrace();
			return "";
		}
	

	}

	public static String readJSON(String filename, String var) throws IOException, JsonException{
        // read the array
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
		String ret = parser.get(var).toString();
        // close reader
        reader.close();
        return ret;
	}

	public static void writeJSON(String filename, String var, String value) throws IOException, JsonException{
		JsonObject obj = new JsonObject();
        obj.put(var, value);
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));

		if(readJSON(filename, var) != null){
			 // writes the stickers object
			 Jsoner.serialize(obj, writer);

			 // close the writer
			 writer.close();
		}

	}

    public static void writeJsonArray(String filename, String arrayname ,String var, String val) throws JsonException {
		JsonArray jsonArray;
        try {

			if(readJsonArray(filename, arrayname) == null){
				jsonArray = new JsonArray();
			}else{
				jsonArray = readJsonArray(filename, arrayname);
			}
            JsonObject obj = new JsonObject();
            // create a writer stickers\\stickers.json
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));

            // appends it to the existing stickers
            obj.put(var, val);
            jsonArray.add(obj);

            // puts the array in the stickers object
            JsonObject stickerObject = new JsonObject();
            stickerObject.put("sticker", jsonArray);

            // writes the stickers object
            Jsoner.serialize(stickerObject, writer);

            // close the writer
            writer.close();
            // success
        } catch (IOException ex) {
            ex.printStackTrace();

        } catch (NullPointerException e) {
			e.printStackTrace();
		}
    }

    public static JsonArray readJsonArray(String filename, String arrayname) throws IOException, JsonException {
        // read the array
        Reader reader = Files.newBufferedReader(Paths.get(filename));
		JsonArray jsonArray;
		try {
			JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
			jsonArray = (JsonArray) parser.get(arrayname);
		} catch (Exception e) {
			jsonArray = null;
		}
        // close reader
        reader.close();
        return jsonArray;
    }
	
}