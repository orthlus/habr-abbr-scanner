package main;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpDelayInterceptor implements Interceptor {
	private final int delay;

	public HttpDelayInterceptor(int delay) {
		this.delay = delay;
	}

	@NotNull
	@Override
	public Response intercept(@NotNull Chain chain) throws IOException {
		try {
			TimeUnit.SECONDS.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return chain.proceed(chain.request());
	}
}
