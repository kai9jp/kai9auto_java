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

import lombok.Data;

/**
 * 複合キー用のクラス(処理履歴_親)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori_rireki1_key implements Serializable {
    @Embedded
    private Integer s1_id;
    private Integer s_count;
}

/**
 * 処理履歴_親 :モデル
 */
@Entity
@Data
@Table(name = "syori_rireki1")
@IdClass(syori_rireki1_key.class)
public class syori_rireki1 {

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
     * 処理設定_更新回数
     */
    @Column(name = "syori_modify_count")
    private Integer syori_modify_count = 0;

    /**
     * キーワードマスタ_更新回数
     */
    @Column(name = "m_keyword_modify_count")
    private Integer m_keyword_modify_count = 0;

    /**
     * 結果(0:想定通り、1:想定違い、2:想定通りの相違)
     */
    @Column(name = "result_type")
    private Integer result_type;

    /**
     * シート数
     */
    @Column(name = "sheet_count")
    private Integer sheet_count = 0;

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
     * 開始日時
     */
    @Column(name = "start_time")
    private Date start_time;

    /**
     * 終了日時
     */
    @Column(name = "end_time")
    private Date end_time;

    /**
     * 進捗率
     */
    @Column(name = "percent")
    private Integer percent = 0;

    /**
     * タイムアウト判定
     */
    @Column(name = "is_timeout")
    private Boolean is_timeout = false;;

    /**
     * 中止判定
     */
    @Column(name = "is_suspension")
    private Boolean is_suspension = false;;

    /**
     * ログ
     */
    @Column(name = "log")
    private String log = "";

    /**
     * 実行IP
     */
    @Column(name = "execute_ip")
    private String execute_ip = "";

    /**
     * 実行ポート
     */
    @Column(name = "execute_port")
    private String execute_port = "";

    /**
     * 実行UUID
     */
    @Column(name = "execute_uuid")
    private String execute_uuid = "";

    /**
     * 処理連結NG
     */
    @Column(name = "s_linking_ng")
    private Boolean s_linking_ng = false;

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

    // 処理名称:処理1のカラム
    private transient String s1_name = "";

    // 実施者:非DB項目
    private transient String update_user = "";

    @Transient
    public transient Set<syori_rireki2> syori_rireki2s = new TreeSet<syori_rireki2>();

    /**
     * ログ追記用
     */
    @Transient
    private transient List<String> logList = new ArrayList<>();

    public syori_rireki1() {
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
