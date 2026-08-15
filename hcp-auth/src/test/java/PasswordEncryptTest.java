import com.hcp.common.security.utils.SecurityUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * <p>
 * PasswordEncryptTest
 * </p>
 * 密码加密测试类
 * 用于生成 BCrypt 加密密码，可直接用于数据库插入
 * @author JHan
 * @Date 2026-08-09 17:56
 */
@SpringBootTest
public class PasswordEncryptTest {

    @Test
    public void getBCryptPassword() {
        String password = "Abc@123456";  // 改成你想要的密码
        String encrypted = SecurityUtils.encryptPassword(password);
        System.out.println("明文: " + password);
        System.out.println("密文: " + encrypted); // $2a$10$PbXHW3ubCcUWXkpWAjkceeqUlspAgmije5saycceb2W7i/tgrkAka
    }

    @Test
    public void checkoutPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbPassword = "$2a$10$PbXHW3ubCcUWXkpWAjkceeqUlspAgmije5saycceb2W7i/tgrkAka";  // 数据库中的密码
        String inputPassword = "Abc@123456";  // 用户输入的密码

        boolean matches = encoder.matches(inputPassword, dbPassword);
        System.out.println("密码是否正确: " + matches);  // 输出: true
    }


}
