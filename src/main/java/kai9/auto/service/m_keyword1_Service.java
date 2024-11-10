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

import kai9.auto.dto.m_keyword1_Request;
import kai9.auto.model.m_keyword1;
import kai9.auto.repository.m_keyword1_Repository;

/**
 * 処理設定_親 :サービス
 */
@Service
public class m_keyword1_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private m_keyword1_Repository m_keyword1_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<m_keyword1> searchAll() {
        String sql = "select * from m_keyword1_a";
        RowMapper<m_keyword1> rowMapper = new BeanPropertyRowMapper<m_keyword1>(m_keyword1.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public m_keyword1 create(m_keyword1_Request m_keyword1_request) throws CloneNotSupportedException {
        m_keyword1 m_keyword1 = new m_keyword1();
        m_keyword1.setModify_count(1);// 新規登録は1固定
        if (m_keyword1_request.getExcel() != null) m_keyword1.setExcel(m_keyword1_request.getExcel());
        m_keyword1.setExcel_filename(m_keyword1_request.getExcel_filename());
        m_keyword1.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        m_keyword1.setUpdate_u_id(user_id);
        m_keyword1 = m_keyword1_rep.save(m_keyword1);

        // 履歴の登録:SQL実行
        String sql = "insert into m_keyword1_b select * from m_keyword1_a";
        jdbcTemplate.update(sql);

        return m_keyword1;
    }

    /**
     * 更新
     */
    public m_keyword1 update(m_keyword1_Request m_keyword1_Request) {
        m_keyword1 m_keyword1 = findById(m_keyword1_Request.getModify_count());
        // 更新処理
        m_keyword1.setModify_count(m_keyword1_Request.getModify_count() + 1);// 更新回数+1
        if (m_keyword1_Request.getExcel() != null) m_keyword1.setExcel(m_keyword1_Request.getExcel());
        m_keyword1.setExcel_filename(m_keyword1_Request.getExcel_filename());
        m_keyword1.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        m_keyword1.setUpdate_u_id(user_id);

        // delete & insert
        jdbcTemplate.update("DELETE FROM m_keyword1_a");
        jdbcTemplate.update("DELETE FROM m_keyword2_a");
        m_keyword1 = m_keyword1_rep.save(m_keyword1);

        // 履歴の登録:SQL実行
        String sql = "insert into m_keyword1_b select * from m_keyword1_a";
        jdbcTemplate.update(sql);

        return m_keyword1;
    }

    /**
     * 主キー検索
     */
    public m_keyword1 findById(int modify_count) {
        String sql = "select * from m_keyword1_a where modify_count = ?";// レコードが一件しかないテーブル
        RowMapper<m_keyword1> rowMapper = new BeanPropertyRowMapper<m_keyword1>(m_keyword1.class);

        m_keyword1 m_keyword1 = jdbcTemplate.queryForObject(sql, rowMapper, modify_count);
        return m_keyword1;
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
