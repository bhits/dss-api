package gov.samhsa.c2s.dss;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("default")
@TestPropertySource(properties = {"port: 0"})
public class DssApplicationTests {

    @Test
    public void contextLoads() {
    }
}