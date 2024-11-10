package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_SetWebDriver {

    @Autowired
    private ApplicationContext context;// メモリ解放不要

    // タイムアウト秒数
    public static final int TIMEOUT_SECONDS = 30;

    /**
     * [web]ブラウザ起動
     * 
     * 第１引数：ドライバファイル名
     * 第２引数：何か文字が入っていればTRUE(ヘッドレスモードで実行)
     * 第３引数：何か文字が入っていればTRUE(想定外の場合に自動でスナップショットを取得する)
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            s3p.IsScreenShot = !s3p.s3.getValue3().isEmpty();
            Boolean IsHeadless = !s3p.s3.getValue2().isEmpty();

            String driverFileName = s3p.s3.getValue1();
            if (driverFileName.equals("edge")) {
                if (IsHeadless) {
                    s3p.driver = context.getBean("edge_Headless", WebDriver.class);
                } else {
                    s3p.driver = context.getBean("edge", WebDriver.class);
                }
            } else if (driverFileName.equals("firefox")) {
                if (IsHeadless) {
                    s3p.driver = context.getBean("firefox_Headless", WebDriver.class);
                } else {
                    s3p.driver = context.getBean("firefox", WebDriver.class);
                }
            } else if (driverFileName.equals("chrome")) {
                if (IsHeadless) {
                    s3p.driver = context.getBean("chrome_Headless", WebDriver.class);
                } else {
                    s3p.driver = context.getBean("chrome", WebDriver.class);
                }
            } else {
                s3p.sr3s.updateError(s3p.sr3, "無効なWEBドライバ指定です。edge/firefox/chromeから選択して下さい。指定ドライバ名=" + driverFileName);
                return;
            }
            ;

            // webドライバをプロトタイプインスタンス(使う時だけビーン登録する術)として作成
            s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(TIMEOUT_SECONDS));

            String crlf = System.lineSeparator();
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "WEBドライバを起動しました" + crlf + "SQL=" + driverFileName);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
