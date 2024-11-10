package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_FileUpload {

    /**
     * [web]ファイルアップロード
     * 第１引数：アップロードするファイルのフルパス（ローカル）
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }

            String filePath = s3p.s3.getValue1();

            // アップロードするファイルの存在を確認
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                s3p.sr3s.updateError(s3p.sr3, "指定されたファイルが存在しないか、ファイルではありません。ファイルパス=" + filePath);
                return;
            }

            List<WebElement> inputElements = s3p.driver.findElements(By.tagName("input"));
            WebElement element = null;

            for (WebElement inputElement : inputElements) {
                if ("file".equals(inputElement.getAttribute("type"))) {
                    element = inputElement;
                    break;
                }
            }

            if (element == null) {
                s3p.WebUpdateError(s3p.sr3, "ファイル入力要素が見つかりませんでした。");// スクショ付エラー
                return;
            }

            // 要素が表示されるまで待機する
            s3p.wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));

            try {
                // ファイルパスを送信
                element.sendKeys(filePath);
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ファイルアップロードに成功しました。ファイルパス=" + filePath);
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "ファイルアップロードに失敗しました。ファイルパス=" + filePath);// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }
}
