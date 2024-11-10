package kai9.auto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * 複合キー用のクラス(処理履歴_孫)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori_rireki3_key implements Serializable {
    @Embedded
    private Integer s1_id;
    private Integer s_count;
    private Integer s2_id;
    private Integer s3_id;
}

/**
 * 処理履歴_孫 :モデル
 */
@Entity
@Data
@Table(name = "syori_rireki3")
@IdClass(syori_rireki3_key.class)
public class syori_rireki3 {

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
     * 処理No_3
     */
    @Id
    @Column(name = "s3_id")
    private Integer s3_id = 0;

    /**
     * 結果(0:想定通り、1:想定違い、2:想定通りの相違)
     */
    @Column(name = "result_type")
    private Integer result_type = 0;

    /**
     * OKフラグ
     */
    @Column(name = "is_ok")
    private Boolean is_ok = false;

    /**
     * 進捗率(SYORI3)
     */
    @Column(name = "percent3")
    private Integer percent3 = 0;

    /**
     * ログ
     */
    @Column(name = "log")
    private String log = "";

    /**
     * screen_shot_filepath
     */
    @Column(name = "screen_shot_filepath")
    private String screen_shot_filepath = "";

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

    // エクセル行:処理3のカラム
    private transient Integer row = 0;

    // ステップ:処理3のカラム
    private transient Integer step = 0;

    // 処理内容:処理3のカラム
    private transient String proc_cont = "";

    // コメント:処理3のカラム
    private transient String comment = "";

    // 集計数:処理3のカラム
    private transient Integer sum = 0;

    // キーワード:処理3のカラム
    private transient String keyword = "";

    // 値1:処理3のカラム
    private transient String value1 = "";

    // 値2:処理3のカラム
    private transient String value2 = "";

    // 値3:処理3のカラム
    private transient String value3 = "";

    // 変数:処理3のカラム
    private transient String variable1 = "";

    // 想定結果:処理3のカラム
    private transient String ass_result = "";

    // 実施結果:処理3のカラム
    private transient String run_result = "";

    // NGで停止:処理3のカラム
    private transient Boolean ng_stop = false;

    // 強制実行:処理3のカラム
    private transient Boolean forced_run = false;

    // キーワード(OK文言):非DB項目
    private transient String ok_result = "";

    // キーワード(NG文言):非DB項目
    private transient String ng_result = "";

    /**
     * ログ追記用
     */
    @Transient
    private transient List<String> logList = new ArrayList<>();

    // コンストラクタ
    public syori_rireki3() {
    }

    /**
     * ログ追記用
     */
    public void addLog(String message) {
        if (log == null) {
            log = "";
        }

        logList.clear();
        if (!log.isBlank()) {
            logList.addAll(List.of(log.split("\n")));
        }
        logList.add(message);

        log = String.join("\n", logList);
    }

}
