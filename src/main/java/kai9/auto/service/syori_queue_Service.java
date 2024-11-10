package kai9.auto.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import kai9.auto.dto.syori_queue_Request;
import kai9.auto.model.syori_queue;
import kai9.auto.repository.syori_queue_Repository;

/**
 * 処理キュー :サービス
 */
@Service
public class syori_queue_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    private syori_queue_Repository syori_queue_rep;

    /**
     * 新規登録
     * 
     * @throws SQLException
     * @throws IOException
     * @throws DataAccessException
     */
    public syori_queue create(syori_queue_Request syori_queue_request) throws CloneNotSupportedException, SQLException, InvocationTargetException, IllegalAccessException, DataAccessException, IOException {
        syori_queue syori_queue = new syori_queue();
        syori_queue.setS1_id(syori_queue_request.getS1_id());
        syori_queue.setS2_ids(syori_queue_request.getS2_ids());
        syori_queue.setS3_ids(syori_queue_request.getS3_ids());
        syori_queue.setRun_host(syori_queue_request.getRun_host());
        syori_queue.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_queue.setUpdate_u_id(user_id);

        // 登録
        syori_queue = syori_queue_rep.save(syori_queue);

        return syori_queue;
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
    public void delete(syori_queue syori_queue_p) {
        String sql = "select * from syori_queue where s1_id = ? and run_host = ? and update_date = ? ";
        RowMapper<syori_queue> rowMapper = new BeanPropertyRowMapper<syori_queue>(syori_queue.class);
        syori_queue syori_queue = jdbcTemplate.queryForObject(sql, rowMapper, syori_queue_p.getS1_id(), syori_queue_p.getRun_host(), syori_queue_p.getUpdate_date());
        syori_queue_rep.delete(syori_queue);
    }
}
