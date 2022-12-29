package main.rss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "channel")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
class ChannelDto {
	@JacksonXmlProperty(localName = "item")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<ItemDto> itemDtos;
}
