package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_CheckValue {

    /**
     * [web]value値の確認(全エレメント)
     * 第１引数：値
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String value = s3p.s3.getValue1();

            // ページ内の全ての要素を取得
            List<WebElement> allElements = s3p.driver.findElements(By.xpath("//*"));

            // 各要素のテキストを調べる
            for (WebElement element : allElements) {
                if (element.isDisplayed() && element.getText().contains(value)) {
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "一致しました。値=" + value);
                    return;
                }
            }

            // 入力値が正しく反映されなかった場合の処理
            s3p.WebUpdateError(s3p.sr3, "一致しませんでした。値=" + value);// スクショ付エラー
            return;

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }

}
