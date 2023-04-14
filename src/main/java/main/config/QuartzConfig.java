package main.config;

import main.quartz.ScanJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;

@Configuration
public class QuartzConfig {
	@Bean
	public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
		TriggerKey scanJob = TriggerKey.triggerKey(ScanJob.class.getName());
		JobDetail scanJobDetail = JobBuilder.newJob()
				.storeDurably()
				.ofType(ScanJob.class)
				.build();
		SimpleTrigger scanTrigger = TriggerBuilder.newTrigger()
				.withSchedule(repeatHourlyForever(2))
				.withIdentity(scanJob)
				.build();

		Scheduler scheduler = factory.getScheduler();
		scheduler.scheduleJob(scanJobDetail, scanTrigger);
		scheduler.start();
		return scheduler;
	}
}
