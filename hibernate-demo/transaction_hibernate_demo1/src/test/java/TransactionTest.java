import com.demo.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by lorne on 2017/6/7.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "/applicationContext_hibernate_mysql.xml", "/applicationContext_dubbo_consumer.xml"})
public class TransactionTest {


    @Autowired
    private TestService testService;

    @Test
    public void test(){
        String name = testService.hello();
        System.out.println("res:"+name);
    }

}
