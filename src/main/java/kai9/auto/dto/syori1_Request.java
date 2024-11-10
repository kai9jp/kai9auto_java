package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 処理設定_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori1_Request implements Serializable {

    /**
     * 処理No_1
     */
    private int s1_id;

    /**
     * 更新回数
     */
    private int modify_count;

    /**
     * 処理名称
     */
    private String s1_name;

    /**
     * 実行ホスト
     */
    private String run_host;

    /**
     * 実行時刻
     */
    private String run_timing;

    /**
     * 前回実行IP
     */
    private String execute_ip;

    /**
     * 前回実行ポート
     */
    private String execute_port;

    /**
     * 前回実行時刻
     */
    private Date execute_date;

    /**
     * APIアドレス
     */
    private String api_url;

    /**
     * 備考
     */
    private String bikou;

    /**
     * シナリオエクセル
     */
    @JsonProperty("s_excel_dumy") // API連携時のエラー回避策。別名にする事でnullが入る。別途MultipartFileで受け取る。BLOB型はFILE前程で自動生成する。
    private byte[] s_excel;

    /**
     * シナリオファイル名
     */
    private String s_excel_filename;

    /**
     * 列番号)処理名
     */
    private int col_s1_name;

    /**
     * 列番号)処理No
     */
    private int col_s1_id;

    /**
     * 列番号)実行ホスト
     */
    private int col_run_host;

    /**
     * 列番号)実行時刻
     */
    private int col_run_timing;

    /**
     * 列番号)実行時引数
     */
    private int col_run_parameter;

    /**
     * 列番号)備考
     */
    private int col_bikou;

    /**
     * 列番号)実行順
     */
    private int col_run_order;

    /**
     * 列番号)シート名
     */
    private int col_sheetname;

    /**
     * 列番号)実施FLG
     */
    private int col_is_do;

    /**
     * 列番号)正常/異常
     */
    private int col_is_normal;

    /**
     * 列番号)実行結果_開始
     */
    private int col_r_start_time;

    /**
     * 列番号)実行結果_終了
     */
    private int col_r_end_time;

    /**
     * 列番号)実行結果_結果
     */
    private int col_result;

    /**
     * 列番号)NGで停止
     */
    private int col_ng_stop;

    /**
     * 列番号)シナリオ
     */
    private int col_scenario;

    /**
     * 列番号)処理概要
     */
    private int col_s_outline;

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

    private syori2_Request[] syori2s;

    public syori1_Request() {
        syori2s = new syori2_Request[] {};
    }

}
