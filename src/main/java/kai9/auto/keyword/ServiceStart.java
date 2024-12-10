package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.auto.common.Syori3Param;
import kai9.libs.Kai9Utils;

@Component
public class ServiceStart {

    /**
     * サービス起動
     *
     * 第１引数：サービス名
     */
    public void exec(Syori3Param s3p) throws IOException, InterruptedException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }

            String serviceName = s3p.s3.getValue1().trim();

            // サービスの状態を確認する
            ProcessBuilder queryBuilder = new ProcessBuilder("sc", "query", serviceName);
            Process queryProcess = queryBuilder.start();
            BufferedReader queryInput = new BufferedReader(new InputStreamReader(queryProcess.getInputStream(), "MS932"));
            String queryOutput;
            boolean servicePendingOrRunning = false;
            while ((queryOutput = queryInput.readLine()) != null) {
                if (queryOutput.trim().startsWith("STATE")) {
                    if (queryOutput.contains("RUNNING") || queryOutput.contains("START_PENDING")) {
                        servicePendingOrRunning = true;
                        break;
                    }
                }
            }
            if (servicePendingOrRunning) {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サービスは既に実行中です。");
                return;
            }

            // サービスを起動する
            ProcessBuilder startBuilder = new ProcessBuilder("net", "start", serviceName);
            Process process = startBuilder.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS932"));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "MS932"));
            StringBuilder output = new StringBuilder();

            // 標準出力からの読み取り
            stdInput.lines().forEach(line -> output.append(line).append(crlf));
            // 標準エラー出力からの読み取り
            stdError.lines().forEach(line -> output.append(line).append(crlf));

            // プロセスの終了を待機し、リターンコードを取得
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                s3p.sr3s.updateError(s3p.sr3, "サービスの起動に失敗しました" + crlf + "コマンド=net start " + serviceName + crlf + output);
            } else {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サービスを起動しました" + crlf + output);
            }
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
        }
    }
}
