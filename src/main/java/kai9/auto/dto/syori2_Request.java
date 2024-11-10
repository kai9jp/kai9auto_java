package kai9.auto.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 処理設定_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori2_Request implements Serializable {

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
     * 実行順
     */
    private int run_order;

    /**
     * シート名
     */
    private String sheetname;

    /**
     * 実施FLG
     */
    @JsonProperty("is_do")
    private boolean is_do;

    /**
     * 正常/異常
     */
    @JsonProperty("is_normal")
    private boolean is_normal;

    /**
     * NGで停止
     */
    private boolean ng_stop;

    /**
     * 強制実行
     */
    private boolean forced_run;

    /**
     * シナリオ
     */
    private String scenario;

    /**
     * 処理概要
     */
    private String s_outline;

    /**
     * STEP数
     */
    private int step_count;

    /**
     * 列番号)ステップ
     */
    private int col_step;

    /**
     * 列番号)処理内容
     */
    private int col_proc_cont;

    /**
     * 列番号)コメント
     */
    private int col_comment;

    /**
     * 列番号)集計数
     */
    private int col_sum;

    /**
     * 列番号)キーワード
     */
    private int col_keyword;

    /**
     * 列番号)値1
     */
    private int col_value1;

    /**
     * 列番号)値2
     */
    private int col_value2;

    /**
     * 列番号)値3
     */
    private int col_value3;

    /**
     * 列番号)変数
     */
    private int col_variable1;

    /**
     * 列番号)想定結果
     */
    private int col_ass_result;

    /**
     * 列番号)実施結果
     */
    private int col_run_result;

    /**
     * 列番号)想定相違
     */
    private int col_ass_diff;

    /**
     * 列番号)NGで停止
     */
    private int col_ng_stop;

    /**
     * 列番号)開始
     */
    private int col_start_time;

    /**
     * 列番号)終了
     */
    private int col_end_time;

    /**
     * 列番号)所要時間
     */
    private int col_sum_time;

    /**
     * 列番号)ログ
     */
    private int col_log;

    /**
     * 行番号)詳細ログ
     */
    private int row_log;

    private syori3_Request[] syori3s;

    public syori2_Request() {
        syori3s = new syori3_Request[] {};
    }

}
