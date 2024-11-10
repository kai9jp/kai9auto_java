package kai9.auto.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 処理設定_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class m_keyword2_Request implements Serializable {

    /**
     * 更新回数
     */
    private int modify_count;

    /**
     * NO
     */
    private int no;

    /**
     * キーワード
     */
    private String keyword;

    /**
     * 関数名
     */
    private String func_name;

    /**
     * OK文言
     */
    private String ok_result;

    /**
     * NG文言
     */
    private String ng_result;

    /**
     * 第1引数
     */
    private String param1;

    /**
     * 第2引数
     */
    private String param2;

    /**
     * 第3引数
     */
    private String param3;

    /**
     * 変数
     */
    private String variable1;

    /**
     * 備考
     */
    private String bikou;

}
