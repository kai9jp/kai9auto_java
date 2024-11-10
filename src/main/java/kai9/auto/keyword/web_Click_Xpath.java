package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_Click_Xpath {

    /**
     * [web]押下(xpath)
     * 第１引数：xpath
     * 第２引数：動作完了を待つか(省略可) ※第2引数は任意の文字が入っていれば「TRUE」として扱う。FALSEを設定する場合は省略(値を入れない)する事。
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String id = s3p.s3.getValue1();
            Boolean is_wait = Boolean.valueOf(s3p.s3.getValue2());

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(id)));
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id);// スクショ付エラー
                return;
            }
            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id);// スクショ付エラー
                return;
            }

            // 要素までスクロールする
            JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", element);
            // 要素が表示されるまで待機する
            s3p.wait.until(ExpectedConditions.visibilityOf(element));

            try {
                // Actionsクラスを使用してクリックを実行
                Actions actions = new Actions(s3p.driver);
                actions.moveToElement(element).click().perform();

                if (is_wait) s3p.WebWaitFor();
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "押下に成功しました。ID=" + id);
                return;
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを押下できませんでした。ID=" + id); // スクショ付エラー
                return;
            }
        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }

}
