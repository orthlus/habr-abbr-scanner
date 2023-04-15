package main;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import main.exceptions.HabrHttpException;
import main.rss.RssAdapter;
import main.rss.RssFeed;
import main.rss.RssItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HabrClient {
	@Autowired
	private RssAdapter rssAdapter;

	private OkHttpClient client = new OkHttpClient.Builder()
			.callTimeout(10, TimeUnit.SECONDS)
			.addInterceptor(new HttpDelayInterceptor(10))
			.build();
	private XmlMapper xmlMapper = new XmlMapper();

	public List<RssItem> getLastPostsFromRss() {
		String url = "https://habr.com/ru/rss/all/all/?fl=ru";
		Request request = new Request.Builder().get().url(url).build();
		try (Response response = call(request)) {
			String text = text(response);
			RssFeed feed = xmlMapper.readValue(text, RssFeed.class);
			return rssAdapter.convert(feed.getPosts());
		} catch (IOException e) {
			log.error("http error - HabrClient.getLastPostsIds", e);
			return List.of();
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private String text(Response response) {
		try {
			return response.body().string();
		} catch (IOException | NullPointerException e) {
			log.error("error getting body of response {}", response);
			throw new RuntimeException(e);
		}
	}

	private Response call(Request request) {
		try {
			return client.newCall(request).execute();
		} catch (IOException e) {
			log.error("http error by request {} - {}", request, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public int getMaxPostIdFromRss() throws HabrHttpException {
		return getLastPostsFromRss().stream()
				.map(RssItem::getPostId)
				.mapToInt(Integer::intValue)
				.max().orElse(0);
	}

	@Deprecated
	public int getMaxPostIdFromMainPage() {
		String pattern = "<article id=\"\\d+\"";
		String url = "https://habr.com/ru/all/";
		Request request = new Request.Builder().get().url(url).build();
		try (Response response = call(request)) {
			String text = text(response);
			return Pattern.compile(pattern).matcher(text).results()
					.map(MatchResult::group)
					.map(html -> html
							.replace("<article id=\"", "")
							.replace("\"", ""))
					.mapToInt(Integer::valueOf)
					.max().orElse(0);
		}
	}

	public boolean isPostHasABBR(int postId) {
		String url = "https://habr.com/ru/post/%d/".formatted(postId);
		Request request = new Request.Builder().get().url(url).build();
		try (Response response = call(request)) {
			int code = response.code();
			if (code == 404 || code == 403) {
				log.info("Page {} code {}", postId, code);
				return false;
			}
			if (code != 200) {
				log.info("Page {} getting error, code {}", postId, code);
				return false;
			}
			String text = text(response);
			return text.contains("class=\"habraabbr\"");
		}
	}
}
