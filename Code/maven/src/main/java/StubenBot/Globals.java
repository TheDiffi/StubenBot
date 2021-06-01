package StubenBot;



import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;


//In dieser Klasse werden alle Global nützliche Methoden geseichert
public class Globals {


	public static Message createEmbed(MessageChannel channel, Color color, String title, String description) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description);
			
		}).block();
		return embed;
	}

	public static Message createEmbed(MessageChannel channel, Color color, String title, String description, String imageUrl) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description).setImage(imageUrl);
			
		}).block();
		return embed;
	}

	public static Message createEmbed(MessageChannel channel, Color color, String title, String description, String imageUrl, String url) {
		var embed = channel.createEmbed(emb -> {
			emb.setColor(color).setTitle(title).setDescription(description).setImage(imageUrl).setUrl(url);
			
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

}