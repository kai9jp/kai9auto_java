package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_SetSelect_Id {

    /**
     * [web]セレクトボックス選択(id)
     * 第１引数：id
     * 第２引数：選択したい値
     * ※数字を【】で囲んで書く事でN番目の要素として動作する
     * 例)【1】は1番目の要素
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
            String target = s3p.s3.getValue2();

            WebElement element = null;
            try {
                element = s3p.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
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
                Select select = new Select(element);
                // 選択する
                // 数字が【】で囲まれているかをチェック
                if (target.matches("【\\d+】")) {
                    // 【】を取り除き、数字をインデックスとして使用
                    int index = Integer.parseInt(target.replaceAll("[^\\d]", ""));
                    select.selectByIndex(index);
                    // 選択されたオプションをインデックスで取得
                    WebElement selectedOption = select.getFirstSelectedOption();
                    String selectedOptionText = selectedOption.getText();
                    // 選択されたオプションのインデックスを確認
                    if (select.getOptions().indexOf(selectedOption) == index) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択に成功しました。インデックス=" + index + "、値=" + selectedOptionText);
                        return;
                    } else {
                        s3p.WebUpdateError(s3p.sr3, "選択できませんでした。インデックス=" + index);// スクショ付エラー
                        return;
                    }
                } else {
                    // それ以外の場合、visible textで選択
                    select.selectByVisibleText(target);
                    // 選択されたオプションを取得する
                    WebElement selectedOption = select.getFirstSelectedOption();
                    // 選択されたオプションのテキストを取得する
                    String selectedOptionText = selectedOption.getText();
                    // 選択されたオプションが想定通りであるかを確認する
                    if (selectedOptionText.equals(target)) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "選択に成功しました。値=" + target);
                        return;
                    } else {
                        s3p.WebUpdateError(s3p.sr3, "選択できませんでした。値=" + target);// スクショ付エラー
                        return;
                    }
                }
            } catch (TimeoutException e) {
                s3p.WebUpdateError(s3p.sr3, "選択できませんでした。値=" + target);// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }
    }

}
