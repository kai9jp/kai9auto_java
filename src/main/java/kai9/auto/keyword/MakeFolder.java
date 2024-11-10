package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class MakeFolder {

    /**
     * フォルダ作成
     * 
     * 有れば作成しない(エラーにもしない)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();// 改行コード
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }

            String folderPath = s3p.s3.getValue1().trim();
            File folder = new File(folderPath);
            if (!folder.exists()) {
                // 無ければ作成
                if (folder.mkdirs()) {
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "フォルダを作成しました" + crlf + folderPath);
                } else {
                    // 原因不明のエラー
                    s3p.sr3s.updateError(s3p.sr3, "フォルダの作成に失敗しました" + crlf + folderPath);
                }
            } else {
                // 既に有れば何もしない(エラーにしない)
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "既にフォルダが存在したので処理をスキップしました" + crlf + folderPath);
            }
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
