package main.quartz;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import main.AppState;
import main.HabrClient;
import main.exceptions.HabrHttpException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class WaitingUnlockHttpJob implements Job {
	@Autowired
	private HabrClient habrClient;
	@Autowired
	private AppState appState;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			if (habrClient.isAlive())
				appState.swapAppState();
		} catch (HabrHttpException ignored) {
		}
	}
}
