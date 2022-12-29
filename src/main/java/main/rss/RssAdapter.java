package main.rss;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

@Component
public class RssAdapter {
	public RssItem convert(ItemDto o) {
		return new RssItem(
				LocalDateTime.parse(o.getPubDate(), RFC_1123_DATE_TIME).plusHours(3),
				Integer.parseInt(o.getGuid().replaceAll("\\D+", ""))
		);
	}

	public List<RssItem> convert(List<ItemDto> dtoObjects) {
		return dtoObjects.stream()
				.map(this::convert)
				.toList();
	}
}
