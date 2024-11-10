package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_Table_Find_Click {

    /**
     * [web]テーブル検索クリック
     * 第１引数：検索値
     * 第２引数：タイムアウト値（秒）
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
        Timeouts originalTimeouts = s3p.driver.manage().timeouts(); // デフォルトのタイムアウト値を保存
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String value = s3p.s3.getValue1();
            String timeoutValue = s3p.s3.getValue2().trim();
            int timeoutInSeconds = 0;

            if (!timeoutValue.isEmpty()) {
                try {
                    timeoutInSeconds = Integer.parseInt(timeoutValue);
                    s3p.driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(timeoutInSeconds));
                } catch (NumberFormatException e) {
                    s3p.sr3s.updateError(s3p.sr3, "第２引数は数値で指定してください。");
                    return;
                }
            }

            WebElement targetCell = s3p.driver.findElement(By.xpath("//td[text()='" + value + "']"));

            // 要素が存在するかどうかを確認
            if (targetCell != null) {

                // 要素までスクロールする
                JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;
                executor.executeScript("arguments[0].scrollIntoView(true);", targetCell);

                // 要素が表示されるまで待機する
                s3p.wait.until(ExpectedConditions.visibilityOf(targetCell));

                // 上にスクロールしてクリックを試みる
                executor.executeScript("arguments[0].scrollIntoView(true);", targetCell);
                try {
                    // Actionsクラスを使用してクリックを実行
                    Actions actions = new Actions(s3p.driver);
                    actions.moveToElement(targetCell).click().perform();
                    s3p.WebWaitFor();
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "押下に成功しました。ID=" + targetCell);
                    return;
                } catch (TimeoutException | ElementClickInterceptedException e) {
                    // 上でクリックができなかった場合、下にスクロールして再試行
                    executor.executeScript("arguments[0].scrollIntoView(false);", targetCell);
                    try {
                        // Actionsクラスを使用してクリックを実行
                        Actions actions = new Actions(s3p.driver);
                        actions.moveToElement(targetCell).click().perform();
                        s3p.WebWaitFor();
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "押下に成功しました。ID=" + targetCell);
                        return;
                    } catch (TimeoutException | ElementClickInterceptedException ex) {
                        s3p.WebUpdateError(s3p.sr3, "エレメントを押下できませんでした。ID=" + targetCell); // スクショ付エラー
                        return;
                    }
                } catch (Exception e) {
                    // その他の予期しない例外が発生した場合
                    s3p.WebUpdateError(s3p.sr3, "予期しないエラーが発生しました。詳細: " + e.getMessage());
                    return;
                }
            }

        } catch (ElementClickInterceptedException e) {
            // クリックが他の要素によって妨げられた場合の処理
            s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。" + crlf + Kai9Utils.GetException(e));
        } catch (NoSuchElementException ex) {
            // 要素が見つからなかった場合の例外処理
            s3p.WebUpdateError(s3p.sr3, "指定されたエレメントが見つかりませんでした。" + crlf + Kai9Utils.GetException(ex));
        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e)); // スクショ付エラー
            return;
        } finally {
            // デフォルトのタイムアウト設定に戻す
            s3p.driver.manage().timeouts().implicitlyWait(originalTimeouts.getImplicitWaitTimeout());
        }
    }

}
