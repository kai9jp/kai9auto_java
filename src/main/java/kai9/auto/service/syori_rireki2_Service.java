package kai9.auto.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.auto.dto.syori_rireki2_Request;
import kai9.auto.model.syori_rireki2;
import kai9.auto.repository.syori_rireki2_Repository;

/**
 * 処理履歴_子 :サービス
 */
@Service
public class syori_rireki2_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private syori_rireki2_Repository syori_rireki2_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori_rireki2> searchAll() {
        String sql = "select * from syori_rireki2 order by s1_id,s_count,s2_id";
        RowMapper<syori_rireki2> rowMapper = new BeanPropertyRowMapper<syori_rireki2>(syori_rireki2.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public syori_rireki2 create(syori_rireki2 sr2) throws CloneNotSupportedException {
        syori_rireki2 syori_rireki2 = new syori_rireki2();
        syori_rireki2.setS1_id(sr2.getS1_id());
        syori_rireki2.setS_count(sr2.getS_count());
        syori_rireki2.setS2_id(sr2.getS2_id());
        syori_rireki2.setRun_order(sr2.getRun_order());
        syori_rireki2.setResult_type(sr2.getResult_type());
        syori_rireki2.setOk_count(sr2.getOk_count());
        syori_rireki2.setNg_count(sr2.getNg_count());
        syori_rireki2.setS_ng_count(sr2.getS_ng_count());
        syori_rireki2.setPercent2(sr2.getPercent2());
        syori_rireki2.setLog(sr2.getLog());
        syori_rireki2.setR_summary(sr2.getR_summary());
        syori_rireki2.setStart_time(sr2.getStart_time());
        syori_rireki2.setEnd_time(sr2.getEnd_time());
        syori_rireki2.setIs_timeout(sr2.getIs_timeout());
        syori_rireki2.setIs_suspension(sr2.getIs_suspension());
        syori_rireki2.setCreate_date(sr2.getCreate_date());
        syori_rireki2.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki2.setUpdate_u_id(user_id);
        syori_rireki2 = syori_rireki2_rep.save(syori_rireki2);

        return syori_rireki2;
    }

    /**
     * 更新
     */
    public syori_rireki2 update(syori_rireki2 sr2) {
        syori_rireki2 syori_rireki2 = findById(sr2.getS1_id(), sr2.getS_count(), sr2.getS2_id());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori_rireki2.getS1_id() != sr2.getS1_id()) IsChange = true;
        if (syori_rireki2.getS_count() != sr2.getS_count()) IsChange = true;
        if (syori_rireki2.getS2_id() != sr2.getS2_id()) IsChange = true;
        if (syori_rireki2.getRun_order() != sr2.getRun_order()) IsChange = true;
        if (syori_rireki2.getResult_type() != sr2.getResult_type()) IsChange = true;
        if (syori_rireki2.getOk_count() != sr2.getOk_count()) IsChange = true;
        if (syori_rireki2.getNg_count() != sr2.getNg_count()) IsChange = true;
        if (syori_rireki2.getS_ng_count() != sr2.getS_ng_count()) IsChange = true;
        if (syori_rireki2.getPercent2() != sr2.getPercent2()) IsChange = true;
        if (!syori_rireki2.getLog().equals(sr2.getLog())) IsChange = true;
        if (!syori_rireki2.getR_summary().equals(sr2.getR_summary())) IsChange = true;
        if (syori_rireki2.getStart_time() != sr2.getStart_time()) IsChange = true;
        if (syori_rireki2.getEnd_time() != sr2.getEnd_time()) IsChange = true;
        if (syori_rireki2.getIs_timeout() != sr2.getIs_timeout()) IsChange = true;
        if (syori_rireki2.getIs_suspension() != sr2.getIs_suspension()) IsChange = true;
        if (syori_rireki2.getCreate_date() != sr2.getCreate_date()) IsChange = true;
        if (!IsChange) return syori_rireki2;

        // 更新処理
        syori_rireki2.setRun_order(sr2.getRun_order());
        syori_rireki2.setResult_type(sr2.getResult_type());
        syori_rireki2.setOk_count(sr2.getOk_count());
        syori_rireki2.setNg_count(sr2.getNg_count());
        syori_rireki2.setS_ng_count(sr2.getS_ng_count());
        syori_rireki2.setPercent2(sr2.getPercent2());
        syori_rireki2.setLog(sr2.getLog());
        syori_rireki2.setR_summary(sr2.getR_summary());
        syori_rireki2.setStart_time(sr2.getStart_time());
        syori_rireki2.setEnd_time(sr2.getEnd_time());
        syori_rireki2.setIs_timeout(sr2.getIs_timeout());
        syori_rireki2.setIs_suspension(sr2.getIs_suspension());
        syori_rireki2.setCreate_date(sr2.getCreate_date());
        syori_rireki2.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki2.setUpdate_u_id(user_id);
        syori_rireki2 = syori_rireki2_rep.save(syori_rireki2);

        return syori_rireki2;
    }

    // 進捗率の更新
    public syori_rireki2 updatePercent(syori_rireki2 sr2) {
        syori_rireki2 syori_rireki2 = findById(sr2.getS1_id(), sr2.getS_count(), sr2.getS2_id());
        syori_rireki2.setPercent2(sr2.getPercent2());
        syori_rireki2.setUpdate_date(new Date());
        syori_rireki2 = syori_rireki2_rep.save(syori_rireki2);
        return syori_rireki2;
    }

    /**
     * 削除
     */
    public syori_rireki2 delete(syori_rireki2_Request syori_rireki2_Request) {
        syori_rireki2 syori_rireki2 = findById(syori_rireki2_Request.getS1_id(), syori_rireki2_Request.getS_count(), syori_rireki2_Request.getS2_id());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki2.setUpdate_u_id(user_id);
        syori_rireki2 = syori_rireki2_rep.save(syori_rireki2);

        return syori_rireki2;
    }

    /**
     * 主キー検索
     */
    public syori_rireki2 findById(int s1_id, int s_count, int s2_id) {
        String sql = "select * from syori_rireki2 where s1_id = ? and s_count = ? and s2_id = ?";
        RowMapper<syori_rireki2> rowMapper = new BeanPropertyRowMapper<syori_rireki2>(syori_rireki2.class);
        syori_rireki2 syori_rireki2 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, s_count, s2_id);
        return syori_rireki2;
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
    public void delete(int s1_id, int s_count, int s2_id) {
        syori_rireki2 syori_rireki2 = findById(s1_id, s_count, s2_id);
        syori_rireki2_rep.delete(syori_rireki2);
    }
}
