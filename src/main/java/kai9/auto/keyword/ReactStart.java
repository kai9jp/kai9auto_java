package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class ReactStart {

    // kai9auto側のDBに対する操作なのでシングルトンスコープのjdbcTemplateを用いる
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * React起動
     * 第１引数：プロジェクト格納先パス
     * 第２引数：起動ポート
     * 第３引数：バックエンドURL(プロキシ用)
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
            if (s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第３引数は省略できません。");
                return;
            }
            // プロジェクト格納先パス
            String ReactPath = s3p.s3.getValue1();

            // 起動ポート
            String ReactPort = s3p.s3.getValue2();

            // バックエンドURL(プロキシ用)
            String BackendURL = s3p.s3.getValue3();

            // npm runコマンドで起動
            String npmPath = "C:/Program Files/nodejs/npm.cmd"; // npmのインストールパス
            ProcessBuilder processBuilder = new ProcessBuilder(npmPath, "run", "start");
            processBuilder.directory(new File(ReactPath));
            // 実行結果の標準出力を取得するために、リダイレクトする
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            // https指定
            Map<String, String> env = processBuilder.environment();
            env.put("HTTPS", "true");
            env.put("BROWSER", "none");
            env.put("PORT", ReactPort);
            env.put("REACT_APP_PROXY_URL", BackendURL);
            // 外部プロセスを起動する
            Process process = processBuilder.start();

            // パラメータ変数が指定されている場合、プロセスIDを記憶させ、処理を抜ける
            String variableName = s3p.GetVariableName();
            if (!variableName.isEmpty()) {
                System.out.println(variableName);
                // プロセスIDを取得し、Mapに格納する
                long processId = process.pid();
                s3p.Variable1Map.put(variableName, String.valueOf(processId));
            }

            // 外部プロセスの標準出力を非同期で読み取るためのExecutorServiceを用意
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            StringBuilder compileResult = new StringBuilder();
            String crlf = System.lineSeparator();

            // プロセスの標準出力を読み取るタスクを実行
            Future<Void> future = executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        compileResult.append(line).append(crlf);
                        System.out.println(line); // リアルタイムで標準出力を表示
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });

            // プロセスが完了するか、中止になるまで、3秒ごとにDBをチェックする
            while (!s3p.sr3.getIs_suspension() && process.isAlive()) {
                try {
                    Thread.sleep(3000); // 3秒待機する
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // パラメータ変数が指定されている場合、プロセスIDを記憶させ、処理を抜ける
                // コンパイル結果の標準出力を取得し、No issues found.の文言があれば、処理を抜ける
                if (!variableName.isEmpty()) {
                    if (compileResult.toString().toLowerCase().contains("no issues found.")) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + ReactPath + crlf + "[実行結果]" + crlf + compileResult);
                        break;
                    }
                }

                // 標準出力を取得し、errorの文言があれば、処理を抜ける
                if (compileResult.toString().toLowerCase().contains("error")) {
                    // 外部プロセスを強制終了する
                    process.destroy();
                    break;
                }

                // JdbcTemplateを使ってsyori_rireki3テーブルからフラグをチェックする
                int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM syori_rireki3 WHERE s1_id = ? AND s_count = ? AND s2_id = ? AND s3_id = ? AND is_suspension = true",
                        Integer.class, s3p.sr3.getS1_id(), s3p.sr3.getS_count(), s3p.sr3.getS2_id(), s3p.sr3.getS3_id());

                if (count > 0) { // フラグがONになった場合
                    s3p.sr3.setIs_suspension(true);
                    // 外部プロセスを強制終了する
                    process.destroy();
                    break;
                }
            }
            if (variableName.isEmpty()) {
                future.get(); // 標準出力の読み取りが完了するまで待機
                int exitCode = process.waitFor();

                if (!compileResult.toString().toLowerCase().contains("error")) {
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + ReactPath + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[実行結果]" + crlf + compileResult);
                } else {
                    s3p.sr3s.updateError(s3p.sr3, "異常終了" + crlf + "[モジュール]" + crlf + ReactPath + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[実行結果]" + crlf + compileResult);
                }
            } else {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + ReactPath + "[プロセスID]" + crlf + process.pid() + crlf + compileResult);
            }

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
