package kai9.auto.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.model.db_version;
import kai9.auto.service.db_version_Service;

@Component
public class DBVersionSync {

    private db_version_Service db_version_Service;

    @Autowired
    private ApplicationContext context;// メモリ解放不要

    /**
     * DBバージョン同期
     * 第1引数：DDLの格納先フォルダ
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        this.db_version_Service = context.getBean(db_version_Service.class);
        String crlf = System.lineSeparator();// 改行コード
        String sqls = "";
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            String scriptsFolder = s3p.s3.getValue1();
            if (!Files.isDirectory(Paths.get(scriptsFolder))) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数で指定されたフォルダが存在しません。");
                return;
            }
            String schemaName = "";
            if (!s3p.s3.getValue2().trim().isEmpty()) {
                schemaName = s3p.s3.getValue2().trim();
                // スキーマを指定
                s3p.db_jdbcTemplate.execute("SET search_path TO " + schemaName);
            }

            // DBバージョンテーブルの存在確認
            String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'db_version_a')";
            Boolean IsExist = s3p.db_jdbcTemplate.queryForObject(sql, Boolean.class);

            db_version db_version = new db_version();
            int dbVersion = 0;
            if (IsExist) {
                // 現在のDBバージョンを取得するSQLを発行
                db_version = db_version_Service.findById(s3p.db_jdbcTemplate);
                dbVersion = db_version.getDb_version();
            }

            // スクリプトファイルを取得し、ファイル名の数字順にソートする
            File folder = new File(scriptsFolder);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".sql"));
            Arrays.sort(files, Comparator.comparing(File::getName));

            // 各スクリプトファイルをループして、DBバージョンよりも新しいスクリプトのみ実行する
            int scriptVersion = 0;
            for (File file : files) {
                String filename = file.getName();
                // ファイル名から数字の部分(先頭の数値箇所)を抽出する
                scriptVersion = extractScriptVersion(filename);

                // スクリプトのバージョンが現在のDBバージョンよりも大きい場合にのみSQLスクリプトを実行する
                if (scriptVersion > dbVersion) {
                    // SQLスクリプトをファイルから読み取り、実行する
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                        sql = reader.lines().collect(Collectors.joining("\n"));
                        s3p.db_jdbcTemplate.execute(sql);
                        sqls = sqls + sql + crlf;
                    } catch (IOException e) {
                        s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
                        return;
                    }
                }
            }

            // 再度DBバージョンテーブルの存在確認(上のSQL発行でDBバージョンテーブルが作られている場合に備え最新化
            sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'db_version_a')";
            IsExist = s3p.db_jdbcTemplate.queryForObject(sql, Boolean.class);

            // DBバージョンを更新する
            if (IsExist) {
                db_version.setDb_version(scriptVersion);
                db_version_Service.update(db_version, s3p.db_jdbcTemplate);
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "DBバージョンを更新しました" + crlf + "バージョン=" + scriptVersion + crlf + sqls);
            } else {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "DBバージョンを更新しました" + crlf + "バージョン=UP無し" + crlf + sqls);
            }

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e) + crlf + sqls);
            return;
        } finally {
            this.db_version_Service = null;
        }
    }

    /**
     * ファイル名からスクリプトバージョンを抽出する。
     * ファイル名は、001_DBバージョン.sqlや99981_DBバージョン.sql、1_DBバージョン.sqlなど、数字で始まる文字列である必要がある。
     * 「040間に文字08が入るテスト.sql」の場合に「40」が返る安心設計
     * スクリプトバージョンは、先頭の0を除いた数字として返す。
     *
     * @param filename ファイル名
     * @return スクリプトバージョン
     */
    public static int extractScriptVersion(String filename) {
        // ファイル名から拡張子を除いた部分を取得する
        String filenameWithoutExt = filename.substring(0, filename.indexOf("."));

        // 数字が現れるまでスキップし、数字が現れたらその文字を結果の文字列に追加する
        // 数字が連続する限り続け、数字以外の文字が現れたらループを終了する
        StringBuilder scriptVersionBuilder = new StringBuilder();
        boolean digitStarted = false;
        for (char c : filenameWithoutExt.toCharArray()) {
            if (Character.isDigit(c)) {
                digitStarted = true;
                scriptVersionBuilder.append(c);
            } else if (digitStarted) {
                break;
            }
        }

        // 先頭の0を除いた文字列を取得する
        // (正規表現"^0+(?!$)"を使うことで、先頭の0を除く。ただし、0だけの場合は残す)
        String scriptVersionStr = scriptVersionBuilder.toString().replaceFirst("^0+(?!$)", "");

        // 文字列を整数に変換する
        int scriptVersion = Integer.parseInt(scriptVersionStr);

        return scriptVersion;
    }

}
