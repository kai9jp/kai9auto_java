package kai9.auto.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 環境マスタ :モデル
 */
@Entity
@Data
@Table(name = "app_env_a")
public class AppEnv {

    /**
     * 更新回数
     */
    @Id
    @Column(name = "modify_count")
    private Integer modify_count;

    /**
     * DIR_パラメータシート
     */
    @Column(name = "dir_parametersheet")
    private String dir_parametersheet;

    /**
     * DIR_処理シナリオ
     */
    @Column(name = "dir_processingscenario")
    private String dir_processingscenario;

    /**
     * DIR_処理済シナリオ
     */
    @Column(name = "dir_processedscenario")
    private String dir_processedscenario;

    /**
     * DIR_テストデータ
     */
    @Column(name = "dir_testdata")
    private String dir_testdata;

    /**
     * DIR_一定期間保存
     */
    @Column(name = "dir_retentionperiod")
    private String dir_retentionperiod;

    /**
     * DIR_世代管理
     */
    @Column(name = "dir_generationmanagement")
    private String dir_generationmanagement;

    /**
     * DIR_WEBスクリーンショット
     */
    @Column(name = "dir_web_screenshot")
    private String dir_web_screenshot;

    /**
     * DIR_TMP
     */
    @Column(name = "dir_tmp")
    private String dir_tmp;

    /**
     * セレニウム ウェブドライバ(edge)
     */
    @Column(name = "path_webdriver_edge")
    private String path_webdriver_edge;

    /**
     * セレニウム ウェブドライバ(firefox)
     */
    @Column(name = "path_webdriver_firefox")
    private String path_webdriver_firefox;

    /**
     * セレニウム ウェブドライバ(chrome)
     */
    @Column(name = "path_webdriver_chrome")
    private String path_webdriver_chrome;

    /**
     * ブラウザのバイナリパス(edge)
     */
    @Column(name = "path_binary_edge")
    private String path_binary_edge;

    /**
     * ブラウザのバイナリパス(firefox)
     */
    @Column(name = "path_binary_firefox")
    private String path_binary_firefox;

    /**
     * ブラウザのバイナリパス(chrome)
     */
    @Column(name = "path_binary_chrome")
    private String path_binary_chrome;

    /**
     * DIR_テストデータ略称
     */
    @Column(name = "dir_testdataabbreviation")
    private String dir_testdataabbreviation;

    /**
     * DIR_一定期間保存略称
     */
    @Column(name = "dir_retentionperiodabbreviation")
    private String dir_retentionperiodabbreviation;

    /**
     * DIR_TMP略称
     */
    @Column(name = "dir_tmpabbreviation")
    private String dir_tmpabbreviation;

    /**
     * [経過日数]tmpフォルダ削除
     */
    @Column(name = "del_days_tmp")
    private Integer del_days_tmp;

    /**
     * [経過日数]一定期間保存
     */
    @Column(name = "del_days_retentionperiod")
    private Integer del_days_retentionperiod;

    /**
     * [経過日数]世代管理
     */
    @Column(name = "del_days_generationmanagement")
    private Integer del_days_generationmanagement;

    /**
     * [経過日数]処理済シナリオ
     */
    @Column(name = "del_days_processedscenario")
    private Integer del_days_processedscenario;

    /**
     * [経過日数]ログ
     */
    @Column(name = "del_days_log")
    private Integer del_days_log;

    /**
     * [経過日数]処理履歴
     */
    @Column(name = "del_days_processhistory")
    private Integer del_days_processhistory;

    /**
     * [経過日数]履歴レコード
     */
    @Column(name = "del_days_historyrecord")
    private Integer del_days_historyrecord;

    /**
     * [経過日数]WEBスクショ
     */
    @Column(name = "del_days_web_screenshot")
    private Integer del_days_web_screenshot;

    /**
     * 世代管理数
     */
    @Column(name = "num_gm")
    private Integer num_gm;

    /**
     * タイムアウト分数
     */
    @Column(name = "timeout_m")
    private Integer timeout_m;

    /**
     * ログ打ち切り行数
     */
    @Column(name = "log_cut")
    private Integer log_cut;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private Integer update_u_id;

    /**
     * 更新日時
     */
    @Column(name = "update_date")
    private Date update_date;

    /**
     * 自動SVN更新
     */
    @Column(name = "tc_svn_update")
    private Boolean tc_svn_update;

    public AppEnv() {
    }// コンストラクタ スタブ

}
