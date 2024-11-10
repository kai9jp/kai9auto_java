package kai9.auto.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import lombok.Data;

/**
 * 複合キー用のクラス(処理設定_親)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori2_key implements Serializable {
    @Embedded
    private Integer s1_id;
    private Integer s2_id;
}

/**
 * 処理設定_親 :モデル
 */
@Entity
@Data
@Table(name = "syori2_a")
@IdClass(syori2_key.class)
public class syori2 {

    /**
     * 処理No_1
     */
    @Id
    @Column(name = "s1_id")
    private Integer s1_id;

    /**
     * 更新回数
     */
    @Column(name = "modify_count")
    private Integer modify_count;

    /**
     * 処理No_2
     */
    @Id
    @Column(name = "s2_id")
    private Integer s2_id;

    /**
     * 実行順
     */
    @Column(name = "run_order")
    private Integer run_order;

    /**
     * シート名
     */
    @Column(name = "sheetname")
    private String sheetname;

    /**
     * 実施FLG
     */
    @Column(name = "is_do")
    private Boolean is_do;

    /**
     * 正常/異常
     */
    @Column(name = "is_normal")
    private Boolean is_normal;

    /**
     * NGで停止
     */
    @Column(name = "ng_stop")
    private Boolean ng_stop;

    /**
     * 強制実行
     */
    @Column(name = "forced_run")
    private Boolean forced_run;

    /**
     * シナリオ
     */
    @Column(name = "scenario")
    private String scenario;

    /**
     * 処理概要
     */
    @Column(name = "s_outline")
    private String s_outline;

    /**
     * STEP数
     */
    @Column(name = "step_count")
    private Integer step_count;

    /**
     * 列番号)ステップ
     */
    @Column(name = "col_step")
    private Integer col_step;

    /**
     * 列番号)処理内容
     */
    @Column(name = "col_proc_cont")
    private Integer col_proc_cont;

    /**
     * 列番号)コメント
     */
    @Column(name = "col_comment")
    private Integer col_comment;

    /**
     * 列番号)集計数
     */
    @Column(name = "col_sum")
    private Integer col_sum;

    /**
     * 列番号)キーワード
     */
    @Column(name = "col_keyword")
    private Integer col_keyword;

    /**
     * 列番号)値1
     */
    @Column(name = "col_value1")
    private Integer col_value1;

    /**
     * 列番号)値2
     */
    @Column(name = "col_value2")
    private Integer col_value2;

    /**
     * 列番号)値3
     */
    @Column(name = "col_value3")
    private Integer col_value3;

    /**
     * 列番号)変数
     */
    @Column(name = "col_variable1")
    private Integer col_variable1;

    /**
     * 列番号)想定結果
     */
    @Column(name = "col_ass_result")
    private Integer col_ass_result;

    /**
     * 列番号)実施結果
     */
    @Column(name = "col_run_result")
    private Integer col_run_result;

    /**
     * 列番号)想定相違
     */
    @Column(name = "col_ass_diff")
    private Integer col_ass_diff;

    /**
     * 列番号)NGで停止
     */
    @Column(name = "col_ng_stop")
    private Integer col_ng_stop;

    /**
     * 列番号)開始
     */
    @Column(name = "col_start_time")
    private Integer col_start_time;

    /**
     * 列番号)終了
     */
    @Column(name = "col_end_time")
    private Integer col_end_time;

    /**
     * 列番号)所要時間
     */
    @Column(name = "col_sum_time")
    private Integer col_sum_time;

    /**
     * 列番号)ログ
     */
    @Column(name = "col_log")
    private Integer col_log;

    /**
     * 行番号)詳細ログ
     */
    @Column(name = "row_log")
    private Integer row_log;

    @Transient
    public transient Set<syori3> syori3s = new TreeSet<syori3>();

    public syori2() {
    }// コンストラクタ スタブ

}
