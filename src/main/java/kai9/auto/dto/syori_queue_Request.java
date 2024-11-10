package kai9.auto.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 処理キュー :リクエストデータ
 */
@SuppressWarnings("serial")
@Data
public class syori_queue_Request implements Serializable {

    /**
     * 処理No1
     */
    private int s1_id;

    /**
     * 処理No2リスト
     */
    private String s2_ids;

    /**
     * 処理No3リスト
     */
    private String s3_ids;

    /**
     * 実行ホスト
     */
    private String run_host;

    /**
     * 更新者
     */
    private int update_u_id;

    /**
     * 更新日時
     */
    private Date update_date;

}