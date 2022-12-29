package main;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.exceptions.HabrHttpException;
import main.exceptions.PageAccessDeniedException;
import main.exceptions.PageNotFoundException;
import main.rss.RssAdapter;
import main.rss.RssFeed;
import main.rss.RssItem;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HabrClient {
	private CloseableHttpClient httpClient;
	private final RequestConfig requestConfig;

	private int countReconnects = 0;

	private final XmlMapper xmlMapper = new XmlMapper();
	@Autowired
	private RssAdapter rssAdapter;

	{
		int timeout = 2000;
		requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		initHttpClient();
	}

	private void initHttpClient() {
		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.build();
	}

	private void reconnect() {
		initHttpClient();
		countReconnects++;
	}

	private CloseableHttpResponse execute(RequestBuilder requestBuilder) throws IOException, HabrHttpException {
		sleepSeconds(1);
		URI uri = requestBuilder.getUri();
		log.info("start request {}", uri);
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(requestBuilder.build());
		} catch (ConnectionPoolTimeoutException e) {
			if (countReconnects > 3) {
				countReconnects = 0;
				throw new HabrHttpException();
			} else reconnect();

			return execute(requestBuilder);
		}
		countReconnects = 0;
		log.info("request {}, response code {}", uri, getStatusCode(response));
		return response;
	}

	public boolean isAlive() throws HabrHttpException {
		String url = "https://habr.com/ru/all";
		RequestBuilder request = RequestBuilder.get(url);
		try {
			CloseableHttpResponse response = execute(request);
			return getStatusCode(response) == 200;
		} catch (IOException e) {
			log.error("http error - HabrClient.isAlive", e);
			return false;
		}
	}

	public List<RssItem> getLastPostsFromRss() throws HabrHttpException {
		String url = "https://habr.com/ru/rss/all/all/?fl=ru";
		RequestBuilder request = RequestBuilder.get(url);
		try {
			CloseableHttpResponse response = execute(request);
			RssFeed feed = xmlMapper.readValue(textFromResponse(response), RssFeed.class);
			return rssAdapter.convert(feed.getPosts());
		} catch (IOException e) {
			log.error("http error - HabrClient.getLastPostsIds", e);
			return List.of();
		}
	}

	public int getMaxPostIdFromRss() throws HabrHttpException {
		return getLastPostsFromRss().stream()
				.map(RssItem::getPostId)
				.mapToInt(Integer::intValue)
				.max().orElse(0);
	}

	@Deprecated
	public int getMaxPostIdFromMainPage() throws HabrHttpException {
		String pattern = "<article id=\"\\d+\"";
		String url = "https://habr.com/ru/all/";
		RequestBuilder request = RequestBuilder.get(url);
		try {
			CloseableHttpResponse response = execute(request);
			String pageContent = textFromResponse(response);
			return Pattern.compile(pattern).matcher(pageContent).results()
					.map(MatchResult::group)
					.map(html -> html
							.replace("<article id=\"", "")
							.replace("\"", ""))
					.mapToInt(Integer::valueOf)
					.max().orElse(0);
		} catch (IOException e) {
			log.error("http error - HabrClient.getMaxPostIdFromMainPage", e);
			return 0;
		}
	}

	public boolean isPostHasABBR(int postId) throws PageNotFoundException, HabrHttpException, PageAccessDeniedException {
		String url = "https://habr.com/ru/post/%d/".formatted(postId);
		try {
			RequestBuilder request = RequestBuilder.get(url);
			CloseableHttpResponse response = execute(request);
			if (getStatusCode(response) == 404) {
				log.info("Page {} not found", postId);
				throw new PageNotFoundException();
			}
			if (getStatusCode(response) == 403) {
				log.info("Page {} access denied", postId);
				throw new PageAccessDeniedException();
			}
			if (getStatusCode(response) != 200) {
				log.info("Page {} getting error, code {}", postId, getStatusCode(response));
				throw new IOException();
			}
			String pageContent = textFromResponse(response);
			return pageContent.contains("class=\"habraabbr\"");
		} catch (IOException e) {
			log.error("http error - HabrClient.isPostHasABBR", e);
			return false;
		}
	}

	private String textFromResponse(CloseableHttpResponse response) throws IOException {
		return EntityUtils.toString(response.getEntity());
	}

	private int getStatusCode(CloseableHttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}

	private void sleepSeconds(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
