package main.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.AppState;
import main.Db;
import main.HabrClient;
import main.ScanJobsHelper;
import main.exceptions.HabrHttpException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class ScanJob implements Job {
	private final AppState appState;
	private final Db db;
	private final HabrClient habrClient;
	private final ScanJobsHelper scanJobsHelper;

	private void scanNewPosts() {
		Map<Integer, Boolean> posts = new HashMap<>();
		try {
			int maxSitePostId = habrClient.getMaxPostIdFromRss();
			for (Integer postId : db.getUnprocessedPostsIds(maxSitePostId)) {
				boolean postHasABBR = habrClient.isPostHasABBR(postId);
				posts.put(postId, postHasABBR);
				db.saveNewPost(postId, postHasABBR);
			}
		} catch (HabrHttpException e) {
			if (!posts.isEmpty()) {
				db.saveNewPosts(posts);
				scanJobsHelper.sendTelegramMessages(posts);
			}
			appState.swapAppState();
			return;
		}
		if (posts.isEmpty()) {
			return;
		}
		db.saveNewPosts(posts);
		scanJobsHelper.sendTelegramMessages(posts);
	}

	private void sendToTelegramOldIds() {
		List<Integer> notSentToTelegramPostsHasAbbr = db.getNotSentToTelegramPostsHasAbbr();
		if (notSentToTelegramPostsHasAbbr.isEmpty()) {
			return;
		}

		Map<Integer, Boolean> toSend = new HashMap<>();
		notSentToTelegramPostsHasAbbr.forEach(id -> toSend.put(id, true));
		scanJobsHelper.sendTelegramMessages(toSend);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("ScanJob start");
		scanNewPosts();
		sendToTelegramOldIds();
		log.info("ScanJob finish");
	}
}
