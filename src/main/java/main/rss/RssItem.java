package main.rss;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RssItem {
	private LocalDateTime publishDateTime;
	private int postId;
}
