package kai9.auto.keyword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class DeleteFolder {

    /**
     * フォルダ削除
     * 
     * 無ければ削除しない(エラーにする)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();// 改行コード
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            String folderPath = s3p.s3.getValue1().trim();
            Path folder = Paths.get(folderPath);
            if (Files.exists(folder)) {
                // 有れば削除
                if (FileSystemUtils.deleteRecursively(folder)) {
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "フォルダを削除しました" + crlf + folderPath);
                } else {
                    s3p.sr3s.updateError(s3p.sr3, "フォルダの削除に失敗しました" + crlf + folderPath);
                }
            } else {
                // 無ければエラー
                s3p.sr3s.updateError(s3p.sr3, "フォルダが存在しませんでした" + crlf + folderPath);
            }
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
