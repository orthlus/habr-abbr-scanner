package main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramMessageSender extends TelegramLongPollingBot {
	@Value("${telegram.channel_id}")
	private String channelId;
	@Value("${telegram.admin_id}")
	private String adminId;
	@Value("${telegram.bot.nickname}")
	private String botNickname;
	@Value("${telegram.bot.token}")
	private String botToken;

	public void sendAdminMessage(String message) {
		sendMessage(adminId, message);
	}

	public void sendChannelMessage(String message) {
		sendMessage(channelId, message);
	}

	private void sendMessage(String userId, String message) {
		try {
			execute(new SendMessage(userId, message));
		} catch (TelegramApiException e) {
			log.error("Ошибка отправки сообщения", e);
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		String userId = String.valueOf(update.getMessage().getChatId());
		sendMessage(userId, "Работает");
	}

	@Override
	public String getBotUsername() {
		return botNickname;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}
}
