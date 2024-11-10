package kai9.auto.keyword;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class SQLResult {

    /**
     * SQL結果取得
     * 第1引数：SQL
     * SQLを発行し結果をログに格納(取得系のみで、exec系の発行は出来ない)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }

            // SQL発行
            String sql = s3p.s3.getValue1();
            // SQLクエリを実行して結果をテキスト形式で取得する
            String result = s3p.db_jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
                @Override
                public String extractData(ResultSet rs) throws SQLException {
                    // 結果を格納するStringBuilderを作成する
                    StringBuilder result = new StringBuilder();
                    // 結果セットの列数を取得する
                    int columnCount = rs.getMetaData().getColumnCount();
                    // 結果セットから行を一つずつ取り出して、テキスト形式で結果を格納する
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            result.append(rs.getString(i)).append("\t"); // タブで列を区切る
                        }
                        result.append("\n"); // 改行で行を区切る
                    }
                    // StringBuilderを文字列に変換して返す
                    return result.toString();
                }
            });
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, result);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
