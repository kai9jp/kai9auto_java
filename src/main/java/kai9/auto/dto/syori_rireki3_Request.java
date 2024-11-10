package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 処理履歴_孫 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori_rireki3_Request implements Serializable {

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
     * 処理No_3
     */
    private int s3_id;

    /**
     * 成功したかのフラグ
     */
    @JsonProperty("is_ok")
    private boolean is_ok;

    /**
     * 進捗率(SYORI3)
     */
    private int percent3;

    /**
     * ログ
     */
    private String log;

    /**
     * スクショ格納場所
     */
    private String screen_shot_filepath;

    /**
     * 開始時刻
     */
    private Date start_time;

    /**
     * 終了時刻
     */
    private Date end_time;

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
     * 更新者
     */
    private int update_u_id;

    /**
     * 作成日時
     */
    private Date create_date;

    /**
     * 更新日時
     */
    private Date update_date;

    /**
     * 削除フラグ
     */
    private boolean delflg;

}
