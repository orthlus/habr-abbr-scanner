package main.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.Db;
import main.HabrClient;
import main.TelegramMessageSender;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class ScanJob implements Job {
	private final Db db;
	private final HabrClient habrClient;
	private final TelegramMessageSender telegram;

	private void scanNewPosts() {
		int maxSitePostId = habrClient.getMaxPostIdFromRss();
		int lastScannedPostId = db.getLastScannedPostId();
		IntStream
				.rangeClosed(lastScannedPostId + 1, maxSitePostId)
				.filter(habrClient::isPostHasABBR)
				.forEach(postId -> {
					String msg = telegramMsg(postId);
					telegram.sendChannelMessage(msg);
				});
		db.updateLastScanned(maxSitePostId);
	}

	private String telegramMsg(int postId) {
		return """
				Новый пост с аббревиатурой:
				https://habr.com/ru/post/%s/
				""".formatted(postId);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("ScanJob start");
		try {
			scanNewPosts();
		} catch (Exception e) {
			log.error("scan error", e);
			throw new JobExecutionException(e);
		}
		log.info("ScanJob finish");
	}
}
