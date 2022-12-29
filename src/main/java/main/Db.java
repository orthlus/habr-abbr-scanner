package main;

import lombok.extern.slf4j.Slf4j;
import main.tables.records.TelegramMessagesRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Row2;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static main.Tables.ACCESS_DENIED_POSTS;
import static main.tables.NotFoundPosts.NOT_FOUND_POSTS;
import static main.tables.Posts.POSTS;
import static main.tables.TelegramMessages.TELEGRAM_MESSAGES;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectZero;

@Slf4j
@Component
public class Db {
	@Autowired
	private DSLContext db;

	public List<Integer> getIdsSentToTelegram() {
		return db.select(TELEGRAM_MESSAGES.POST_ID)
				.from(TELEGRAM_MESSAGES)
				.fetch(TELEGRAM_MESSAGES.POST_ID);
	}

	public void saveTelegramMessages(List<Integer> postIds) {
		List<TelegramMessagesRecord> records = postIds.stream()
				.map(postId -> new TelegramMessagesRecord().setPostId(postId))
				.toList();
		db.batchInsert(records).execute();
	}

	public void saveNewPosts(Map<Integer, Boolean> values) {
		List<Row2<Integer, Boolean>> records = values.keySet().stream()
				.map(key -> DSL.row(key, values.get(key)))
				.toList();
		int rows = db
				.insertInto(POSTS)
				.columns(POSTS.ID, POSTS.HAS_ABBR)
				.valuesOfRows(records)
				.onConflictDoNothing()
				.execute();
		if (rows != values.keySet().size()) {
			log.warn("db insert - Db.saveNewPosts inserted {} rows, but expected {}",
					rows, values.keySet().size());
		} else {
			log.info("Saved new posts {}", values.keySet());
		}
	}

	public void saveNewPost(int postId, boolean hasABBR) {
		int rows = db
				.insertInto(POSTS)
				.columns(POSTS.ID, POSTS.HAS_ABBR)
				.values(postId, hasABBR)
				.execute();
		if (rows != 1) {
			log.error("Error db insert - Db.saveNewPost inserted {} rows", rows);
		} else {
			log.info("Saved new post {}, has abbr {}", postId, hasABBR);
		}
	}

	public void saveNotFoundPost(int postId) {
		db.insertInto(NOT_FOUND_POSTS)
				.columns(NOT_FOUND_POSTS.ID)
				.values(postId)
				.execute();
		log.info("Saved new not found post {}", postId);
	}

	public List<Integer> getLastNNotFoundPosts(int n) {
		return db.select(NOT_FOUND_POSTS.ID)
				.from(NOT_FOUND_POSTS)
				.orderBy(NOT_FOUND_POSTS.ID)
				.limit(n)
				.fetch(NOT_FOUND_POSTS.ID);
	}

	public List<Integer> getUnprocessedPostsIds(int maxPostId) {
		return db.fetch("""
						SELECT GENERATE_SERIES((SELECT MIN(id) FROM habr_abbr_scanner.posts), ?)
						EXCEPT
						SELECT id FROM habr_abbr_scanner.posts
						EXCEPT
						SELECT id FROM habr_abbr_scanner.not_found_posts
						EXCEPT
						SELECT id FROM habr_abbr_scanner.access_denied_posts;
						""", maxPostId)
				.getValues(field("generate_series", Integer.class));
	}

	public void saveAccessDeniedPost(int postId) {
		db.insertInto(ACCESS_DENIED_POSTS)
				.columns(ACCESS_DENIED_POSTS.ID)
				.values(postId)
				.execute();
		log.info("Saved new access denied post {}", postId);
	}

	public List<Integer> getLastNAccessDeniedPosts(int n) {
		return db.select(ACCESS_DENIED_POSTS.ID)
				.from(ACCESS_DENIED_POSTS)
				.orderBy(ACCESS_DENIED_POSTS.ID)
				.limit(n)
				.fetch(ACCESS_DENIED_POSTS.ID);
	}

	public List<Integer> getNotSentToTelegramPostsHasAbbr() {
		return db.fetch("""
						SELECT id FROM habr_abbr_scanner.posts WHERE has_abbr
						EXCEPT
						SELECT post_id FROM habr_abbr_scanner.telegram_messages;
						""")
				.getValues(field("id", Integer.class));
	}

	public void deleteNotFoundPosts(Set<Integer> postIds) {
		db.delete(NOT_FOUND_POSTS)
				.where(NOT_FOUND_POSTS.ID.in(postIds))
				.execute();
	}

	public void deleteAccessDeniedPosts(Set<Integer> postIds) {
		db.delete(ACCESS_DENIED_POSTS)
				.where(ACCESS_DENIED_POSTS.ID.in(postIds))
				.execute();
	}

	public boolean isPostProcessed(int postId) {
		return db.fetchExists(selectZero()
				.from(POSTS)
				.where(POSTS.ID.eq(postId)));
	}

	public int getLastPostId() {
		return db.select(POSTS.ID)
				.from(POSTS)
				.orderBy(POSTS.TIMESTAMP.desc())
				.limit(1)
				.fetchSingle().value1();
	}

	public void saveTelegramMessage(int postId) {
		db.insertInto(TELEGRAM_MESSAGES)
				.columns(TELEGRAM_MESSAGES.POST_ID)
				.values(postId)
				.execute();
	}
}
