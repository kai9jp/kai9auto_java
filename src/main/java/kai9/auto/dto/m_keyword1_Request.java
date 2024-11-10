package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 処理設定_親 :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class m_keyword1_Request implements Serializable {

    /**
     * 更新回数
     */
    private int modify_count;

    /**
     * エクセル
     */
    @JsonProperty("excel_dumy") // API連携時のエラー回避策。別名にする事でnullが入る。別途MultipartFileで受け取る。BLOB型はFILE前程で自動生成する。
    private byte[] excel;

    /**
     * エクセルファイル名
     */
    private String excel_filename;

    /**
     * 更新者
     */
    private int update_u_id;

    /**
     * 更新日時
     */
    private Date update_date;

    private m_keyword2_Request[] m_keyword2s;

    public m_keyword1_Request() {
        m_keyword2s = new m_keyword2_Request[] {};
    }

}
