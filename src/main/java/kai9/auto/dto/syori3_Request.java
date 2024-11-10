package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 処理設定_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori3_Request implements Serializable {

    /**
     * 処理No_1
     */
    private int s1_id;

    /**
     * 更新回数
     */
    private int modify_count;

    /**
     * 処理No_2
     */
    private int s2_id;

    /**
     * 処理No_3
     */
    private int s3_id;

    /**
     * エクセル行
     */
    private int row;

    /**
     * ステップ
     */
    private int step;

    /**
     * 処理内容
     */
    private String proc_cont;

    /**
     * コメント
     */
    private String comment;

    /**
     * 集計数
     */
    private int sum;

    /**
     * キーワード
     */
    private String keyword;

    /**
     * 値1
     */
    private String value1;

    /**
     * 値2
     */
    private String value2;

    /**
     * 値3
     */
    private String value3;

    /**
     * 変数
     */
    private String variable1;

    /**
     * 想定結果
     */
    private String ass_result;

    /**
     * 実施結果
     */
    private String run_result;

    /**
     * NGで停止
     */
    private boolean ng_stop;

    /**
     * 強制実行
     */
    private boolean forced_run;

    /**
     * 開始
     */
    private Date start_time;

    /**
     * 終了
     */
    private Date end_time;

}
