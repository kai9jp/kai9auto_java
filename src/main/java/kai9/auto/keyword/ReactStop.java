package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class ReactStop {

    /**
     * React停止
     * 
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getVariable1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "変数は省略できません。");
                return;
            }

            // パラメータ変数が指定されている場合、プロセスIDを記憶させ、処理を抜ける
            if (s3p.GetVariableName().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "変数名が特定できませんでした。[@変数名@] の形式で指定して下さい");
                return;
            }

            long processId = Long.parseLong(s3p.Variable1Map.get(s3p.GetVariableName()));
            // プロセスIDを使用して、対象の外部プロセスを停止する
            ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/pid", Long.toString(processId));
            pb.redirectErrorStream(true); // 標準出力と標準エラーをマージ
            Process process = pb.start();
            // Shift_JIS エンコーディングで InputStreamReader を作成
            InputStreamReader isr = new InputStreamReader(process.getInputStream(), "Shift_JIS");
            BufferedReader br = new BufferedReader(isr);
            // 外部プロセスの出力を読み込み、表示
            StringBuilder compileResult = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                compileResult.append(line);
                System.out.println(line);
            }
            process.waitFor();

            String crlf = System.lineSeparator();
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "Reactの停止に成功しました" + crlf + "プロセスID=" + processId + crlf + compileResult);

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
