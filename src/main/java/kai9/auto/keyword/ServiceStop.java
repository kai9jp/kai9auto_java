package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.auto.common.Syori3Param;

@Component
public class ServiceStop {

    /**
     * サービス停止
     * 
     * 第１引数：サービス名
     * 第２引数：タイムアウト値を秒で指定する
     * 第３引数：タイムアウトした場合にサービスを強制終了させる場合、「タイムアウト時に強制終了」と指定する
     * 
     */
    public void exec(Syori3Param s3p) throws IOException, InterruptedException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            String serviceName = s3p.s3.getValue1().trim();

            // タイムアウト値の検証
            String timeoutValue = s3p.s3.getValue2().trim();
            int timeoutInSeconds = Integer.MAX_VALUE;// 最大値は（2,147,483,647秒、約68年）
            if (!timeoutValue.isEmpty()) {
                try {
                    timeoutInSeconds = Integer.parseInt(timeoutValue);
                } catch (NumberFormatException e) {
                    s3p.sr3s.updateError(s3p.sr3, "タイムアウト値が無効です。");
                    return;
                }
            }

            // 強制終了オプションの検証
            String forceStopOption = s3p.s3.getValue3().trim();
            if (!forceStopOption.isEmpty()) {
                if (!"タイムアウト時に強制終了".equals(forceStopOption)) {
                    s3p.sr3s.updateError(s3p.sr3, "無効なオプションが指定されました。");
                    return;
                }
            }

            // 既に停止しているかを確認
            ProcessBuilder queryBuilder = new ProcessBuilder("sc", "query", serviceName);
            Process queryProcess = queryBuilder.start();
            BufferedReader queryInput = new BufferedReader(new InputStreamReader(queryProcess.getInputStream(), "MS932"));
            String queryOutput;
            boolean serviceStopped = false;
            while ((queryOutput = queryInput.readLine()) != null) {
                if (queryOutput.trim().startsWith("STATE")) {
                    if (queryOutput.contains("STOPPED")) {
                        serviceStopped = true;
                        break;
                    }
                }
            }
            if (serviceStopped) {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サービスは既に停止中です。");
                return;
            }

            // サービスを停止する
            ProcessBuilder stopBuilder = new ProcessBuilder("net", "stop", serviceName);
            Process process = stopBuilder.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS932"));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "MS932"));
            StringBuilder output = new StringBuilder();

            try {
                // タイムアウトを考慮してプロセスを待機
                boolean finished = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);
                if (!finished) {
                    // タイムアウト時の処理
                    if ("タイムアウト時に強制終了".equals(forceStopOption)) {
                        process.destroy();
                        // プロセスIDを取得
                        ProcessBuilder queryExBuilder = new ProcessBuilder("sc", "queryex", serviceName);
                        Process queryExProcess = queryExBuilder.start();
                        BufferedReader queryExInput = new BufferedReader(new InputStreamReader(queryExProcess.getInputStream()));
                        String line;
                        int pid = 0;
                        while ((line = queryExInput.readLine()) != null) {
                            if (line.trim().startsWith("PID")) {
                                pid = Integer.parseInt(line.split(":")[1].trim());
                                break;
                            }
                        }
                        if (pid > 0) {
                            // プロセスの強制終了
                            ProcessBuilder taskKillBuilder = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid));
                            Process taskKillProcess = taskKillBuilder.start();
                            BufferedReader stdInput2 = new BufferedReader(new InputStreamReader(taskKillProcess.getInputStream(), "MS932"));
                            BufferedReader stdError2 = new BufferedReader(new InputStreamReader(taskKillProcess.getErrorStream(), "MS932"));
                            StringBuilder output2 = new StringBuilder();
                            // 標準出力と標準エラー出力からの読み取り
                            stdInput2.lines().forEach(line2 -> output2.append(line2).append(crlf));
                            stdError2.lines().forEach(line2 -> output2.append(line2).append(crlf));
                            s3p.sr3s.updateError(s3p.sr3, "サービスの停止がタイムアウトしたので、強制終了しました。" + crlf + "タイムアウト値=" + timeoutInSeconds + "秒" + crlf + output2.toString());
                        } else {
                            s3p.sr3s.updateError(s3p.sr3, "プロセスの強制停止に失敗しました。");
                        }
                    } else {
                        s3p.sr3s.updateError(s3p.sr3, "サービスの停止がタイムアウトしました。" + crlf + "タイムアウト値=" + timeoutInSeconds + "秒");
                    }
                } else {
                    // 戻り値を取得
                    int exitCode = process.exitValue();
                    // 標準出力と標準エラー出力からの読み取り
                    stdInput.lines().forEach(line -> output.append(line).append(crlf));
                    stdError.lines().forEach(line -> output.append(line).append(crlf));
                    if (exitCode == 0) {
                        // サービス停止処理が正常に完了した場合の処理
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サービスを停止しました" + crlf + output.toString());
                    } else {
                        s3p.sr3s.updateError(s3p.sr3, "サービスの停止に失敗しました" + crlf + "コマンド=net stop " + serviceName + crlf + output.toString());
                    }
                }

            } catch (Exception e) {
                s3p.sr3s.updateError(s3p.sr3, "サービスの停止に失敗しました" + crlf + "コマンド=net stop " + serviceName + crlf + output.toString() + crlf + e.getMessage());
            }
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, e.toString());
        }
    }
}
