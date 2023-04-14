package main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramMessageSender extends DefaultAbsSender {
	@Value("${telegram.channel_id}")
	private String channelId;
	@Value("${telegram.bot.token}")
	private String botToken;

	public TelegramMessageSender() {
		this(new DefaultBotOptions());
	}

	public TelegramMessageSender(DefaultBotOptions options) {
		super(options);
	}

	public void sendChannelMessage(String message) {
		try {
			SendMessage msg = new SendMessage(channelId, message);
			msg.disableWebPagePreview();
			execute(msg);
		} catch (TelegramApiException e) {
			log.error("Error send telegram message", e);
		}
	}

	@Override
	public String getBotToken() {
		return botToken;
	}
}
