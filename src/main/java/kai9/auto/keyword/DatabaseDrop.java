package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class DatabaseDrop {

    /**
     * DB削除
     * 第1引数：DBアドレスとポート/デフォルトデータベース名 例)localhost:5432/postgres
     * 第2引数：IDとパスワードを:で連結し記載
     * 第3引数：データベース名
     * 
     * @throws SQLException
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
            if (s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数は省略できません。");
                return;
            }
            if (!s3p.s3.getValue1().contains(":") || !s3p.s3.getValue1().contains("/")) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数には「:」と「/」が含まれている必要があります。形式は「ip(又はホスト名):ポート番号/デフォルトデータベース名」として下さい。");
                return;
            }
            if (!s3p.s3.getValue2().contains(":")) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数に「:」が含まれていません「id:password」形式にして下さい");
                return;
            }

            String url = "jdbc:postgresql://" + s3p.s3.getValue1();
            String username = s3p.s3.getValue2().split(":")[0];
            String password = s3p.s3.getValue2().split(":")[1];
            String databaseName = s3p.s3.getValue3();
            if (!databaseName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数のDB名が不正です。「英数字またはアンダースコア」以外が含まれています");
                return;
            }

            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 存在チェック
            String checkDBNameSQL = "SELECT count(*) FROM pg_database WHERE datname = ?";
            Integer count = jdbcTemplate.queryForObject(checkDBNameSQL, Integer.class, databaseName);
            if (count == 0) {
                s3p.sr3s.updateError(s3p.sr3, "削除対象のデータベースが存在しません。「データベース名=" + databaseName + "」");
                return;
            }

            // DB削除
            String createDatabaseSQL = "DROP DATABASE " + databaseName + ";";
            jdbcTemplate.execute(createDatabaseSQL);

            String crlf = System.lineSeparator();
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "DB削除に成功しました" + crlf + "データベース名=" + databaseName);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
