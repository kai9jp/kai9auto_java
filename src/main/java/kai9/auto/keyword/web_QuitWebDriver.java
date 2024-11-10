package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_QuitWebDriver {

    /**
     * [web]ブラウザ終了
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.driver != null) {
                s3p.driver.quit();
                s3p.driver = null;
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "WEBドライバを停止しました");
                return;
            } else {
                s3p.sr3s.updateError(s3p.sr3, "WEBドライバを停止できませんでした");
                return;
            }
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
