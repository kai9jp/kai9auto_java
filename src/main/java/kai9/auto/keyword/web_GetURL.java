package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_GetURL {

    /**
     * [web]URL遷移
     * 第１引数：URL
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String url = s3p.s3.getValue1();
            // サイトアクセス
            s3p.driver.get(url);

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サイトアクセスに成功しました。URL=" + url);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
