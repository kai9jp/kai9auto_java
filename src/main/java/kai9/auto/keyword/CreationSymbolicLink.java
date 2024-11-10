package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class CreationSymbolicLink {

    /**
     * シンボリックリンク作成
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
        try {
            // 第一引数チェック
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            // 第二引数チェック
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }

            // 第一引数と第二引数を取得
            String destPath = s3p.s3.getValue1().trim(); // シンボリックリンクのパス
            String srcPath = s3p.s3.getValue2().trim(); // リンク先のパス

            // mklinkコマンドを発行
            String command = "cmd /c mklink /D \"" + destPath + "\" \"" + srcPath + "\"";
            Process process = Runtime.getRuntime().exec(command);

            // 標準出力とエラー出力を取得 (エンコーディングを指定)
            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS932"));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "MS932"));

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            String line;
            while ((line = stdOutput.readLine()) != null) {
                output.append(line).append(crlf);
            }
            while ((line = stdError.readLine()) != null) {
                error.append(line).append(crlf);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "シンボリックリンクの作成に成功しました" + crlf + "リンク: " + destPath + crlf + "リンク先: " + srcPath + crlf + "出力: " + output.toString());
            } else {
                s3p.sr3s.updateError(s3p.sr3, "シンボリックリンクの作成に失敗しました" + crlf + "リンク: " + destPath + crlf + "リンク先: " + srcPath + crlf + "エラー: " + error.toString());
            }

        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
        }
    }

}
