package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class SpringCompile {

    // kai9auto側のDBに対する操作なのでシングルトンスコープのjdbcTemplateを用いる
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * springコンパイル
     * 第1引数：プロジェクトのルートディレクトリ
     * 例)G:\OneDrive\work\25.java\06.kai9auto
     *
     * 第2引数：Mavenの実行ファイルのパス
     * 例)C:\java\apache-maven-3.6.3\bin\mvn.cmd
     * 
     * 第3引数：生成モジュール格納パス
     * 例)d:\kai9auto\test001\kai9-test.jar
     * 
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

            // プロジェクトのルートディレクトリ
            String projectDir = s3p.s3.getValue1();

            // Mavenの実行ファイルのパス
            String mavenPath = s3p.s3.getValue2();

            // Mavenのコマンドラインオプション
            String[] mavenCommand = { mavenPath, "clean", "install", "-Pexclude-resources" }; // application.ymlを除外

            // Mavenのプロセスを起動し、プロジェクトをコンパイルする
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(projectDir));
            processBuilder.command(mavenCommand);
            // コンパイル結果の標準出力を取得するために、リダイレクトする
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 標準出力を別スレッドでリアルタイムに読み取る
            // 明示的にMS932（Shift_JIS）エンコーディングを使用する
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("MS932")))) {
                String line;
                StringBuilder compileResult = new StringBuilder();  // コンパイル結果を保持する
                while ((line = reader.readLine()) != null) {
                    compileResult.append(line).append(System.lineSeparator());
                }

                // プロセスが完了するまで待機し、終了コードを取得する
                int exitCode = process.waitFor();
                String crlf = System.lineSeparator();

                if (s3p.sr3.getIs_suspension()) {
                    s3p.sr3s.updateError(s3p.sr3, "中止されました" + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[コンパイル結果]" + crlf + compileResult);
                } else {
                    //if (!compileResult.toString().toLowerCase().contains("error")) { ソースコードと、そのパスの名前にerrorを含む物が有り、これだとダメだったので、単純にexitコードでの確認に変更(元々、何故exitコードでの確認にしていなかったのか不明)
                    if (exitCode == 0) {
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "実行終了" + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[コンパイル結果]" + crlf + compileResult);
                    } else {
                        s3p.sr3s.updateError(s3p.sr3, "異常終了" + crlf + "[プロセスの終了コード]" + exitCode + crlf + "[コンパイル結果]" + crlf + compileResult);
                    }
                }
            }

            // プロセスが完了するか、中止になるまで、3秒ごとにDBをチェックする
            while (!s3p.sr3.getIs_suspension() && process.isAlive()) {
                try {
                    Thread.sleep(3000); // 3秒待機する
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }
}
