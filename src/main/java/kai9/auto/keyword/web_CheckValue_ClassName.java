package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_CheckValue_ClassName {

    /**
     * [web]value値の確認(ClassName)
     * 第１引数：ClassName
     * 第２引数：値
     * 
     * @throws SQLException
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第２引数は省略できません。");
                return;
            }
            String ClassName = s3p.s3.getValue1();
            String value = s3p.s3.getValue2();

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(ClassName)));
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ClassName=" + ClassName);// スクショ付エラー
                return;
            }
            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ClassName=" + ClassName);// スクショ付エラー
                return;
            }

            // 親要素をスクロールして表示する
            WebElement parentElement = element.findElement(By.xpath(".."));
            JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", parentElement);

            // 要素までスクロールする
            executor = (JavascriptExecutor) s3p.driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", element);
            // 要素が表示されるまで待機する
            s3p.wait.until(ExpectedConditions.visibilityOf(element));

            try {
                // 要素に入力された値を取得して、値が正しく入力されたかどうかを確認する
                String text = element.getText();
                if (text.equals(value)) {
                    // 入力値が正しく反映された場合の処理
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "一致しました。値=" + value);
                    return;
                } else {
                    // 入力値が正しく反映されなかった場合の処理
                    s3p.WebUpdateError(s3p.sr3, "一致しませんでした。値=" + value);// スクショ付エラー
                    return;
                }

            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントから値を取得できませんでした。");// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }

}
