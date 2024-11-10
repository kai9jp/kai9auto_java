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
 * 複合キー用のクラス(処理履歴_ログ)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori_rireki_log_key implements Serializable {
    @Embedded
    private int s1_id;
    private int s_count;
    private int s2_id;
}

/**
 * 処理履歴_ログ :モデル
 */
@Entity
@Data
@Table(name = "syori_rireki_log")
@IdClass(syori_rireki_log_key.class)
public class syori_rireki_log {

    /**
     * 処理No_1
     */
    @Id
    @Column(name = "s1_id")
    private int s1_id;

    /**
     * 処理回数
     */
    @Id
    @Column(name = "s_count")
    private int s_count;

    /**
     * 処理No_2
     */
    @Id
    @Column(name = "s2_id")
    private int s2_id;

    /**
     * ログ行数
     */
    @Column(name = "l_count")
    private int l_count;

    /**
     * ログ
     */
    @Column(name = "log")
    private String log;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private int update_u_id;

    /**
     * 更新日時
     */
    @Column(name = "update_date")
    private Date update_date;

    /**
     * 削除フラグ
     */
    @Column(name = "delflg")
    private boolean delflg;

    public syori_rireki_log() {
    }// コンストラクタ スタブ

}
