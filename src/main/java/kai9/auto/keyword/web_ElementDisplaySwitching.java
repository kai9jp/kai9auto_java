package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_ElementDisplaySwitching {

    /**
     * [web]エレメント表示切替(id)
     * 第１引数：id
     * 第２引数：表示/非表示
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty() || s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数および第２引数は省略できません。");
                return;
            }

            String id = s3p.s3.getValue1();
            String displayState = s3p.s3.getValue2();

            JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;

            // JavaScriptを使って非表示要素を含むエレメントを取得
            WebElement element = (WebElement) executor.executeScript(
                    "return document.getElementById(arguments[0]);", id);

            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id); // スクショ付エラー
                return;
            }

            // エレメントの表示/非表示を切り替える
            if (displayState.equalsIgnoreCase("表示")) {
                executor.executeScript("arguments[0].style.display='block';", element);
            } else if (displayState.equalsIgnoreCase("非表示")) {
                executor.executeScript("arguments[0].style.display='none';", element);
            } else {
                s3p.sr3s.updateError(s3p.sr3, "第２引数には「表示」または「非表示」を指定してください。");
                return;
            }

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "表示切替に成功しました。ID=" + id);
            return;

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e)); // スクショ付エラー
            return;
        }
    }
}
