package kai9.auto.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.auto.dto.syori3_Request;
import kai9.auto.model.syori3;
import kai9.auto.repository.syori3_Repository;

/**
 * 処理設定_親 :サービス
 */
@Service
public class syori3_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private syori3_Repository syori3_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori3> searchAll() {
        String sql = "select * from syori3_a order by s1_id,s2_id,s3_id";
        RowMapper<syori3> rowMapper = new BeanPropertyRowMapper<syori3>(syori3.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public syori3 create(syori3_Request syori3_request) throws CloneNotSupportedException {
        syori3 syori3 = new syori3();
        syori3.setS1_id(syori3_request.getS1_id());
        syori3.setModify_count(syori3_request.getModify_count());
        syori3.setS2_id(syori3_request.getS2_id());
        syori3.setS3_id(syori3_request.getS3_id());
        syori3.setRow(syori3_request.getRow());
        syori3.setStep(syori3_request.getStep());
        syori3.setProc_cont(syori3_request.getProc_cont());
        syori3.setComment(syori3_request.getComment());
        syori3.setSum(syori3_request.getSum());
        syori3.setKeyword(syori3_request.getKeyword());
        syori3.setValue1(syori3_request.getValue1());
        syori3.setValue2(syori3_request.getValue2());
        syori3.setValue3(syori3_request.getValue3());
        syori3.setVariable1(syori3_request.getVariable1());
        syori3.setAss_result(syori3_request.getAss_result());
        syori3.setRun_result(syori3_request.getRun_result());
        syori3.setNg_stop(syori3_request.isNg_stop());
        syori3.setForced_run(syori3_request.isForced_run());
        syori3.setStart_time(syori3_request.getStart_time());
        syori3.setEnd_time(syori3_request.getEnd_time());
        syori3 = syori3_rep.save(syori3);

        // 履歴の登録:SQL実行
        String sql = "insert into syori3_b select * from syori3_a where s1_id = ? and s2_id = ? and s3_id = ?";
        jdbcTemplate.update(sql, syori3.getS1_id(), syori3.getS2_id(), syori3.getS3_id());

        return syori3;
    }

    /**
     * 更新
     */
    public syori3 update(syori3_Request syori3_Request) {
        syori3 syori3 = findById(syori3_Request.getS1_id(), syori3_Request.getS2_id(), syori3_Request.getS3_id());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori3.getS1_id() != syori3_Request.getS1_id()) IsChange = true;
        if (syori3.getS2_id() != syori3_Request.getS2_id()) IsChange = true;
        if (syori3.getS3_id() != syori3_Request.getS3_id()) IsChange = true;
        if (syori3.getRow() != syori3_Request.getRow()) IsChange = true;
        if (syori3.getStep() != syori3_Request.getStep()) IsChange = true;
        if (!syori3.getProc_cont().equals(syori3_Request.getProc_cont())) IsChange = true;
        if (!syori3.getComment().equals(syori3_Request.getComment())) IsChange = true;
        if (syori3.getSum() != syori3_Request.getSum()) IsChange = true;
        if (!syori3.getKeyword().equals(syori3_Request.getKeyword())) IsChange = true;
        if (!syori3.getValue1().equals(syori3_Request.getValue1())) IsChange = true;
        if (!syori3.getValue2().equals(syori3_Request.getValue2())) IsChange = true;
        if (!syori3.getValue3().equals(syori3_Request.getValue3())) IsChange = true;
        if (!syori3.getVariable1().equals(syori3_Request.getVariable1())) IsChange = true;
        if (!syori3.getAss_result().equals(syori3_Request.getAss_result())) IsChange = true;
        if (!syori3.getRun_result().equals(syori3_Request.getRun_result())) IsChange = true;
        if (syori3.getNg_stop() != syori3_Request.isNg_stop()) IsChange = true;
        if (syori3.getForced_run() != syori3_Request.isForced_run()) IsChange = true;
        if (syori3.getStart_time() != syori3_Request.getStart_time()) IsChange = true;
        if (syori3.getEnd_time() != syori3_Request.getEnd_time()) IsChange = true;
        if (!IsChange) return syori3;

        // 更新処理
        syori3.setModify_count(syori3_Request.getModify_count() + 1);// 更新回数+1
        syori3.setRow(syori3_Request.getRow());
        syori3.setStep(syori3_Request.getStep());
        syori3.setProc_cont(syori3_Request.getProc_cont());
        syori3.setComment(syori3_Request.getComment());
        syori3.setSum(syori3_Request.getSum());
        syori3.setKeyword(syori3_Request.getKeyword());
        syori3.setValue1(syori3_Request.getValue1());
        syori3.setValue2(syori3_Request.getValue2());
        syori3.setValue3(syori3_Request.getValue3());
        syori3.setVariable1(syori3_Request.getVariable1());
        syori3.setAss_result(syori3_Request.getAss_result());
        syori3.setRun_result(syori3_Request.getRun_result());
        syori3.setNg_stop(syori3_Request.isNg_stop());
        syori3.setForced_run(syori3_Request.isForced_run());
        syori3.setStart_time(syori3_Request.getStart_time());
        syori3.setEnd_time(syori3_Request.getEnd_time());
        syori3 = syori3_rep.save(syori3);

        // 履歴の登録:SQL実行
        String sql = "insert into syori3_b select * from syori3_a where s1_id = ? and s2_id = ? and s3_id = ?";
        jdbcTemplate.update(sql, syori3.getS1_id(), syori3.getS2_id(), syori3.getS3_id());

        return syori3;
    }

    /**
     * 削除
     */
    public syori3 delete(syori3_Request syori3_Request) {
        syori3 syori3 = findById(syori3_Request.getS1_id(), syori3_Request.getS2_id(), syori3_Request.getS3_id());
        syori3.setModify_count(syori3_Request.getModify_count() + 1);// 更新回数+1
        syori3 = syori3_rep.save(syori3);

        // 履歴の登録:SQL実行
        String sql = "insert into syori3_b select * from syori3_a where s1_id = ? and s2_id = ? and s3_id = ?";
        jdbcTemplate.update(sql, syori3.getS1_id(), syori3.getS2_id(), syori3.getS3_id());

        return syori3;
    }

    /**
     * 主キー検索
     */
    public syori3 findById(int s1_id, int s2_id, int s3_id) {
        String sql = "select * from syori3_a where s1_id = ? and s2_id = ? and s3_id = ?";
        RowMapper<syori3> rowMapper = new BeanPropertyRowMapper<syori3>(syori3.class);
        syori3 syori3 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, s2_id, s3_id);
        return syori3;
    }

    /**
     * ログインIDからユーザIDを取得
     */
    public int getUserIDByLoginID(String login_id) {
        String sql = "select * from m_user_a where login_id = ?";
        Map<String, Object> map = jdbcTemplate_com.queryForMap(sql, login_id);
        int user_id = (int) map.get("user_id");
        return user_id;
    }

    /**
     * 物理削除
     */
    public void delete(int s1_id, int s2_id, int s3_id) {
        syori3 syori3 = findById(s1_id, s2_id, s3_id);
        syori3_rep.delete(syori3);
    }
}
