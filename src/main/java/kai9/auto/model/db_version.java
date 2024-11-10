package kai9.auto.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * DBバージョン :モデル
 */
@Entity
@Data
@Table(name = "db_version_a")
public class db_version {

    /**
     * 更新回数
     */
    @Id
    @Column(name = "modify_count")
    private Integer modify_count = 0;

    /**
     * DBバージョン
     */
    @Column(name = "db_version")
    private Integer db_version = 0;

    /**
     * DBバージョンAPP
     */
    @Column(name = "db_version_app")
    private Integer db_version_app = 0;

    /**
     * 更新者
     */
    @Column(name = "update_u_id")
    private Integer update_u_id = 0;

    /**
     * 更新日時
     */
    @Column(name = "update_date")
    private Date update_date;

    public db_version() {
    }// コンストラクタ スタブ

}
