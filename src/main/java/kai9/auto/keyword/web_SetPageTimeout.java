package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import org.json.JSONException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_SetPageTimeout {

    /**
     * [web]ページロートタイムアウト設定(秒)
     * 第１引数：タイムアウト値(秒)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            Integer time = Integer.valueOf(s3p.s3.getValue1());
            // サイトアクセス
            s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(time));

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "タイムアウト値を設定しました。タイムアウト値(秒)=" + time);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
