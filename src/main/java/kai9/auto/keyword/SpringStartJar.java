package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
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
public class SpringStartJar {

    // kai9auto用のDBを用いるのでシングルトンスコープを利用
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * springのCD 継続的デプロイ
     * 第１引数：「JARファイルのパス,application.propertiesファイルのパス,実行先フォルダ(省略可)」
     * 第２引数：：DB接続情報 「DBアドレス:ポート/デフォルトデータベース名」形式で指定 例)localhost:5432/postgres
     * 第３引数：WEBサーバ情報 「ポート」
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();
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

            // プログラム情報
            String[] parts1 = s3p.s3.getValue1().split(",");// カンマで分割
            if (parts1.length <= 2) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数にはカンマ区切りで2つ以上の要素が必要ですが、要素数「" + parts1.length + "」でした");
                return;
            }
            // JARファイルのパス
            String jarPath = parts1[0];
            // application.propertiesファイルのパス
            String appPropertiesPath = parts1[1];
            if (parts1.length >= 3) {
                // 実行先フォルダのパス
                String ExecPath = parts1[2];
                // 存在しない場合は作成する
                Path path = Paths.get(ExecPath);
                try {
                    Files.createDirectories(path);
                } catch (Exception e) {
                    s3p.sr3s.updateError(s3p.sr3, "フォルダの作成に失敗しました。" + crlf + "実行先フォルダ=「" + ExecPath + "」" + crlf + e.getMessage());
                    return;
                }

                // jarとapplication.propertiesを実行先フォルダへコピーする
                Path jarSourcePath = Paths.get(jarPath);
                Path appPropertiesSourcePath = Paths.get(appPropertiesPath);
                Path destPath = Paths.get(ExecPath);

                // コピー先のファイル名で再代入
                jarPath = Paths.get(ExecPath, jarSourcePath.getFileName().toString()).toString();
                appPropertiesPath = Paths.get(ExecPath, appPropertiesSourcePath.getFileName().toString()).toString();
                try {
                    // JARファイルをコピーする
                    Files.copy(jarSourcePath, destPath.resolve(jarSourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    // application.propertiesファイルをコピーする
                    Files.copy(appPropertiesSourcePath, destPath.resolve(appPropertiesSourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // WEBサーバ情報
            String[] parts2 = s3p.s3.getValue2().split(",");// カンマで分割
            // serverのport
            String serverPort = parts2[0];

            // DB接続情報 localhost:5432/postgres,test001,pw,public
            String[] parts3 = s3p.s3.getValue3().split(",");// カンマで分割
            if (parts3.length != 4) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数にはカンマ区切りで4要素必要ですが要素数「" + parts3.length + "」でした");
                return;
            }
            String db_Url = "jdbc:postgresql://" + parts3[0];
            String db_Username = parts3[1];
            String db_Password = parts3[2];
            String schema = parts3[3];// スキーマは未対応:publicで良いのでは・・

            // server.portプロパティを変更する
            try {
                // ファイルの内容を読み込む
                byte[] bytes = Files.readAllBytes(Paths.get(appPropertiesPath));
                String fileContent = new String(bytes, StandardCharsets.UTF_8);

                // 文字列置換を行う
                String updatedContent = fileContent.replaceAll("(^|\\n)server\\.port=.*", "$1server.port=" + serverPort)
                        .replaceAll("(^|\\n)spring\\.datasource\\.primary\\.url=.*", "$1spring.datasource.primary.url=" + db_Url)
                        .replaceAll("(^|\\n)spring\\.datasource\\.primary\\.username=.*", "$1spring.datasource.primary.username=" + db_Username)
                        .replaceAll("(^|\\n)spring\\.datasource\\.primary\\.password=.*", "$1spring.datasource.primary.password=" + db_Password);

                // 同じファイルに書き込む
                Files.write(Paths.get(appPropertiesPath), updatedContent.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Javaコマンドのパス
            // String javaCommand = "java";
            String javaCommand = "C:\\Program Files\\Eclipse Adoptium\\jdk-11.0.17.8-hotspot\\bin\\java";

            // Javaコマンドを起動し、JARファイルを実行するためのProcessBuilderを生成する
            ProcessBuilder processBuilder = new ProcessBuilder(javaCommand, "-jar", "-Dspring.config.location=file:" + appPropertiesPath, jarPath);
            // 実行結果の標準出力を取得するために、リダイレクトする
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
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
                // コンパイル結果の標準出力を取得し、jvm runningの文言があれば、処理を抜ける
                if (!variableName.isEmpty()) {
                    if (compileResult.toString().toLowerCase().contains("jvm running")) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + jarPath + crlf + "[実行結果]" + crlf + compileResult);
                        break;
                    }
                }

                // コンパイル結果の標準出力を取得し、errorの文言があれば、処理を抜ける
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
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + jarPath + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[実行結果]" + crlf + compileResult);
                } else {
                    s3p.sr3s.updateError(s3p.sr3, "異常終了" + crlf + "[モジュール]" + crlf + jarPath + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[実行結果]" + crlf + compileResult);
                }
            } else {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[モジュール]" + crlf + jarPath + "[プロセスID]" + crlf + process.pid() + crlf + compileResult);
            }

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
