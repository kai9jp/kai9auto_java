package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_element_is_exist_ID {

    /**
     * [web]エレメント存在確認(id)
     * 第１引数：id
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String id = s3p.s3.getValue1();
            String value = s3p.s3.getValue2();

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id);
                return;
            }
            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id);
                return;
            }

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "エレメントの存在を確認しました。値=" + value);
            return;

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }
}
