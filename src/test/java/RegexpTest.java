import main.HabrClient;
import main.exceptions.HabrHttpException;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class RegexpTest {
	public static void main(String[] args) throws HabrHttpException {
		String pageContent = "";
		String pattern = "<article id=\"\\d+\"";
		int strings = Pattern.compile(pattern).matcher(pageContent).results()
				.map(MatchResult::group)
				.map(html -> html
						.replace("<article id=\"", "")
						.replace("\"", ""))
				.mapToInt(Integer::valueOf)
				.max().orElse(0);
		System.out.println(new HabrClient().getMaxPostIdFromRss());
		System.out.println(strings);
	}
}
