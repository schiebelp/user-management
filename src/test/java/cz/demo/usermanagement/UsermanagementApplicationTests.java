package cz.demo.usermanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class UsermanagementApplicationTests extends AbstractIntegrationTest {

	@Autowired
	private ApplicationContext context;

	/**
	 * Explicit context loads
	 *
	 * see: https://www.jvt.me/posts/2021/06/25/spring-context-test/
	 */
	@Test
	void contextLoads() {
		assertThat(context).isNotNull();
	}

}
