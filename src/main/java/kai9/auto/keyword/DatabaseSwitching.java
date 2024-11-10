package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class DatabaseSwitching {

    /**
     * DB切替
     * 第1引数：DBアドレスとポート/デフォルトデータベース名 例)localhost:5432/postgres
     * 第2引数：IDとパスワードを:で連結し記載
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数は省略できません。");
                return;
            }
            if (!s3p.s3.getValue1().contains(":") || !s3p.s3.getValue1().contains("/")) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数には「:」と「/」が含まれている必要があります。形式は「ip(又はホスト名):ポート番号/デフォルトデータベース名」として下さい。");
                return;
            }
            if (!s3p.s3.getValue2().contains(":")) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数に「:」が含まれていません「id:password」形式にして下さい");
                return;
            }

            String url = "jdbc:postgresql://" + s3p.s3.getValue1();
            String username = s3p.s3.getValue2().split(":")[0];
            String password = s3p.s3.getValue2().split(":")[1];
            s3p.setSchema(s3p.s3.getValue3());

            // DB切替
            Object lock = new Object();
            String result = "";
            synchronized (lock) {
                result = s3p.ChangeDB(url, username, password);
                lock.notifyAll(); // 待機中のスレッドに通知
            }

            String crlf = System.lineSeparator();
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "DB切替に成功しました" + crlf + "[接続先データベース]" + crlf + url + crlf + "[存在するテーブル]" + crlf + result);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
