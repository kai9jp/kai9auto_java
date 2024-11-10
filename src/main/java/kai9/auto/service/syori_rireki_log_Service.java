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

import kai9.auto.dto.syori_rireki_log_Request;
import kai9.auto.model.syori_rireki_log;
import kai9.auto.repository.syori_rireki_log_Repository;

/**
 * 処理履歴_ログ :サービス
 */
@Service
public class syori_rireki_log_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private syori_rireki_log_Repository syori_rireki_log_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori_rireki_log> searchAll() {
        String sql = "select * from syori_rireki_log order by s1_id,s_count,s2_id";
        RowMapper<syori_rireki_log> rowMapper = new BeanPropertyRowMapper<syori_rireki_log>(syori_rireki_log.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    @Transactional
    public syori_rireki_log create(syori_rireki_log_Request syori_rireki_log_request) throws CloneNotSupportedException {
        syori_rireki_log syori_rireki_log = new syori_rireki_log();
        syori_rireki_log.setS1_id(syori_rireki_log_request.getS1_id());
        syori_rireki_log.setS_count(syori_rireki_log_request.getS_count());
        syori_rireki_log.setS2_id(syori_rireki_log_request.getS2_id());
        syori_rireki_log.setL_count(syori_rireki_log_request.getL_count());
        syori_rireki_log.setLog(syori_rireki_log_request.getLog());
        syori_rireki_log.setDelflg(syori_rireki_log_request.isDelflg());
        syori_rireki_log.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki_log.setUpdate_u_id(user_id);
        syori_rireki_log = syori_rireki_log_rep.save(syori_rireki_log);

        return syori_rireki_log;
    }

    /**
     * 更新
     */
    public syori_rireki_log update(syori_rireki_log_Request syori_rireki_log_Request) {
        syori_rireki_log syori_rireki_log = findById(syori_rireki_log_Request.getS1_id(), syori_rireki_log_Request.getS_count(), syori_rireki_log_Request.getS2_id());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori_rireki_log.getS1_id() != syori_rireki_log_Request.getS1_id()) IsChange = true;
        if (syori_rireki_log.getS_count() != syori_rireki_log_Request.getS_count()) IsChange = true;
        if (syori_rireki_log.getS2_id() != syori_rireki_log_Request.getS2_id()) IsChange = true;
        if (syori_rireki_log.getL_count() != syori_rireki_log_Request.getL_count()) IsChange = true;
        if (!syori_rireki_log.getLog().equals(syori_rireki_log_Request.getLog())) IsChange = true;
        if (syori_rireki_log.isDelflg() != syori_rireki_log_Request.isDelflg()) IsChange = true;
        if (!IsChange) return syori_rireki_log;

        // 更新処理
        syori_rireki_log.setL_count(syori_rireki_log_Request.getL_count());
        syori_rireki_log.setLog(syori_rireki_log_Request.getLog());
        syori_rireki_log.setDelflg(syori_rireki_log_Request.isDelflg());
        syori_rireki_log.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki_log.setUpdate_u_id(user_id);
        syori_rireki_log = syori_rireki_log_rep.save(syori_rireki_log);

        return syori_rireki_log;
    }

    /**
     * 削除
     */
    public syori_rireki_log delete(syori_rireki_log_Request syori_rireki_log_Request) {
        syori_rireki_log syori_rireki_log = findById(syori_rireki_log_Request.getS1_id(), syori_rireki_log_Request.getS_count(), syori_rireki_log_Request.getS2_id());
        syori_rireki_log.setDelflg(!syori_rireki_log_Request.isDelflg());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki_log.setUpdate_u_id(user_id);
        syori_rireki_log = syori_rireki_log_rep.save(syori_rireki_log);

        return syori_rireki_log;
    }

    /**
     * 主キー検索
     */
    public syori_rireki_log findById(int s1_id, int s_count, int s2_id) {
        String sql = "select * from syori_rireki_log where s1_id = ? and s_count = ? and s2_id = ?";
        RowMapper<syori_rireki_log> rowMapper = new BeanPropertyRowMapper<syori_rireki_log>(syori_rireki_log.class);
        syori_rireki_log syori_rireki_log = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, s_count, s2_id);
        return syori_rireki_log;
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
        syori_rireki_log syori_rireki_log = findById(s1_id, s_count, s2_id);
        syori_rireki_log_rep.delete(syori_rireki_log);
    }
}
