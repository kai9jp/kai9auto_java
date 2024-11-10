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

import kai9.auto.dto.syori_rireki1_Request;
import kai9.auto.model.syori_rireki1;
import kai9.auto.repository.syori_rireki1_Repository;

/**
 * 処理履歴_親 :サービス
 */
@Service
public class syori_rireki1_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private syori_rireki1_Repository syori_rireki1_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori_rireki1> searchAll() {
        String sql = "select * from syori_rireki1 order by s1_id,s_count";
        RowMapper<syori_rireki1> rowMapper = new BeanPropertyRowMapper<syori_rireki1>(syori_rireki1.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public syori_rireki1 create(syori_rireki1 sr1) throws CloneNotSupportedException {
        syori_rireki1 syori_rireki1 = new syori_rireki1();
        syori_rireki1.setS1_id(sr1.getS1_id());
        syori_rireki1.setS_count(sr1.getS_count());
        syori_rireki1.setSyori_modify_count(sr1.getSyori_modify_count());
        syori_rireki1.setM_keyword_modify_count(sr1.getM_keyword_modify_count());
        syori_rireki1.setResult_type(sr1.getResult_type());
        syori_rireki1.setSheet_count(sr1.getSheet_count());
        syori_rireki1.setOk_count(sr1.getOk_count());
        syori_rireki1.setNg_count(sr1.getNg_count());
        syori_rireki1.setS_ng_count(sr1.getS_ng_count());
        syori_rireki1.setStart_time(sr1.getStart_time());
        syori_rireki1.setEnd_time(sr1.getEnd_time());
        syori_rireki1.setPercent(sr1.getPercent());
        syori_rireki1.setIs_timeout(sr1.getIs_timeout());
        syori_rireki1.setIs_suspension(sr1.getIs_suspension());
        syori_rireki1.setLog(sr1.getLog());
        syori_rireki1.setExecute_ip(sr1.getExecute_ip());
        syori_rireki1.setExecute_port(sr1.getExecute_port());
        syori_rireki1.setExecute_uuid(sr1.getExecute_uuid());
        syori_rireki1.setS_linking_ng(sr1.getS_linking_ng());
        syori_rireki1.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki1.setUpdate_u_id(user_id);

        syori_rireki1 = syori_rireki1_rep.save(syori_rireki1);
        return syori_rireki1;
    }

    /**
     * 更新
     */
    public syori_rireki1 update(syori_rireki1 sr1) {
        syori_rireki1 syori_rireki1 = findById(sr1.getS1_id(), sr1.getS_count());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori_rireki1.getS1_id() != sr1.getS1_id()) IsChange = true;
        if (syori_rireki1.getS_count() != sr1.getS_count()) IsChange = true;
        if (syori_rireki1.getSyori_modify_count() != sr1.getSyori_modify_count()) IsChange = true;
        if (syori_rireki1.getM_keyword_modify_count() != sr1.getM_keyword_modify_count()) IsChange = true;
        if (syori_rireki1.getResult_type() != sr1.getResult_type()) IsChange = true;
        if (syori_rireki1.getSheet_count() != sr1.getSheet_count()) IsChange = true;
        if (syori_rireki1.getOk_count() != sr1.getOk_count()) IsChange = true;
        if (syori_rireki1.getNg_count() != sr1.getNg_count()) IsChange = true;
        if (syori_rireki1.getS_ng_count() != sr1.getS_ng_count()) IsChange = true;
        if (syori_rireki1.getStart_time() != sr1.getStart_time()) IsChange = true;
        if (syori_rireki1.getEnd_time() != sr1.getEnd_time()) IsChange = true;
        if (syori_rireki1.getPercent() != sr1.getPercent()) IsChange = true;
        if (syori_rireki1.getIs_timeout() != sr1.getIs_timeout()) IsChange = true;
        if (syori_rireki1.getIs_suspension() != sr1.getIs_suspension()) IsChange = true;
        if (!syori_rireki1.getLog().equals(sr1.getLog())) IsChange = true;
        if (!syori_rireki1.getExecute_ip().equals(sr1.getExecute_ip())) IsChange = true;
        if (!syori_rireki1.getExecute_port().equals(sr1.getExecute_port())) IsChange = true;
        if (!syori_rireki1.getExecute_uuid().equals(sr1.getExecute_uuid())) IsChange = true;
        if (!syori_rireki1.getS_linking_ng().equals(sr1.getS_linking_ng())) IsChange = true;
        if (!IsChange) return syori_rireki1;

        // 更新処理
        syori_rireki1.setSyori_modify_count(sr1.getSyori_modify_count());
        syori_rireki1.setM_keyword_modify_count(sr1.getM_keyword_modify_count());
        syori_rireki1.setResult_type(sr1.getResult_type());
        syori_rireki1.setSheet_count(sr1.getSheet_count());
        syori_rireki1.setOk_count(sr1.getOk_count());
        syori_rireki1.setNg_count(sr1.getNg_count());
        syori_rireki1.setS_ng_count(sr1.getS_ng_count());
        syori_rireki1.setStart_time(sr1.getStart_time());
        syori_rireki1.setEnd_time(sr1.getEnd_time());
        syori_rireki1.setPercent(sr1.getPercent());
        syori_rireki1.setIs_timeout(sr1.getIs_timeout());
        syori_rireki1.setIs_suspension(sr1.getIs_suspension());
        syori_rireki1.setLog(sr1.getLog());
        syori_rireki1.setExecute_ip(sr1.getExecute_ip());
        syori_rireki1.setExecute_port(sr1.getExecute_port());
        syori_rireki1.setExecute_uuid(sr1.getExecute_uuid());
        syori_rireki1.setS_linking_ng(sr1.getS_linking_ng());
        syori_rireki1.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki1.setUpdate_u_id(user_id);
        syori_rireki1 = syori_rireki1_rep.save(syori_rireki1);

        return syori_rireki1;
    }

    // 進捗率の更新
    public syori_rireki1 updatePercent(syori_rireki1 sr1) {
        syori_rireki1 syori_rireki1 = findById(sr1.getS1_id(), sr1.getS_count());
        syori_rireki1.setPercent(sr1.getPercent());
        syori_rireki1.setUpdate_date(new Date());
        syori_rireki1 = syori_rireki1_rep.save(syori_rireki1);
        return syori_rireki1;
    }

    /**
     * 削除
     */
    public syori_rireki1 delete(syori_rireki1_Request syori_rireki1_Request) {
        syori_rireki1 syori_rireki1 = findById(syori_rireki1_Request.getS1_id(), syori_rireki1_Request.getS_count());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki1.setUpdate_u_id(user_id);
        syori_rireki1 = syori_rireki1_rep.save(syori_rireki1);

        return syori_rireki1;
    }

    /**
     * 主キー検索
     */
    public syori_rireki1 findById(int s1_id, int s_count) {
        String sql = "select * from syori_rireki1 where s1_id = ? and s_count = ?";
        RowMapper<syori_rireki1> rowMapper = new BeanPropertyRowMapper<syori_rireki1>(syori_rireki1.class);
        syori_rireki1 syori_rireki1 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, s_count);
        return syori_rireki1;
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
    public void delete(int s1_id, int s_count) {
        syori_rireki1 syori_rireki1 = findById(s1_id, s_count);
        syori_rireki1_rep.delete(syori_rireki1);
    }
}
