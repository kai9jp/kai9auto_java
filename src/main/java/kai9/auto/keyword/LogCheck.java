package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.model.syori_rireki3;

@Component
public class LogCheck {

    // kai9auto側のDBに対する操作なのでシングルトンスコープのjdbcTemplateを用いる
    @Autowired
    JdbcTemplate jdbcTemplate;

    // ログ確認(全行)
    // ログ全体を検索しヒット判定の結果を返す。
    // 第2引数はAND条件で検索(カンマ区切で複数指定可能)
    // 第3引数はOR条件で検索(カンマ区切で複数指定可能)
    // ※どちらか片方しか指定できない(第2引数優先)
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (!s3p.s3.getValue1().matches("\\d+")) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は数値以外指定出来ません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty() && s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数と第3引数の両方は省略できません。");
                return;
            }

            String sql = "SELECT * FROM syori_rireki3 a left join syori3_a b on a.s1_id = b.s1_id and a.s2_id = b.s2_id and a.s3_id = b.s3_id WHERE a.s1_id = :s1_id AND a.s2_id = :s2_id AND b.step = :step AND a.s_count = :s_count";
            SqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("s1_id", s3p.sr3.getS1_id(), Types.INTEGER)
                    .addValue("s2_id", s3p.sr3.getS2_id(), Types.INTEGER)
                    .addValue("step", s3p.s3.getValue1(), Types.INTEGER)
                    .addValue("s_count", s3p.sr3.getS_count(), Types.INTEGER);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
//    	    syori_rireki3 sr3_log = namedTemplate.queryForObject(sql, namedParameters, new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class));
            List<syori_rireki3> sr3_logs = namedTemplate.query(sql, namedParameters, new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class));
            if (sr3_logs.isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "ログが存在しませんでした");
                return;
            }
            syori_rireki3 sr3_log = sr3_logs.get(0);

            String log = sr3_log.getLog(); // 検索対象のログ
            if (!s3p.s3.getValue2().equals("")) {
                // AND条件で検索するキーワード
                String andKey = s3p.s3.getValue2();

                // 検索条件をカンマで分割
                String[] keywords = andKey.split(",");

                // 各キーワードがログに含まれているかを判定するフラグと、見つからなかったキーワードを記録するリスト
                boolean allKeywordsFound = true;
                List<String> notFoundKeywords = new ArrayList<>();
                List<String> hitFoundKeywords = new ArrayList<>();

                // 各キーワードを順に検索する
                for (String keyword : keywords) {
                    // 検索キーワードを正規表現パターンに変換する
                    String pattern = ".*" + Pattern.quote(keyword.trim()) + ".*";
                    Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = compiledPattern.matcher(log);

                    // 正規表現で検索する
                    if (!matcher.find()) {
                        // キーワードが見つからなかった場合はフラグをfalseにして、見つからなかったキーワードをリストに追加する
                        allKeywordsFound = false;
                        notFoundKeywords.add(keyword.trim());
                    } else {
                        hitFoundKeywords.add(keyword.trim());
                    }
                }

                // キーワードが1つでも見つからなかった場合の処理
                String notFoundKeywordsStr = String.join(crlf, notFoundKeywords);
                String hitFoundKeywordsStr = String.join(crlf, hitFoundKeywords);
                String msg = "[ログ]" + crlf + log + crlf + crlf + "[一致対象(OK)]" + crlf + hitFoundKeywordsStr + crlf + crlf + "[不一致対象(NG)]" + crlf + notFoundKeywordsStr;
                if (allKeywordsFound) {
                    // 全てのキーワードが見つかった場合の処理
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "一致しました" + crlf + msg);
                } else {
                    s3p.sr3s.updateError(s3p.sr3, "不一致でした" + crlf + msg);
                }
            } else {
                String oaKey = s3p.s3.getValue3(); // OA条件で検索するキーワード

                // 検索条件をカンマで分割
                String[] keywords = oaKey.split(",");

                // 各キーワードがログに含まれているかを判定するフラグと、見つからなかったキーワードを記録するリスト
                boolean anyKeywordFound = false;
                List<String> notFoundKeywords = new ArrayList<>();
                List<String> hitFoundKeywords = new ArrayList<>();

                // 各キーワードを順に検索する
                for (String keyword : keywords) {
                    // 検索キーワードを正規表現パターンに変換する
                    String pattern = ".*" + Pattern.quote(keyword.trim()) + ".*";
                    Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = compiledPattern.matcher(log);

                    // 正規表現で検索する
                    if (matcher.find()) {
                        // キーワードが見つかった場合はフラグをtrueにして、見つかったキーワードをリストに追加する
                        anyKeywordFound = true;
                        hitFoundKeywords.add(keyword.trim());
                    } else {
                        notFoundKeywords.add(keyword.trim());
                    }
                }

                String hitFoundKeywordsStr = String.join(crlf, hitFoundKeywords);
                String notFoundKeywordsStr = String.join(crlf, notFoundKeywords);
                String msg = "[ログ]" + crlf + log + crlf + crlf + "[一致対象]" + crlf + hitFoundKeywordsStr + crlf + crlf + "[不一致対象]" + crlf + notFoundKeywordsStr;
                if (anyKeywordFound) {
                    // いずれかのキーワードが見つかった場合の処理
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "一致しました" + crlf + msg);
                } else {
                    // 全てのキーワードが見つからなかった場合の処理
                    s3p.sr3s.updateError(s3p.sr3, "不一致でした" + crlf + msg);
                }
            }

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
