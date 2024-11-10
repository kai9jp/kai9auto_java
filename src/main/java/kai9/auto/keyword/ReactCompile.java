package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class ReactCompile {

    // kai9auto側のDBに対する操作なのでシングルトンスコープのjdbcTemplateを用いる
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Reactコンパイル
     * 第1引数：プロジェクトのルートディレクトリ
     * 例)G:\OneDrive\work\36.React\02.Kai9ReactTemplate
     * 第2引数：npm.cmdのインストールパス
     * 例)C:\Program Files\nodejs\npm.cmd
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
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
            File directory = new File(projectDir);

            // npmのパス
            String npm_path = s3p.s3.getValue2();
            // 文字列が"npm.cmd"で終わるかどうかをチェック
            if (!npm_path.endsWith("npm.cmd")) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数の指定が不正です。");
                return;
            }

            // プロセスを起動し、プロジェクトをコンパイルする
            ProcessBuilder startBuilder = new ProcessBuilder(npm_path, "run", "build");
            startBuilder.directory(directory); // 作業ディレクトリの設定
            Map<String, String> env = startBuilder.environment();
            env.put("NODE_OPTIONS", "--max-old-space-size=8192");
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
                s3p.sr3s.updateError(s3p.sr3, "Reactのコンパイルに失敗しました" + crlf + "コマンド=npm run build " + crlf + "ディレクトリ=" + projectDir + crlf + output);
            } else {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "Reactのコンパイルに成功しました" + crlf + "ディレクトリ=" + projectDir + crlf + output);
            }
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, "予期せぬエラーが発生しました" + crlf + Kai9Utils.GetException(e));
            return;
        }
    }

}
