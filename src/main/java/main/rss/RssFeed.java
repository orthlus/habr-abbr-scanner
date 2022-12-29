package main.rss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "rss")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class RssFeed {
	@JacksonXmlProperty(localName = "channel")
	private ChannelDto channelDto;

	public List<ItemDto> getPosts() {
		return channelDto.itemDtos;
	}
}