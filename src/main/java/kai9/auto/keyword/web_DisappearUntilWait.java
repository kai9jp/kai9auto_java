package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_DisappearUntilWait {

    /**
     * [web]消えるまで待機(xpath)
     * 第１引数：ClassName(名前の一部で良い)
     * 第２引数：タイムアウト値(秒)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        Timeouts originalTimeouts = s3p.driver.manage().timeouts(); // デフォルトのタイムアウト値を保存
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            String partialClassName = s3p.s3.getValue1();
            String timeoutValue = s3p.s3.getValue2().trim();

            WebDriverWait customWait;
            if (!timeoutValue.isEmpty()) {
                try {
                    int timeoutInSeconds = Integer.parseInt(timeoutValue);

                    // implicitlyWaitを無効にして、明示的な待機のみにする
                    s3p.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

                    customWait = new WebDriverWait(s3p.driver, Duration.ofSeconds(timeoutInSeconds));
                } catch (NumberFormatException e) {
                    s3p.sr3s.updateError(s3p.sr3, "第２引数は数値で指定してください。");
                    return;
                }
            } else {
                customWait = s3p.wait; // デフォルト値を使用
            }

            String xpath = "//*[contains(@class, '" + partialClassName + "')]";

            try {
                // 新しいWebDriverWaitを設定して、即座に要素が存在するかを確認する
                WebDriverWait immediateWait = new WebDriverWait(s3p.driver, Duration.ofSeconds(0));
                WebElement element = immediateWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

                customWait.until(ExpectedConditions.invisibilityOf(element));

                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "要素が消えるまで待機しました。ClassNameの一部=" + partialClassName);
                return;
            } catch (TimeoutException | NoSuchElementException e) {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "既に対象が存在しないので待機をSKIPしました。ClassNameの一部=" + partialClassName);
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e)); // スクショ付エラー
            return;
        } finally {
            // 必要に応じて、元の暗黙の待機時間に戻す
            s3p.driver.manage().timeouts().implicitlyWait(originalTimeouts.getImplicitWaitTimeout());
        }
    }
}
