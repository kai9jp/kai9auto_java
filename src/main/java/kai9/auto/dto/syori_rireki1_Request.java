package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 処理履歴_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori_rireki1_Request implements Serializable {

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
     * 処理設定_更新回数
     */
    private int syori_modify_count;

    /**
     * キーワードマスタ_更新回数
     */
    private int m_keyword_modify_count;

    /**
     * 結果(成功)
     */
    @JsonProperty("is_ok")
    private boolean is_ok;

    /**
     * シート数
     */
    private int sheet_count;

    /**
     * OK数
     */
    private int ok_count;

    /**
     * NG数
     */
    private int ng_count;

    /**
     * 開始日時
     */
    private Date start_time;

    /**
     * 終了日時
     */
    private Date end_time;

    /**
     * 進捗率
     */
    private int percent;

    /**
     * タイムアウト判定
     */
    @JsonProperty("is_timeout")
    private boolean is_timeout;

    /**
     * 中止判定
     */
    @JsonProperty("is_suspension")
    private boolean is_suspension;

    /**
     * ログ
     */
    private String log;

    /**
     * 実行IP
     */
    private String execute_ip;

    /**
     * 実行ポート
     */
    private String execute_port;

    /**
     * 実行UUID
     */
    private String execute_uuid;

    /**
     * 処理連結NG
     */
    private boolean s_linking_ng;

    /**
     * わざとNG
     */
    private boolean on_purpose_ng;

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
