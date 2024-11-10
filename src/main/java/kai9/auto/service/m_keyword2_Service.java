package kai9.auto.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.auto.dto.m_keyword2_Request;
import kai9.auto.model.m_keyword2;
import kai9.auto.repository.m_keyword2_Repository;

/**
 * 処理設定_親 :サービス
 */
@Service
public class m_keyword2_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private m_keyword2_Repository m_keyword2_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<m_keyword2> searchAll() {
        String sql = "select * from m_keyword2_a order by keyword";
        RowMapper<m_keyword2> rowMapper = new BeanPropertyRowMapper<m_keyword2>(m_keyword2.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public m_keyword2 create(m_keyword2_Request m_keyword2_request) throws CloneNotSupportedException {
        m_keyword2 m_keyword2 = new m_keyword2();
        if (m_keyword2_request.getModify_count() == 0) {
            m_keyword2.setModify_count(1);// 新規登録は1固定
        } else {
            m_keyword2.setModify_count(m_keyword2_request.getModify_count());
        }
        m_keyword2.setNo(m_keyword2_request.getNo());
        m_keyword2.setKeyword(m_keyword2_request.getKeyword());
        m_keyword2.setFunc_name(m_keyword2_request.getFunc_name());
        m_keyword2.setOk_result(m_keyword2_request.getOk_result());
        m_keyword2.setNg_result(m_keyword2_request.getNg_result());
        m_keyword2.setParam1(m_keyword2_request.getParam1());
        m_keyword2.setParam2(m_keyword2_request.getParam2());
        m_keyword2.setParam3(m_keyword2_request.getParam3());
        m_keyword2.setVariable1(m_keyword2_request.getVariable1());
        m_keyword2.setBikou(m_keyword2_request.getBikou());
        m_keyword2 = m_keyword2_rep.save(m_keyword2);

        // 履歴の登録:SQL実行
        String sql = "insert into m_keyword2_b select * from m_keyword2_a where keyword = ?";
        jdbcTemplate.update(sql, m_keyword2.getKeyword());

        return m_keyword2;
    }

    /**
     * 主キー検索
     */
    public m_keyword2 findById(String keyword) {
        String sql = "select * from m_keyword2_a where keyword = ?";
        RowMapper<m_keyword2> rowMapper = new BeanPropertyRowMapper<m_keyword2>(m_keyword2.class);
        try {
            m_keyword2 m_keyword2 = jdbcTemplate.queryForObject(sql, rowMapper, keyword);
            return m_keyword2;
        } catch (EmptyResultDataAccessException e) {
            // 検索対象が無い場合はnullを返す
            return null;
        }
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

}
