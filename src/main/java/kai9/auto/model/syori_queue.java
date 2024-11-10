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
 * 複合キー用のクラス(処理キュー)
 */
@Embeddable
@Data
@SuppressWarnings("serial")
class syori_queue_key implements Serializable {
    @Embedded
    private int s1_id;
    private String run_host;
    private Date update_date;
}

/**
 * 処理キュー :モデル
 */
@Entity
@Data
@Table(name = "syori_queue")
@IdClass(syori_queue_key.class)
public class syori_queue {

    /**
     * 処理No1
     */
    @Id
    @Column(name = "s1_id")
    private Integer s1_id;

    /**
     * 処理No2リスト
     */
    @Column(name = "s2_ids")
    private String s2_ids;

    /**
     * 処理No3リスト
     */
    @Column(name = "s3_ids")
    private String s3_ids;

    /**
     * 実行ホスト
     */
    @Id
    @Column(name = "run_host")
    private String run_host;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private Integer update_u_id;

    /**
     * 更新日時
     */
    @Id
    @Column(name = "update_date")
    private Date update_date;

    public syori_queue() {
    }

}
