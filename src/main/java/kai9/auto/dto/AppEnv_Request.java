package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 環境マスタ :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class AppEnv_Request implements Serializable {

    /**
     * 更新回数
     */
    private Integer modify_count;

    /**
     * DIR_パラメータシート
     */
    private String dir_parametersheet;

    /**
     * DIR_処理シナリオ
     */
    private String dir_processingscenario;

    /**
     * DIR_処理済シナリオ
     */
    private String dir_processedscenario;

    /**
     * DIR_テストデータ
     */
    private String dir_testdata;

    /**
     * DIR_一定期間保存
     */
    private String dir_retentionperiod;

    /**
     * DIR_世代管理
     */
    private String dir_generationmanagement;

    /**
     * DIR_WEBスクリーンショット
     */
    private String dir_web_screenshot;

    /**
     * DIR_TMP
     */
    private String dir_tmp;

    /**
     * セレニウム ウェブドライバ(edge)
     */
    private String path_webdriver_edge;

    /**
     * セレニウム ウェブドライバ(firefox)
     */
    private String path_webdriver_firefox;

    /**
     * セレニウム ウェブドライバ(chrome)
     */
    private String path_webdriver_chrome;

    /**
     * ブラウザのバイナリパス(edge)
     */
    private String path_binary_edge;

    /**
     * ブラウザのバイナリパス(firefox)
     */
    private String path_binary_firefox;

    /**
     * ブラウザのバイナリパス(chrome)
     */
    private String path_binary_chrome;

    /**
     * DIR_テストデータ略称
     */
    private String dir_testdataabbreviation;

    /**
     * DIR_一定期間保存略称
     */
    private String dir_retentionperiodabbreviation;

    /**
     * DIR_TMP略称
     */
    private String dir_tmpabbreviation;

    /**
     * [経過日数]tmpフォルダ削除
     */
    private Integer del_days_tmp;

    /**
     * [経過日数]一定期間保存
     */
    private Integer del_days_retentionperiod;

    /**
     * [経過日数]世代管理
     */
    private Integer del_days_generationmanagement;

    /**
     * [経過日数]処理済シナリオ
     */
    private Integer del_days_processedscenario;

    /**
     * [経過日数]ログ
     */
    private Integer del_days_log;

    /**
     * [経過日数]処理履歴
     */
    private Integer del_days_processhistory;

    /**
     * [経過日数]履歴レコード
     */
    private Integer del_days_historyrecord;

    /**
     * [経過日数]WEBスクショ
     */
    private Integer del_days_web_screenshot;

    /**
     * 世代管理数
     */
    private Integer num_gm;

    /**
     * タイムアウト分数
     */
    private Integer timeout_m;

    /**
     * ログ打ち切り行数
     */
    private Integer log_cut;

    /**
     * 更新者
     */
    private Integer update_u_id;

    /**
     * 更新日時
     */
    private Date update_date;

    /**
     * 自動SVN更新
     */
    private Boolean tc_svn_update;

}
