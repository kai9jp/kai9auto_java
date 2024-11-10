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
public class web_SetCheckBox_Id {

    /**
     * [web][web]チェックボックス選択(id)
     * 第１引数：id
     * 第２引数：選択したい値(true 又は false)
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
            String id = s3p.s3.getValue1();
            boolean IsTrue = s3p.s3.getValue2().trim().toLowerCase().equals("true");

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "エレメントを取得できませんでした。ID=" + id);// スクショ付エラー
                return;
            }

            // 要素までスクロールする
            JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", element);
            // 要素が表示されるまで待機する
            s3p.wait.until(ExpectedConditions.visibilityOf(element));

            try {
                if (IsTrue) {
                    // チェックを入れる
                    if (!element.isSelected()) {
                        element.click();

                        // チェックが入っていることを確認する
                        if (element.isSelected()) {
                            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択に成功しました。値=" + IsTrue);
                        } else {
                            s3p.WebUpdateError(s3p.sr3, "選択できませんでした。");// スクショ付エラー
                        }
                    } else {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択に成功しました。値=" + IsTrue);
                    }
                } else {
                    // チェックを外す
                    if (element.isSelected()) {
                        element.click();
                        // チェックが外れていることを確認する
                        if (!element.isSelected()) {
                            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択解除に成功しました。値=" + IsTrue);
                        } else {
                            s3p.WebUpdateError(s3p.sr3, "選択解除できませんでした。");// スクショ付エラー
                        }
                    } else {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択解除に成功しました。値=" + IsTrue);
                    }
                }

            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "選択できませんでした。");// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }

}
