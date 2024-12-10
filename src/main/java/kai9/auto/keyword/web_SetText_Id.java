package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

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
public class web_SetText_Id {

    /**
     * [web]テキスト入力(id)
     * 第１引数：id
     * 第２引数：値
     * 第３引数：タイムアウト値(省略可)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        int timeOut = 0;
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
//    		if (s3p.s3.getValue2().trim().isEmpty()) {
//				s3p.sr3s.updateError(s3p.sr3,"第２引数は省略できません。");
//				return;
//    		}
            String id = s3p.s3.getValue1();
            String value = s3p.s3.getValue2();
            
            
            // 第3引数の解析
            String timeOutSeconds = s3p.s3.getValue3().trim();
            timeOut = web_SetWebDriver.TIMEOUT_SECONDS; // デフォルトのタイムアウト値（省略時）
            if (!timeOutSeconds.isEmpty()) {
                try {
                    timeOut = Integer.parseInt(timeOutSeconds.trim());
                    // 第3引数にタイムアウト値がセットされている場合は変更する
                    s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(timeOut));
                } catch (Exception e) {
                    s3p.sr3s.updateError(s3p.sr3, "第３引数のタイムアウト値が不正です。");
                    return;
                }
            }

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

            JavascriptExecutor executor = (JavascriptExecutor) s3p.driver;
            // 要素までスクロールする
            if (!element.isDisplayed()) {
                executor.executeScript("arguments[0].scrollIntoView(true);", element);
                // 要素が表示されるまで待機する
                s3p.wait.until(ExpectedConditions.visibilityOf(element));
            }

            // inputtype「date」または「datetime-local」にsendkeyする際は、特定のフォーマットにしないと失敗するので補正する
            if (element.getAttribute("type").equals("date") || element.getAttribute("type").equals("datetime-local")) {
                try {
                    value = parseAndFormatDate(value, element.getAttribute("type"));
                } catch (IllegalArgumentException e) {
                    s3p.WebUpdateError(s3p.sr3, e.getMessage()); // スクショ付エラー
                    return;
                }
            }

            try {
                WebElement targetElement = s3p.wait.until(ExpectedConditions.elementToBeClickable(element));
                targetElement.clear(); // 既存の値をクリア

                // JavaScriptを使って直接値を設定
                // 数値型のインプットで、初期値0をClear出来ず、1が01となる事象があるので、その対策
                // 数値型で、BigInt最大値付近の大きな数を上手く処理できない事象があるので、その対策
                String script = "if (arguments[0] instanceof HTMLInputElement) {" +
                        // 数値型のインプットで、初期値0をClear出来ず、1が01となる事象があるので、その対策
                        // 数値型で、BigInt最大値付近の大きな数を上手く処理できない事象があるので、その対策
                        "var valueToSet = typeof arguments[1] === 'bigint' ? arguments[1].toString() : arguments[1];" +
                        // ブラウザ標準のvalue設定メソッドを取得
                        "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        // ブラウザ標準の設定メソッドを使用して、ターゲット要素の値を設定
                        "nativeInputValueSetter.call(arguments[0], valueToSet);" +
                        "} else if (arguments[0] instanceof HTMLTextAreaElement) {" +
                        // ブラウザ標準のvalue設定メソッドを取得
                        "var nativeTextAreaValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;" +
                        // ブラウザ標準の設定メソッドを使用して、ターゲット要素の値を設定
                        "nativeTextAreaValueSetter.call(arguments[0], arguments[1]);" +
                        "}" +
                        // inputイベントを手動で発火させて、Reactや他のライブラリが変更を検知できるようにする
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));";

                    // DOM 操作前に BigInt を文字列に変換して設定
                    executor.executeScript(script, targetElement, value.toString());

                String inputValue = element.getAttribute("value");
                if (inputValue.equals(value)) {
                    if (element.getAttribute("type").equals("date") || element.getAttribute("type").equals("datetime-local")) {
                        // 日付/日時の場合、フォーマットしてしまっているので、元の値を再代入
                        value = s3p.s3.getValue2();
                    }
                    // 入力値が正しく反映された場合の処理
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "(JavaScript)入力に成功しました。値=" + value);
                    return;
                } else {
                    if (element.getAttribute("type").equals("date") || element.getAttribute("type").equals("datetime-local")) {
                        // 日付/日時の場合、フォーマットしてしまっているので、元の値を再代入
                        value = s3p.s3.getValue2();
                    }
                    // 入力値が正しく反映されなかった場合の処理
                    s3p.WebUpdateError(s3p.sr3, "入力に失敗しました。値=" + value);// スクショ付エラー
                    return;
                }

            } catch (TimeoutException e) {
                if (element.getAttribute("type").equals("date") || element.getAttribute("type").equals("datetime-local")) {
                    // 日付/日時の場合、フォーマットしてしまっているので、元の値を再代入
                    value = s3p.s3.getValue2();
                }
                s3p.WebUpdateError(s3p.sr3, "入力に失敗しました。値=" + value);// スクショ付エラー
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));// スクショ付エラー
            return;
        }finally {
            if (timeOut != 0) {
                // タイムアウト値が変更されている場合は戻す
                s3p.wait = new WebDriverWait(s3p.driver, Duration.ofSeconds(web_SetWebDriver.TIMEOUT_SECONDS));
            }
        }
    }

    /**
     * 日付または日時の文字列を解析してフォーマットする
     *
     * この関数は、指定された日付または日時の文字列を、事前定義された日付および日時のフォーマットリストを使用して解析する。
     * 文字列が正常に解析されると、elementTypeが"date"の場合は"yyyy-MM-dd"形式に、
     * elementTypeが"datetime-local"の場合は"yyyy-MM-dd'T'HH:mm"形式にフォーマットされる。
     *
     * @param value 解析およびフォーマットする日付または日時の文字列。
     * @param elementType 入力要素のタイプ。"date"または"datetime-local"であることを期待します。
     * @return フォーマットされた日付または日時の文字列。
     * @throws IllegalArgumentException サポートされているフォーマットで入力文字列を解析できない場合。
     */
    public static String parseAndFormatDate(String value, String elementType) {
        List<DateTimeFormatter> dateFormatters = Arrays.asList(
                // 他にも有れば追加
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/d"),
                DateTimeFormatter.ofPattern("yyyy/M/dd"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("yyyy-M-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyyMMdd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("d MMM uuuu"), // 例: 16 May 2024
                DateTimeFormatter.ofPattern("d MMMM uuuu"), // 例: 16 May 2024
                DateTimeFormatter.ofPattern("MMM d, uuuu") // 例: May 16, 2024
        );

        List<DateTimeFormatter> dateTimeFormatters = Arrays.asList(
                // 日時フォーマット
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-M-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));

        boolean isDateTimeLocal = elementType.equals("datetime-local");
        List<DateTimeFormatter> formatters = isDateTimeLocal ? dateTimeFormatters : dateFormatters;

        TemporalAccessor parsedDate = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                parsedDate = formatter.parse(value);
                break;
            } catch (DateTimeParseException e) {
                // パースに失敗した場合は次のフォーマットを試す
            }
        }

        if (parsedDate != null) {
            if (isDateTimeLocal) {
                LocalDateTime dateTime = LocalDateTime.from(parsedDate);
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } else {
                LocalDate date = LocalDate.from(parsedDate);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } else {
            throw new IllegalArgumentException("入力に失敗しました(サポートされていない日付形式)。値=" + value);
        }
    }

}
