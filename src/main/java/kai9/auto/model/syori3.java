package kai9.auto.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

/**
 * 複合キー用のクラス(処理設定_親)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori3_key implements Serializable {
    @Embedded
    private Integer s1_id;
    private Integer s2_id;
    private Integer s3_id;
}

/**
 * 処理設定_親 :モデル
 */
@Entity
@Data
@Table(name = "syori3_a")
@IdClass(syori3_key.class)
public class syori3 {

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
     * 処理No_3
     */
    @Id
    @Column(name = "s3_id")
    private Integer s3_id;

    /**
     * エクセル行
     */
    @Column(name = "row")
    private Integer row;

    /**
     * ステップ
     */
    @Column(name = "step")
    private Integer step;

    /**
     * 処理内容
     */
    @Column(name = "proc_cont")
    private String proc_cont;

    /**
     * コメント
     */
    @Column(name = "comment")
    private String comment;

    /**
     * 集計数
     */
    @Column(name = "sum")
    private Integer sum;

    /**
     * キーワード
     */
    @Column(name = "keyword")
    private String keyword;

    /**
     * 値1
     */
    @Column(name = "value1")
    private String value1;

    /**
     * 値2
     */
    @Column(name = "value2")
    private String value2;

    /**
     * 値3
     */
    @Column(name = "value3")
    private String value3;

    /**
     * 変数
     */
    @Column(name = "variable1")
    private String variable1;

    /**
     * 想定結果
     */
    @Column(name = "ass_result")
    private String ass_result;

    /**
     * 実施結果
     */
    @Column(name = "run_result")
    private String run_result;

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
     * 開始
     */
    @Column(name = "start_time")
    private Date start_time;

    /**
     * 終了
     */
    @Column(name = "end_time")
    private Date end_time;

    public syori3() {
    }// コンストラクタ スタブ

}
