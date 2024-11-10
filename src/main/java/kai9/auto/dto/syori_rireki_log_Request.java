package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 処理履歴_ログ :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori_rireki_log_Request implements Serializable {

    /**
     * 処理No_1
     */
    private int s1_id;

    /**
     * 更新回数
     */
    private int modify_count;

    /**
     * 処理回数
     */
    private int s_count;

    /**
     * 処理No_2
     */
    private int s2_id;

    /**
     * ログ行数
     */
    private int l_count;

    /**
     * ログ
     */
    private String log;

    /**
     * 更新者
     */
    private int update_u_id;

    /**
     * 更新日時
     */
    private Date update_date;

    /**
     * 削除フラグ
     */
    private boolean delflg;

}
