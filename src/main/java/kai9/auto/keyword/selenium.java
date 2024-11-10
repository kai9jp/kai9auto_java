package kai9.auto.keyword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Base64;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class selenium {

    @Autowired
    private ApplicationContext context;// メモリ解放不要

    // 初期段階のテスト用に作ったクラス、実務利用は無いので、削除してもOK

    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        // webドライバをプロトタイプインスタンス(使う時だけビーン登録する術)として作成
//    	WebDriver driver = context.getBean("firefox", WebDriver.class);
        WebDriver driver = context.getBean("edge", WebDriver.class);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            // サイトアクセス
            driver.get("https://kai9.com:3000/");
            // ログイン
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input_email")));
            WebElement passwordInput = driver.findElement(By.id("input_password"));
            WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
            emailInput.sendKeys("z");
            passwordInput.sendKeys("z");
            Actions actions = new Actions(s3p.driver);
            actions.moveToElement(submitButton).click().perform();

            // ログインに成功し左メニューが出るまで待機
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("collapseMenu")));

            // 処理設定にアクセス
            driver.get("https://kai9.com:3000/syori1");
            // 新規登録ボタン押下
            WebElement addButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".btn-success .fa-plus")));
            actions.moveToElement(addButton).click().perform();

            // ファイルアップロード
            String filePath = "G:\\OneDrive\\work\\25.java\\06.kai9auto\\文書\\06.テスト\\01.処理シナリオ\\0001_処理シナリオ_単体テスト.xlsx";
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
            File file = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }

            // JavaScript を使用してファイル参照ボタンを出す(無理やり出す)
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            String script = "document.querySelector('input[type=\"file\"]').style.display = 'block';";
            jsExecutor.executeScript(script);

            // ファイルアップロード
            WebElement fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='file']")));
            fileInput.sendKeys(file.getAbsolutePath());

            // 登録ボタン押下
            WebElement uploadButton = driver.findElement(By.cssSelector("button[type='submit']"));
            jsExecutor.executeScript("arguments[0].scrollIntoView(true);", uploadButton);// 要素が表示されるまでスクロール
            actions.moveToElement(uploadButton).click().perform();

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "テスト正常終了");
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
            driver.quit();
        }
    }

}
