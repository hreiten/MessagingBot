package main.java;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class MessageProgram implements MessageListener {

	public String JSONPath; // the absolute path of a json file with messages objects
	public String recipient; // the phone number or email of the recipient (to be used with iMessage)
	public List<Message> messages; // A list containing all message objects

	public MessageProgram(String JSONPath, String recipient){
		this.JSONPath = JSONPath;
		this.recipient = recipient;

		try {
			// read messages from JSON file and add them to messages-list
			this.messages = readMessagesFromJSONFile(JSONPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// get list with all messages
	public List<Message> getAllMessages(){
		return messages;
	}

	// get a list of filtered messages based on isSent and category
	public List<Message> getFilteredMessages(boolean isSent, String category){
		return messages.stream()
				.filter(mess -> mess.hasBeenSent() == isSent)
				.filter(mess -> mess.getCategories().contains(category))
				.collect(Collectors.toList());
	}

	// returns a random, unsent message with given category
	public Message getRandomUnsentMessage(String category){

		// extract only the ones that have a valid date
		List<Message> arr = getFilteredMessages(false,category).stream()
				.filter(mess -> mess.getToDate().compareTo(new Date()) >= 0 && mess.getFromDate().compareTo(new Date()) <= 0)
				.collect(Collectors.toList());

		if (arr.size() > 0) {
			int randomint = new Random().nextInt(arr.size());
			return arr.get(randomint);
		} else {
			return null;
		}
	}

	// read messages from JSON file, return list of message objects
	public List<Message> readMessagesFromJSONFile(String filepath) throws IOException {
		JsonReader reader = new JsonReader(new FileReader(filepath));
		Message[] arr = new Gson().fromJson(reader, Message[].class);
		reader.close();

		List<Message> messages = new ArrayList<Message>(Arrays.asList(arr));

		return messages;
	}

	// write messages to JSONFile
	public void writeMessagesToJSONFile(String filepath, List<Message> messages) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filepath, false), "UTF-8"));

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		gson.toJson(messages, writer);

		writer.close();
	}

	// add message to messages
	public void addMessage(Message message) {
		if (!messages.contains(message)) {
			messages.add(message);

			messageHasChanged(message);
		}
	}

	// remove message from messages
	public void removeMessage(Message message) {
		if (messages.contains(message)) {
			messages.remove(message);

			messageHasChanged(message);
		}
	}


	// send a message via iMessage to recipient
	public void sendMessage(Message message, String recipient) {
		String script = "tell app \"Messages\"\n" +
                "set iMessageService to 1st service whose service type = iMessage\n" +
                "set iMessageBuddy to buddy \"" + recipient + "\" of iMessageService\n" +
                "send \"" + message.getContent() + "\" to iMessageBuddy\n" +
                "end tell";

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
		
		try {
			engine.eval(script);
			message.setSentStatus(true);

			messageHasChanged(message);
		} catch (ScriptException err) {
			err.printStackTrace();
		}
	}

	// method to update messages file whenever a change has been made to a message
	@Override
	public void messageHasChanged(Message message) {

		// update the JSON file containing messages
		try {
			writeMessagesToJSONFile(JSONPath, messages);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// checks the system time and determines the current category
	public String getCurrentCategory() {
		// find out what time it is of the day and set category accordingly
		Date date = new Date();
		@SuppressWarnings("deprecation")
		double time = date.getHours() + (double) date.getMinutes()/60;

		String cat = "";
		if (time >= 7 && time < 9) {
			cat = "morning";
		} else if (time >= 12 && time < 18) {
			cat = "midday";
		} else if (time >= 18 && time < 22) {
			cat = "evening";
		} else if ((time >= 22 && time <= 24) || (time >= 0 && time < 2)) {
			cat = "night";
		}

		return cat;
	}

	public void sendEmail(String from, String to, String subject, String content, String passwordPath) {
		Email email = new Email(from, passwordPath);
		email.sendMail(to, subject, content);
	}

	public void run() {
		// find out what time it is of the day and set category accordingly
		String cat = getCurrentCategory();

		// find random new message and send to recipient
		Message messageToBeSent = this.getRandomUnsentMessage(cat);
		if (!(messageToBeSent == null)) {
			sendMessage(messageToBeSent, recipient);

			// send email notifictation
			String subject = "You sent a new message to " + recipient + " using AutomaticMessageService";
			String to = "ha.reiten@gmail.com";
			String content = "<p>You sent a new message to " + recipient + " on the following time: " + new Date().toString() + "</p>"
					+ "<p>Message contents:</p>"
					+ "<p>" + messageToBeSent.getContent() + "</p>";
			sendEmail(to, to, subject, content, "/Users/halvorreiten/Documents/Programming/.password.txt");

		} else {
			// no messages to send at current time or date
			if (cat.equals("")) {
				System.err.println("No messages to send this time of day");
			} else {
				System.err.println("All messages of category \"" + cat + "\" have been sent already.");
			}
		}
	}

	public static void main(String[] args) throws IOException {

		// change path to wherever you have stored your messages
		String filepath = "/Documents/Programming/Java/AutomaticMessageService/AutomaticMessageService/src/main/resources/messages.json";
		String recipient = "ha.reiten@gmail.com";
		
		MessageProgram mp = new MessageProgram(filepath, recipient);
		
		mp.run();

	}
}
