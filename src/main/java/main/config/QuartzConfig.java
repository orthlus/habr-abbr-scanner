package main.config;

import main.quartz.ScanJob;
import main.quartz.WaitingUnlockHttpJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForever;

@Configuration
public class QuartzConfig {
	@Value("${habr.minutes_to_wait_unlock_habr_http}")
	private int minutesToWaitUnlockHabrHttp;
	@Bean
	public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
		TriggerKey scanJob = TriggerKey.triggerKey(ScanJob.class.getName());
		JobDetail scanJobDetail = JobBuilder.newJob()
				.storeDurably()
				.ofType(ScanJob.class)
				.build();
		SimpleTrigger scanTrigger = TriggerBuilder.newTrigger()
				.withSchedule(repeatHourlyForever())
				.withIdentity(scanJob)
				.build();


		TriggerKey unlockHttpJob = TriggerKey.triggerKey(WaitingUnlockHttpJob.class.getName());
		JobDetail waitingUnlockHttpJobDetail = JobBuilder.newJob()
				.storeDurably()
				.ofType(WaitingUnlockHttpJob.class)
				.build();
		SimpleTrigger waitingUnlockHttpTrigger = TriggerBuilder.newTrigger()
				.withSchedule(repeatMinutelyForever(minutesToWaitUnlockHabrHttp))
				.withIdentity(unlockHttpJob)
				.build();

		Scheduler scheduler = factory.getScheduler();
		scheduler.scheduleJob(scanJobDetail, scanTrigger);
		scheduler.scheduleJob(waitingUnlockHttpJobDetail, waitingUnlockHttpTrigger);
		scheduler.start();

		scheduler.pauseTrigger(unlockHttpJob);
		return scheduler;
	}
}
