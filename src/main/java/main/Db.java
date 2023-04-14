package main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static main.Tables.LAST_SCAN_POST;

@Slf4j
@Component
@RequiredArgsConstructor
public class Db {
	private final DSLContext db;

	public int getLastScannedPostId() {
		return db.select(LAST_SCAN_POST.ID)
				.from(LAST_SCAN_POST)
				.limit(1)
				.fetchSingle().value1();
	}

	public void updateLastScanned(int lastId) {
		db.update(LAST_SCAN_POST)
				.set(LAST_SCAN_POST.ID, lastId)
				.execute();
	}
}
