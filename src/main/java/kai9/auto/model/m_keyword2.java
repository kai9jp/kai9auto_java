package kai9.auto.model;

import java.io.Serializable;

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
class m_keyword2_key implements Serializable {
    @Embedded
    private String keyword;
}

/**
 * 処理設定_親 :モデル
 */
@Entity
@Data
@Table(name = "m_keyword2_a")
@IdClass(m_keyword2_key.class)
public class m_keyword2 {

    /**
     * 更新回数
     */
    @Id
    @Column(name = "modify_count")
    private int modify_count;

    /**
     * NO
     */
    @Column(name = "no")
    private int no;

    /**
     * キーワード
     */
    @Id
    @Column(name = "keyword")
    private String keyword;

    /**
     * 関数名
     */
    @Column(name = "func_name")
    private String func_name;

    /**
     * OK文言
     */
    @Column(name = "ok_result")
    private String ok_result;

    /**
     * NG文言
     */
    @Column(name = "ng_result")
    private String ng_result;

    /**
     * 第1引数
     */
    @Column(name = "param1")
    private String param1;

    /**
     * 第2引数
     */
    @Column(name = "param2")
    private String param2;

    /**
     * 第3引数
     */
    @Column(name = "param3")
    private String param3;

    /**
     * 変数
     */
    @Column(name = "variable1")
    private String variable1;

    /**
     * 備考
     */
    @Column(name = "bikou")
    private String bikou;

    public m_keyword2() {
    }// コンストラクタ スタブ

}
