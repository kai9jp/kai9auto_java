package kai9.auto.configuration;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import kai9.auto.model.AppEnv;

@Configuration
public class SeleniumConfig {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean(name = "firefox", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver firefoxDriver() {
        return makeDriver("firefox", false);
    }

    @Bean(name = "edge", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver edgeDriver() {
        return makeDriver("edge", false);
    }

    @Bean(name = "chrome", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver chromeDriver() {
        return makeDriver("chrome", false);
    }

    @Bean(name = "firefox_Headless", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver firefoxDriver_Headless() {
        return makeDriver("firefox", true);
    }

    @Bean(name = "edge_Headless", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver edgeDriver_Headless() {
        return makeDriver("edge", true);
    }

    @Bean(name = "chrome_Headless", destroyMethod = "quit")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    WebDriver chromeDriver_Headless() {
        return makeDriver("chrome", true);
    }

    private WebDriver makeDriver(String type, Boolean IsHeadless) {
        // 環境マスタをロード
        String sql = "select * from app_env_a";
        RowMapper<AppEnv> rowMapper = new BeanPropertyRowMapper<AppEnv>(AppEnv.class);
        List<AppEnv> AppEnvList = jdbcTemplate.query(sql, rowMapper);
        if (AppEnvList.isEmpty()) {
            throw new RuntimeException("環境マスタのロードに失敗しました");
        }
        AppEnv AppEnv = AppEnvList.get(0);

        if (type.equals("edge")) {
            // Seleniumドライバーのパスを設定するためのシステムプロパティを指定する
            System.setProperty("webdriver.edge.driver", AppEnv.getPath_webdriver_edge());
            EdgeOptions options = new EdgeOptions();

            if (IsHeadless) {
                // ヘッドレスモードを有効にする
                options.addArguments("--headless");
            }
            options.setBinary(AppEnv.getPath_binary_edge());
            // 証明書エラーを無視
            options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            // 無効な証明書を許可
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            // クロスオリジンを許可
            options.addArguments("--remote-allow-origins=*");
            return new EdgeDriver(options);
        } else if (type.equals("firefox")) {
            // Seleniumドライバーのパスを設定するためのシステムプロパティを指定する
            System.setProperty("webdriver.gecko.driver", AppEnv.getPath_webdriver_firefox());
            FirefoxOptions options = new FirefoxOptions();
            if (IsHeadless) {
                // ヘッドレスモードを有効にする
                options.addArguments("--headless");
            }
            options.setBinary(AppEnv.getPath_binary_firefox());
            // 証明書エラーを無視
            options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            // 無効な証明書を許可
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            return new FirefoxDriver(options);
        } else if (type.equals("chrome")) {
            // Seleniumドライバーのパスを設定するためのシステムプロパティを指定する
            System.setProperty("webdriver.chrome.driver", AppEnv.getPath_webdriver_chrome());
            ChromeOptions options = new ChromeOptions();
            if (IsHeadless) {
                // ヘッドレスモードを有効にする
                options.addArguments("--headless");
            }
            options.setBinary(AppEnv.getPath_binary_chrome());
            // 証明書エラーを無視
            options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            // 無効な証明書を許可
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            // クロスオリジンを許可
            options.addArguments("--remote-allow-origins=*");
            return new ChromeDriver(options);
        }
        ;
        return null;

    }

}
