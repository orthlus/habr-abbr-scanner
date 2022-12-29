import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import main.rss.RssFeed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RssParseTest {
	public static void main(String[] args) throws IOException {
		XmlMapper xmlMapper = new XmlMapper();

//		CloseableHttpClient httpClient = HttpClients.createDefault();
//		CloseableHttpResponse response = httpClient.execute(RequestBuilder.get("https://habr.com/ru/rss/all/all/?fl=ru").build());
//		String xmlStr = EntityUtils.toString(response.getEntity());
		String xmlStr = Files.readString(Path.of(""));

		RssFeed rssFeed = xmlMapper.readValue(xmlStr, RssFeed.class);
		System.out.println(rssFeed.getPosts());
	}
}
