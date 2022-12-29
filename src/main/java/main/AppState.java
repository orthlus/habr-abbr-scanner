package main;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.quartz.RareScanJob;
import main.quartz.ScanJob;
import main.quartz.WaitingUnlockHttpJob;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppState {
	@Value("${habr.minutes_to_wait_unlock_habr_http}")
	private int minutesToWaitUnlockHabrHttp;
	@Autowired
	private Scheduler scheduler;
	private final TriggerKey unlockHttpJob = TriggerKey.triggerKey(WaitingUnlockHttpJob.class.getName());
	private final TriggerKey scanJob = TriggerKey.triggerKey(ScanJob.class.getName());
	private final TriggerKey rareScanJob = TriggerKey.triggerKey(RareScanJob.class.getName());

	public void swapAppState() {
		try {
			if (isTriggerPaused(scanJob) && isTriggerPaused(rareScanJob) && isTriggerWorking(unlockHttpJob)) {
				scheduler.pauseTrigger(unlockHttpJob);
				scheduler.resumeTrigger(scanJob);
				scheduler.resumeTrigger(rareScanJob);
				log.info("Finish sleep to unlock http to habr");
			} else {
				scheduler.pauseTrigger(scanJob);
				scheduler.pauseTrigger(rareScanJob);
				scheduler.resumeTrigger(unlockHttpJob);
				log.info("Start sleep {} minutes for unlock http to habr", minutesToWaitUnlockHabrHttp);
			}
		} catch (SchedulerException ex) {
			log.error("Error swap app state");
			throw new RuntimeException(ex);
		}
	}

	private boolean isTriggerWorking(TriggerKey triggerKey) throws SchedulerException {
		return scheduler.getTriggerState(triggerKey).equals(Trigger.TriggerState.NORMAL);
	}

	private boolean isTriggerPaused(TriggerKey triggerKey) throws SchedulerException {
		return scheduler.getTriggerState(triggerKey).equals(Trigger.TriggerState.PAUSED);
	}
}
