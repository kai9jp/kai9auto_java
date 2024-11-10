package kai9.auto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 複合キー用のクラス(処理履歴_子)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori_rireki2_key implements Serializable {
    @Embedded
    private Integer s1_id;
    private Integer s_count;
    private Integer s2_id;
}

/**
 * 処理履歴_子 :モデル
 */
@Entity
@Data
@Table(name = "syori_rireki2")
@IdClass(syori_rireki2_key.class)
public class syori_rireki2 {

    /**
     * 処理No_1
     */
    @Id
    @Column(name = "s1_id")
    private Integer s1_id = 0;

    /**
     * 処理回数
     */
    @Id
    @Column(name = "s_count")
    private Integer s_count = 0;

    /**
     * 処理No_2
     */
    @Id
    @Column(name = "s2_id")
    private Integer s2_id = 0;

    /**
     * 実行順
     */
    @Column(name = "run_order")
    private Integer run_order = 0;

    /**
     * 結果(0:想定通り、1:想定違い、2:想定通りの相違)
     */
    @Column(name = "result_type")
    private Integer result_type;

    /**
     * OK数
     */
    @Column(name = "ok_count")
    private Integer ok_count = 0;

    /**
     * NG数
     */
    @Column(name = "ng_count")
    private Integer ng_count = 0;

    /**
     * 想定NG数
     */
    @Column(name = "s_ng_count")
    private Integer s_ng_count = 0;

    /**
     * 進捗率(SYORI2)
     */
    @Column(name = "percent2")
    private Integer percent2 = 0;

    /**
     * ログ
     */
    @Column(name = "log")
    private String log = "";

    /**
     * 結果サマリ
     */
    @Column(name = "r_summary")
    private String r_summary = "";

    /**
     * 開始時刻
     */
    @Column(name = "start_time")
    private Date start_time;

    /**
     * 終了時刻
     */
    @Column(name = "end_time")
    private Date end_time;

    /**
     * タイムアウト判定
     */
    @Column(name = "is_timeout")
    private Boolean is_timeout = false;

    /**
     * 中止判定
     */
    @Column(name = "is_suspension")
    private Boolean is_suspension = false;;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private Integer update_u_id;

    /**
     * 作成日時
     */
    @Column(name = "create_date")
    private Date create_date;

    /**
     * 更新日時
     */
    @Column(name = "update_date")
    private Date update_date;

    // 実施FLG:処理2のカラム
    @JsonProperty("is_do")
    private transient Boolean is_do = false;

    // シート名:処理2のカラム
    private transient String sheetname = "";

    // NGで停止:処理2のカラム
    private transient Boolean ng_stop = false;

    // シナリオ:処理2のカラム
    private transient String scenario = "";

    // 処理概要:処理2のカラム
    private transient String s_outline = "";

    @Transient
    public transient Set<syori_rireki3> syori_rireki3s = new TreeSet<syori_rireki3>();

    /**
     * ログ追記用
     */
    @Transient
    private transient List<String> logList = new ArrayList<>();

    public syori_rireki2() {
    }// コンストラクタ スタブ

    /**
     * ログ追記用
     */
    public void addLog(String message) {
        if (log == null) {
            log = "";
        }

        logList.clear();
        logList.addAll(List.of(log.split("\n")));
        logList.add(message);

        log = String.join("\n", logList);
    }

}
