package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.exec.AutoExec;
import kai9.auto.model.syori_rireki1;

@Component
public class S_Linking {

    @Autowired
    @Qualifier("commonjdbc")
    public JdbcTemplate jdbcTemplate_com;

    @Autowired
    public JdbcTemplate db_jdbcTemplate;

    /**
     * 処理連結
     * 
     * 引数1で指定されたNoの処理を実行する
     * 引数2(カンマ区切り可/省略可)
     * 引数3(カンマ区切り可/省略可)
     * ※引数3指定時は引数2も必須
     * ※引数3指定時は引数2の複数指定不可
     */
    public void exec(Syori3Param s3p, String keyword) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();// 改行コード
        try {
            String value1 = s3p.s3.getValue1().trim();
            String value2 = s3p.s3.getValue2().trim();
            String value3 = s3p.s3.getValue3().trim();
            // 引数チェック
            if (value1.isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (!value1.matches("\\d+")) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は数値でなければなりません。");
                return;
            }
            if (!value2.isEmpty() && !value2.matches("\\d+(,\\d+)*")) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は数値またはカンマ区切りの数値でなければなりません。");
                return;
            }
            if (!value3.isEmpty()) {
                if (value2.isEmpty()) {
                    s3p.sr3s.updateError(s3p.sr3, "第3引数を指定した場合、第2引数は必須です。");
                    return;
                }
                if (value2.contains(",")) {
                    s3p.sr3s.updateError(s3p.sr3, "第3引数を指定した場合、第2引数は単一の数値でなければなりません。");
                    return;
                }
                if (!value3.matches("\\d+(,\\d+)*")) {
                    s3p.sr3s.updateError(s3p.sr3, "第3引数は数値またはカンマ区切りの数値でなければなりません。");
                    return;
                }
            }

            // 処理連結NGフラグ
            Boolean s_linking_ng = (keyword.equals("S_LinkingNG") || keyword.equals("S_LinkingNG_nowait"));

            // execute_uuidをセット
            UUID uniqueID = UUID.randomUUID();
            // 処理実行
            AutoExec.callAPI(Integer.valueOf(value1), value2, value3, 0, uniqueID.toString(), db_jdbcTemplate, jdbcTemplate_com, s_linking_ng, "");

            if (keyword.equals("S_Linking") || keyword.equals("S_LinkingNG")) {
                // 処理連結 又は 処理連結NG版

                // 完了を待つ
                String SELECT_SQL = "SELECT * FROM syori_rireki1 WHERE s1_id = ? AND execute_uuid = ?";
                int POLLING_INTERVAL_MS = 1000; // 1秒間隔でポーリング
                int TIMEOUT_MS = 10000; // タイムアウト時間10秒
                long startTime = System.currentTimeMillis();
                while (true) {
                    // SQL発行
                    RowMapper<syori_rireki1> rowMapper = new BeanPropertyRowMapper<>(syori_rireki1.class);
                    List<syori_rireki1> results = db_jdbcTemplate.query(
                            SELECT_SQL,
                            rowMapper,
                            Integer.valueOf(value1),
                            uniqueID.toString());
                    if (results.isEmpty()) {
                        // 10秒経過しても処理が行われていない場合、APIコールに失敗しているので例外を発生し抜ける
                        if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                            throw new RuntimeException("処理の呼び出しに失敗しました。");
                        }
                    }

                    // 結果確認
                    Integer percent = null;
                    Integer resultType = null;
                    Date end_time = null;
                    if (!results.isEmpty()) {
                        syori_rireki1 firstRecord = results.get(0);
                        percent = firstRecord.getPercent();
                        resultType = firstRecord.getResult_type();
                        end_time = firstRecord.getEnd_time();
                    }

                    if (end_time != null && percent != null && percent == 100) {
                        // 結果表示
                        System.out.println("Processing complete.");
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "処理が完了しました。" +
                                crlf + "処理1=" + value1 + "、処理2=" + value2 + "、処理3=" + value3 +
                                crlf + "結果=" + resultType +
                                crlf + "※0:想定通り、1:想定違い、2:想定通りの相違、3:中止");
                        return;
                    }

                    try {
                        Thread.sleep(POLLING_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("別スレッドからの要求でスレッドを中断しました。", e);
                    }
                }
            } else {
                // 処理連結(待機せず) 又は 処理連結NG版(待機せず)
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "処理をコールしました。" + crlf + "処理1=" + value1 + "、処理2=" + value2 + "、処理3=" + value3);
                return;
            }

        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
