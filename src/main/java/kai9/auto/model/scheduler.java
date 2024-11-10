package kai9.auto.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


@Entity
@Table(name = "scheduler_a")
@Data
/**
 * モデル
 */
public class scheduler {
    /**
     * @Id：主キーに指定する。※複合キーの場合は@EmbeddedIdを使用
     * @GeneratedValue：主キーの指定をJPAに委ねる
     * @Column：name属性でマッピングするカラム名を指定する
     * @GenerationType.IDENTITY:自動採番
     */

    /**
     * プリミティブ型ではなくラッパークラスを用いている理由 の解説
     * プリミティブ型
     * → nullを扱えない
     * → int,boolean,long
     * ラッパークラス
     * → nullを扱える
     * → Integer,Boolean,Long
     */

    /**
     * プライマリキー
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sc_pk")
    private Integer sc_pk;

    /**
     * 更新回数
     */
    @Column(name = "modify_count")
    private Integer modify_count;

    /**
     * 機能名
     */
    @Column(name = "function_name")
    private String function_name;

    /**
     * スケジュールラベル
     */
    @Column(name = "schedule_label")
    private String schedule_label;

    /**
     * スケジュールパターン
     */
    @Column(name = "schedule_pattern")
    private String schedule_pattern;

    /**
     * 実行時刻
     */
    @Column(name = "execution_time")
    private String execution_time;

    /**
     * 曜日
     */
    @Column(name = "weekdays")
    private String weekdays;

    /**
     * 週番号
     */
    @Column(name = "weeks_number")
    private String weeks_number;

    /**
     * 実行日
     */
    @Column(name = "execution_day")
    private Integer execution_day;

    /**
     * 月末N日前
     */
    @Column(name = "month_end_n_days_ago")
    private Integer month_end_n_days_ago;

    /**
     * 繰り返し間隔
     */
    @Column(name = "recurring_interval")
    private Integer recurring_interval;

    /**
     * 繰り返し終了時間
     */
    @Column(name = "recurring_end_time")
    private String recurring_end_time;

    /**
     * 備考
     */
    @Column(name = "bikou")
    private String bikou;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private Integer update_u_id;

    /**
     * 更新日時
     */
    @Column(name = "update_date")
    private java.sql.Timestamp update_date;

    /**
     * 削除フラグ
     */
    @Column(name = "delflg")
    private Boolean delflg;

    // 更新者:非DB項目
    private transient String update_user = "";


    public scheduler() {
        // コンストラクタ
        this.modify_count = 0;
        this.function_name = "";
        this.schedule_label = "";
        this.schedule_pattern = "";
        this.execution_time = "00:00";
        this.weekdays = "";
        this.weeks_number = "";
        this.execution_day = 0;
        this.month_end_n_days_ago = 0;
        this.recurring_interval = 0;
        this.recurring_end_time = "00:00";
        this.bikou = "";
        this.update_u_id = 0;
        this.update_date = new java.sql.Timestamp(System.currentTimeMillis());
        this.delflg = false;
        this.update_user = "";
    }    

}
