package main.rss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement(localName = "item")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
class ItemDto {
	@JacksonXmlProperty
	private String guid;
	@JacksonXmlProperty
	private String pubDate;
}
