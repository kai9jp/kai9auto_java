package kai9.auto.model;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import lombok.Data;

/**
 * 処理設定_親 :モデル
 */
@Entity
@Data
@Table(name = "m_keyword1_a")
public class m_keyword1 {

    /**
     * 更新回数
     */
    @Id
    @Column(name = "modify_count")
    private int modify_count;

    /**
     * エクセル
     */
    @Column(name = "excel")
    private byte[] excel;

    /**
     * エクセルファイル名
     */
    @Column(name = "excel_filename")
    private String excel_filename;

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

    @Transient
    public transient Set<m_keyword2> m_keyword2s = new TreeSet<m_keyword2>();

    public m_keyword1() {
    }// コンストラクタ スタブ

}
