package kai9.auto.model;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import lombok.Data;

/**
 * 処理設定_親 :モデル
 */
@Entity
@Data
@Table(name = "syori1_a")
public class syori1 {

    /**
     * 処理No_1
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s1_id")
    private Integer s1_id;

    /**
     * 更新回数
     */
    @Column(name = "modify_count")
    private Integer modify_count;

    /**
     * 処理名称
     */
    @Column(name = "s1_name")
    private String s1_name;

    /**
     * 実行ホスト
     */
    @Column(name = "run_host")
    private String run_host;

    /**
     * 実行時刻
     */
    @Column(name = "run_timing")
    private String run_timing;

    /**
     * 前回実行IP
     */
    @Column(name = "execute_ip")
    private String execute_ip;

    /**
     * 前回実行ポート
     */
    @Column(name = "execute_port")
    private String execute_port;

    /**
     * 前回実行時刻
     */
    @Column(name = "execute_date")
    private Date execute_date;

    /**
     * APIアドレス
     */
    @Column(name = "api_url")
    private String api_url;

    /**
     * 備考
     */
    @Column(name = "bikou")
    private String bikou;

    /**
     * シナリオエクセル
     */
    @Column(name = "s_excel")
    private byte[] s_excel;

    /**
     * シナリオファイル名
     */
    @Column(name = "s_excel_filename")
    private String s_excel_filename;

    /**
     * 列番号)処理名
     */
    @Column(name = "col_s1_name")
    private Integer col_s1_name;

    /**
     * 列番号)処理No
     */
    @Column(name = "col_s1_id")
    private Integer col_s1_id;

    /**
     * 列番号)実行ホスト
     */
    @Column(name = "col_run_host")
    private Integer col_run_host;

    /**
     * 列番号)実行時刻
     */
    @Column(name = "col_run_timing")
    private Integer col_run_timing;

    /**
     * 列番号)実行時引数
     */
    @Column(name = "col_run_parameter")
    private Integer col_run_parameter;

    /**
     * 列番号)備考
     */
    @Column(name = "col_bikou")
    private Integer col_bikou;

    /**
     * 列番号)実行順
     */
    @Column(name = "col_run_order")
    private Integer col_run_order;

    /**
     * 列番号)シート名
     */
    @Column(name = "col_sheetname")
    private Integer col_sheetname;

    /**
     * 列番号)実施FLG
     */
    @Column(name = "col_is_do")
    private Integer col_is_do;

    /**
     * 列番号)正常/異常
     */
    @Column(name = "col_is_normal")
    private Integer col_is_normal;

    /**
     * 列番号)実行結果_開始
     */
    @Column(name = "col_r_start_time")
    private Integer col_r_start_time;

    /**
     * 列番号)実行結果_終了
     */
    @Column(name = "col_r_end_time")
    private Integer col_r_end_time;

    /**
     * 列番号)実行結果_結果
     */
    @Column(name = "col_result")
    private Integer col_result;

    /**
     * 列番号)NGで停止
     */
    @Column(name = "col_ng_stop")
    private Integer col_ng_stop;

    /**
     * 列番号)シナリオ
     */
    @Column(name = "col_scenario")
    private Integer col_scenario;

    /**
     * 列番号)処理概要
     */
    @Column(name = "col_s_outline")
    private Integer col_s_outline;

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
     * 削除フラグ
     */
    @Column(name = "delflg")
    private Boolean delflg;

    @Transient
    public transient Set<syori2> syori2s = new TreeSet<syori2>();

    public syori1() {
    }

}
