package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_GetURL {

    /**
     * [web]URL遷移とコンテンツ確認
     * 第１引数：URL
     * 第２引数：確認する文字列または画像の要素識別子
     * 第３引数：タイムアウト値（秒）
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第２引数は省略できません（確認する文字列または画像の識別子）。");
                return;
            }

            // デフォルトのタイムアウト値（省略時用）
            int timeoutSeconds = web_SetWebDriver.TIMEOUT_SECONDS;
            if (!s3p.s3.getValue3().trim().isEmpty()) {
                try {
                    timeoutSeconds = Integer.parseInt(s3p.s3.getValue3().trim()); // タイムアウト値（秒）
                } catch (Exception e) {
                    s3p.sr3s.updateError(s3p.sr3, "第３引数の値が不正です（タイムアウト値）。値=" + s3p.s3.getValue3());
                    return;
                }
            }

            String url = s3p.s3.getValue1();
            String target = s3p.s3.getValue2(); // 確認する文字列または要素の識別子

            long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

            while (System.currentTimeMillis() < endTime) {
                try {
                    // サイトアクセス
                    s3p.driver.get(url);

                    // 指定された要素が存在するかを確認
                    boolean isFound = checkElementExists(s3p.driver, target);

                    if (isFound) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サイトアクセスに成功し、指定の要素が見つかりました。URL=" + url + ", ターゲット=" + target);
                        return;
                    } else {
                        Thread.sleep(1000); // 1秒待機してリトライ
                    }
                } catch (Exception e) {
                    // アクセスエラーやチェックエラーが発生してもリトライを続行
                    Thread.sleep(1000); // 1秒待機してリトライ
                }
            }

            // タイムアウト
            s3p.WebUpdateError(s3p.sr3, "タイムアウトに達しましたが、指定の要素が見つかりませんでした。URL=" + url + ", ターゲット=" + target);
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
        }
    }

    /**
     * 指定された文字列や画像が存在するか確認する
     * 
     * @param driver WebDriver
     * @param target 確認する文字列または画像の要素識別子
     * @return 存在すればtrue、そうでなければfalse
     */
    private boolean checkElementExists(WebDriver driver, String target) {
        try {
            // 指定がCSSセレクタの場合（例：#idや.class名など）
            // CSSセレクタとして解釈可能な場合（例：#idや.class名など）
            try {
                if (driver.findElements(By.cssSelector(target)).size() > 0) {
                    return true;
                }
            } catch (InvalidSelectorException e) {
                // CSSセレクタとして解釈できない場合はスキップ
            }

            // 指定が文字列の場合
            // ページ内の全ての要素を取得
            List<WebElement> allElements = driver.findElements(By.xpath("//*"));
            // 各要素のテキストを調べる
            for (WebElement element : allElements) {
                if (element.isDisplayed() && element.getText().contains(target)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            // エラー時はfalseを返す
            return false;
        }
    }

}
