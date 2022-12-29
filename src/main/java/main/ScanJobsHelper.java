package main;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Slf4j
@Component
@Scope(scopeName = SCOPE_PROTOTYPE)
public class ScanJobsHelper {
	@Value("${telegram.posts_in_one_message}")
	private int postsInOneTelegramMessage;
	@Autowired
	private Db db;
	@Autowired
	private TelegramMessageSender telegram;

	public void sendTelegramMessages(Map<Integer, Boolean> posts) {
		List<Integer> newPosts = posts.keySet().stream()
				.filter(posts::get)
				.toList();

		newPosts = filterNotSentPosts(newPosts);
		newPosts = new ArrayList<>(newPosts);
		newPosts.sort(null);

		List<List<Integer>> messagesData = Lists.partition(newPosts, postsInOneTelegramMessage);

		messagesData.forEach(this::sendTelegramMessage);
	}

	private void sendTelegramMessage(List<Integer> postIds) {
		String startMsg = "Новые посты с использованием аббревиатур:\n";
		String msg = postIds.stream()
				.map("https://habr.com/ru/post/%s/"::formatted)
				.collect(Collectors.joining("\n"));
		telegram.sendChannelMessage(startMsg + msg);
		db.saveTelegramMessages(postIds);
	}

	private List<Integer> filterNotSentPosts(List<Integer> posts) {
		List<Integer> idsAlreadySentToTelegram = db.getIdsSentToTelegram();
		//except
		return posts.stream().filter(o -> !idsAlreadySentToTelegram.contains(o)).toList();
	}
}
