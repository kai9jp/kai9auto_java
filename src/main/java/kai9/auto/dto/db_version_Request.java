package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * DBバージョン :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class db_version_Request implements Serializable {

    /**
     * 更新回数
     */
    private Integer modify_count;

    /**
     * DBバージョン
     */
    private Integer db_version;

    /**
     * DBバージョンAPP
     */
    private Integer db_version_app;

    /**
     * 更新者
     */
    private Integer update_u_id;

    /**
     * 更新日時
     */
    private Date update_date;

}
