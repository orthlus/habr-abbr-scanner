package main.quartz;

import lombok.extern.slf4j.Slf4j;
import main.*;
import main.exceptions.HabrHttpException;
import main.exceptions.PageAccessDeniedException;
import main.exceptions.PageNotFoundException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DisallowConcurrentExecution
public class RareScanJob implements Job {
	@Value("${habr.count_not_found_posts_scan}")
	private int countNotFoundPostsScan;
	@Value("${habr.count_access_denied_posts_scan}")
	private int countAccessDeniedPostsScan;
	@Autowired
	private AppState appState;
	@Autowired
	private Db db;
	@Autowired
	private HabrClient habrClient;
	@Autowired
	private ScanJobsHelper scanJobsHelper;

	private void rescanAccessDeniedPosts() {
		Map<Integer, Boolean> posts = new HashMap<>();
		List<Integer> accessDeniedPosts = db.getLastNAccessDeniedPosts(countAccessDeniedPostsScan);
		try {
			for (Integer postId : accessDeniedPosts)
				try {
					boolean postHasABBR = habrClient.isPostHasABBR(postId);
					posts.put(postId, postHasABBR);
				} catch (PageAccessDeniedException e) {
					log.info("access denied post id {} still access denied", postId);
				} catch (PageNotFoundException ignored) {
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
		db.deleteAccessDeniedPosts(posts.keySet());
		scanJobsHelper.sendTelegramMessages(posts);
	}

	private void rescanNotFoundPosts() {
		Map<Integer, Boolean> posts = new HashMap<>();
		List<Integer> notFoundPosts = db.getLastNNotFoundPosts(countNotFoundPostsScan);
		try {
			for (Integer postId : notFoundPosts)
				try {
					boolean postHasABBR = habrClient.isPostHasABBR(postId);
					posts.put(postId, postHasABBR);
				} catch (PageNotFoundException e) {
					log.info("not found post id {} still not found", postId);
				} catch (PageAccessDeniedException ignored) {
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
		db.deleteNotFoundPosts(posts.keySet());
		scanJobsHelper.sendTelegramMessages(posts);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("RareScanJob start");
		rescanAccessDeniedPosts();
		rescanNotFoundPosts();
		log.info("RareScanJob finish");
	}
}
