package kai9.auto.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.auto.dto.syori1_Request;
import kai9.auto.model.syori1;
import kai9.auto.repository.syori1_Repository;

/**
 * 処理設定_親 :サービス
 */
@Service
public class syori1_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    private syori1_Repository syori1_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori1> searchAll() {
        String sql = "select * from syori1_a order by s1_id";
        RowMapper<syori1> rowMapper = new BeanPropertyRowMapper<syori1>(syori1.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     * 
     * @throws SQLException
     * @throws IOException
     * @throws DataAccessException
     */
    public syori1 create(syori1_Request syori1_request) throws CloneNotSupportedException, SQLException, InvocationTargetException, IllegalAccessException, DataAccessException, IOException {
        syori1 syori1 = new syori1();
        syori1.setS1_id(syori1_request.getS1_id());
        syori1.setModify_count(1);// 新規登録は1固定
        syori1.setS1_name(syori1_request.getS1_name());
        syori1.setRun_host(syori1_request.getRun_host());
        syori1.setRun_timing(syori1_request.getRun_timing());
        syori1.setExecute_ip(syori1_request.getExecute_ip());
        syori1.setExecute_port(syori1_request.getExecute_port());
        syori1.setExecute_date(syori1_request.getExecute_date());
        syori1.setApi_url(syori1_request.getApi_url());
        syori1.setBikou(syori1_request.getBikou());
        if (syori1_request.getS_excel() != null) syori1.setS_excel(syori1_request.getS_excel());
        syori1.setS_excel_filename(syori1_request.getS_excel_filename());
        syori1.setCol_s1_name(syori1_request.getCol_s1_name());
        syori1.setCol_s1_id(syori1_request.getCol_s1_id());
        syori1.setCol_run_timing(syori1_request.getCol_run_timing());
        syori1.setCol_run_parameter(syori1_request.getCol_run_parameter());
        syori1.setCol_bikou(syori1_request.getCol_bikou());
        syori1.setCol_run_order(syori1_request.getCol_run_order());
        syori1.setCol_sheetname(syori1_request.getCol_sheetname());
        syori1.setCol_is_do(syori1_request.getCol_is_do());
        syori1.setCol_is_normal(syori1_request.getCol_is_normal());
        syori1.setCol_r_start_time(syori1_request.getCol_r_start_time());
        syori1.setCol_r_end_time(syori1_request.getCol_r_end_time());
        syori1.setCol_result(syori1_request.getCol_result());
        syori1.setCol_ng_stop(syori1_request.getCol_ng_stop());
        syori1.setCol_scenario(syori1_request.getCol_scenario());
        syori1.setCol_s_outline(syori1_request.getCol_s_outline());
        syori1.setDelflg(syori1_request.isDelflg());
        syori1.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori1.setUpdate_u_id(user_id);

        // 番号が0の場合は自動採番、指定された場合は
        if (syori1.getS1_id() == 0) {
            // 自動採番
            syori1 = syori1_rep.save(syori1);
        } else {
            // ダイレクトSQL(ChatGPT作)
            String sql = "INSERT INTO syori1_a (s1_id, modify_count, s1_name, run_host, run_timing, execute_ip, execute_port, execute_date, api_url, bikou, s_excel, s_excel_filename, col_s1_name, col_s1_id, col_run_host,col_run_timing, col_run_parameter, col_bikou, col_run_order, col_sheetname, col_is_do, col_is_normal, col_r_start_time, col_r_end_time, col_result, col_ng_stop, col_scenario, col_s_outline, update_u_id, update_date, delflg) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, syori1.getS1_id(), syori1.getModify_count(), syori1.getS1_name(), syori1.getRun_host(), syori1.getRun_timing(), syori1.getExecute_ip(), syori1.getExecute_port(), syori1.getExecute_date(), syori1.getApi_url(), syori1.getBikou(), syori1.getS_excel(), syori1.getS_excel_filename(), syori1.getCol_s1_name(), syori1.getCol_s1_id(), syori1.getCol_run_host(), syori1.getCol_run_timing(), syori1.getCol_run_parameter(), syori1.getCol_bikou(), syori1.getCol_run_order(), syori1.getCol_sheetname(), syori1.getCol_is_do(), syori1.getCol_is_normal(), syori1.getCol_r_start_time(), syori1.getCol_r_end_time(), syori1.getCol_result(), syori1.getCol_ng_stop(), syori1.getCol_scenario(), syori1.getCol_s_outline(), syori1.getUpdate_u_id(), syori1.getUpdate_date(), syori1.getDelflg());

            // 指定した事でシーケンスのルールが満たされなくなるので、シーケンスを更新する
            // 次に採番予定の値を確認する
            sql = "SELECT last_value FROM syori1_a_s1_id_seq";
            int s1_id_A = jdbcTemplate.queryForObject(sql, int.class) + 1;

            // 最大値を確認する
            sql = "SELECT MAX(s1_id) AS next_id FROM syori1_a";
            int s1_id_B = jdbcTemplate.queryForObject(sql, int.class);

            if (s1_id_A != s1_id_B) {
                // 次に採番予定の値を変更する
                sql = "SELECT setval('syori1_a_s1_id_seq', " + s1_id_B + ")";
                jdbcTemplate.execute(sql);
            }
        }

        // 履歴の登録:SQL実行
        String sql = "insert into syori1_b select * from syori1_a where s1_id = ?";
        jdbcTemplate.update(sql, syori1.getS1_id());

        return syori1;
    }

    /**
     * 更新
     */
    public syori1 update(syori1_Request syori1_Request) {
        syori1 syori1 = findById(syori1_Request.getS1_id());
        // 更新処理
        syori1.setModify_count(syori1_Request.getModify_count() + 1);// 更新回数+1
        syori1.setS1_name(syori1_Request.getS1_name());
        syori1.setRun_host(syori1_Request.getRun_host());
        syori1.setRun_timing(syori1_Request.getRun_timing());
        syori1.setExecute_ip(syori1_Request.getExecute_ip());
        syori1.setExecute_port(syori1_Request.getExecute_port());
        syori1.setExecute_date(syori1_Request.getExecute_date());
        syori1.setApi_url(syori1_Request.getApi_url());
        syori1.setBikou(syori1_Request.getBikou());
        if (syori1_Request.getS_excel() != null) syori1.setS_excel(syori1_Request.getS_excel());
        syori1.setS_excel_filename(syori1_Request.getS_excel_filename());
        syori1.setCol_s1_name(syori1_Request.getCol_s1_name());
        syori1.setCol_s1_id(syori1_Request.getCol_s1_id());
        syori1.setCol_run_host(syori1_Request.getCol_run_host());
        syori1.setCol_run_timing(syori1_Request.getCol_run_timing());
        syori1.setCol_run_parameter(syori1_Request.getCol_run_parameter());
        syori1.setCol_bikou(syori1_Request.getCol_bikou());
        syori1.setCol_run_order(syori1_Request.getCol_run_order());
        syori1.setCol_sheetname(syori1_Request.getCol_sheetname());
        syori1.setCol_is_do(syori1_Request.getCol_is_do());
        syori1.setCol_is_normal(syori1_Request.getCol_is_normal());
        syori1.setCol_r_start_time(syori1_Request.getCol_r_start_time());
        syori1.setCol_r_end_time(syori1_Request.getCol_r_end_time());
        syori1.setCol_result(syori1_Request.getCol_result());
        syori1.setCol_ng_stop(syori1_Request.getCol_ng_stop());
        syori1.setCol_scenario(syori1_Request.getCol_scenario());
        syori1.setCol_s_outline(syori1_Request.getCol_s_outline());
        syori1.setDelflg(syori1_Request.isDelflg());
        syori1.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori1.setUpdate_u_id(user_id);

        // delete & insert
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("s1_id", syori1_Request.getS1_id());
        namedJdbcTemplate.update("DELETE FROM syori2_a where s1_id = :s1_id", params);
        namedJdbcTemplate.update("DELETE FROM syori3_a where s1_id = :s1_id", params);

        syori1 = syori1_rep.save(syori1);

        // 履歴の登録:SQL実行
        String sql = "insert into syori1_b select * from syori1_a where s1_id = ?";
        jdbcTemplate.update(sql, syori1.getS1_id());

        return syori1;
    }

    /**
     * 削除
     */
    public syori1 delete(syori1_Request syori1_Request) {
        syori1 syori1 = findById(syori1_Request.getS1_id());
        syori1.setDelflg(!syori1_Request.isDelflg());
        syori1.setModify_count(syori1_Request.getModify_count() + 1);// 更新回数+1

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori1.setUpdate_u_id(user_id);
        syori1 = syori1_rep.save(syori1);

        // 履歴の登録:SQL実行
        String sql = "insert into syori1_b select * from syori1_a where s1_id = ?";
        jdbcTemplate.update(sql, syori1.getS1_id());

        return syori1;
    }

    /**
     * 主キー検索
     */
    public syori1 findById(int s1_id) {
        String sql = "select * from syori1_a where s1_id = ?";
        RowMapper<syori1> rowMapper = new BeanPropertyRowMapper<>(syori1.class);
        List<syori1> results = jdbcTemplate.query(sql, rowMapper, s1_id);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
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

    /**
     * 物理削除
     */
    public void delete(int s1_id) {
        String sql = "select * from syori1_a where s1_id = ?";
        RowMapper<syori1> rowMapper = new BeanPropertyRowMapper<syori1>(syori1.class);
        syori1 syori1 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id);
        syori1_rep.delete(syori1);
    }
}
