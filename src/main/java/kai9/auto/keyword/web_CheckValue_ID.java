package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_CheckValue_ID {

    /**
     * [web]value値の確認(id)
     * 第１引数：id
     * 第２引数：値
     * 第３引数：検索条件(完全一致/含まれる/正規表現)
     * 第3引数にカンマ区切りで「タイムアウト値」(省略可) ※秒で指定
     * 例)正規表現,5
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();// 改行コード
        int timeOut = 0;
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第２引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第３引数は省略できません。");
                return;
            }
            String id = s3p.s3.getValue1();
            String value = s3p.s3.getValue2();
            String[] thirdArg = s3p.s3.getValue3().split(",", 2);

            // 第3引数の解析
            String searchCondition = thirdArg[0];
            timeOut = 30; // デフォルトのタイムアウト値（省略時）
            if (thirdArg.length > 1) {
                try {
                    timeOut = Integer.parseInt(thirdArg[1].trim());
                    // 第3引数にタイムアウト値がセットされている場合は変更する
                    s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(timeOut));
                } catch (Exception e) {
                    s3p.sr3s.updateError(s3p.sr3, "第３引数のタイムアウト値が不正です。");
                }
            }

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。id=" + id);// スクショ付エラー
                return;
            }
            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。id=" + id);// スクショ付エラー
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
                String text = "";
                text = element.getAttribute("value");// "input"用
                if (text == null) {
                    text = element.getText();// "textarea" 用
                }

                // 検索条件に基づいた処理
                boolean isMatch = false;
                switch (searchCondition) {
                case "完全一致":
                    // 検索文字に制御文字が入る場合を考慮しPattern.quoteでエスケープする
                    isMatch = text.equals(value);
                    break;
                case "含まれる":
                    // 検索文字に制御文字が入る場合を考慮しPattern.quoteでエスケープする
                    isMatch = text.contains(value);
                    break;
                case "正規表現":
                    isMatch = text.matches(value);
                    break;
                default:
                    s3p.sr3s.updateError(s3p.sr3, "無効な検索条件です。");
                    return;
                }

                if (isMatch) {
                    // 入力値が正しく反映された場合の処理
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "一致しました。値=" + value);
                    return;
                } else {
                    // 入力値が正しく反映されなかった場合の処理
                    s3p.WebUpdateError(s3p.sr3, "一致しませんでした。値=" + value + crlf + "表示内容=" + text);// スクショ付エラー
                    return;
                }

            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントから値を取得できませんでした。");// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        } finally {
            if (timeOut != 0) {
                // タイムアウト値が変更されている場合は戻す
                s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(web_SetWebDriver.TIMEOUT_SECONDS));
            }
        }

    }

}
