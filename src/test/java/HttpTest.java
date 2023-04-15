import main.HabrClient;

import java.util.LinkedList;
import java.util.List;

public class HttpTest {
	public static void main(String[] args) {
		HabrClient habrClient = new HabrClient();
		List<Integer> postIds = List.of(
				690002,
				690003,
				690006,
				706444,
				690008
		);
		postIds = new LinkedList<>(postIds);
		for (int i = 0; i < 10; i++) {
			postIds.addAll(postIds);
		}
		for (Integer id : postIds) {
			habrClient.isPostHasABBR(id);
		}
	}
}
