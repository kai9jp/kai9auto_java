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

import kai9.auto.dto.AppEnv_Request;
import kai9.auto.model.AppEnv;
import kai9.auto.repository.AppEnv_Repository;

/**
 * 環境マスタ :サービス
 */
@Service
public class AppEnv_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private AppEnv_Repository AppEnv_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<AppEnv> searchAll() {
        String sql = "select * from app_env_a order by ";
        RowMapper<AppEnv> rowMapper = new BeanPropertyRowMapper<AppEnv>(AppEnv.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public AppEnv create(AppEnv_Request AppEnv_request) throws CloneNotSupportedException {
        AppEnv AppEnv = new AppEnv();
        if (AppEnv_request.getModify_count() == 0) {
            AppEnv.setModify_count(1);// 新規登録は1固定
        } else {
            boolean IsChange = false;
            AppEnv = findById();
            if (!AppEnv.getDir_parametersheet().equals(AppEnv_request.getDir_parametersheet())) IsChange = true;
            if (!AppEnv.getDir_processingscenario().equals(AppEnv_request.getDir_processingscenario())) IsChange = true;
            if (!AppEnv.getDir_processedscenario().equals(AppEnv_request.getDir_processedscenario())) IsChange = true;
            if (!AppEnv.getDir_testdata().equals(AppEnv_request.getDir_testdata())) IsChange = true;
            if (!AppEnv.getDir_retentionperiod().equals(AppEnv_request.getDir_retentionperiod())) IsChange = true;
            if (!AppEnv.getDir_generationmanagement().equals(AppEnv_request.getDir_generationmanagement())) IsChange = true;
            if (!AppEnv.getDir_web_screenshot().equals(AppEnv_request.getDir_web_screenshot())) IsChange = true;
            if (!AppEnv.getDir_tmp().equals(AppEnv_request.getDir_tmp())) IsChange = true;
            if (!AppEnv.getPath_webdriver_edge().equals(AppEnv_request.getPath_webdriver_edge())) IsChange = true;
            if (!AppEnv.getPath_webdriver_firefox().equals(AppEnv_request.getPath_webdriver_firefox())) IsChange = true;
            if (!AppEnv.getPath_webdriver_chrome().equals(AppEnv_request.getPath_webdriver_chrome())) IsChange = true;
            if (!AppEnv.getPath_binary_edge().equals(AppEnv_request.getPath_binary_edge())) IsChange = true;
            if (!AppEnv.getPath_binary_firefox().equals(AppEnv_request.getPath_binary_firefox())) IsChange = true;
            if (!AppEnv.getPath_binary_chrome().equals(AppEnv_request.getPath_binary_chrome())) IsChange = true;
            if (!AppEnv.getDir_testdataabbreviation().equals(AppEnv_request.getDir_testdataabbreviation())) IsChange = true;
            if (!AppEnv.getDir_retentionperiodabbreviation().equals(AppEnv_request.getDir_retentionperiodabbreviation())) IsChange = true;
            if (!AppEnv.getDir_tmpabbreviation().equals(AppEnv_request.getDir_tmpabbreviation())) IsChange = true;
            if (AppEnv.getDel_days_tmp().intValue() != AppEnv_request.getDel_days_tmp().intValue()) IsChange = true;
            if (AppEnv.getDel_days_retentionperiod().intValue() != AppEnv_request.getDel_days_retentionperiod().intValue()) IsChange = true;
            if (AppEnv.getDel_days_generationmanagement().intValue() != AppEnv_request.getDel_days_generationmanagement().intValue()) IsChange = true;
            if (AppEnv.getDel_days_processedscenario().intValue() != AppEnv_request.getDel_days_processedscenario().intValue()) IsChange = true;
            if (AppEnv.getDel_days_log().intValue() != AppEnv_request.getDel_days_log().intValue()) IsChange = true;
            if (AppEnv.getDel_days_processhistory().intValue() != AppEnv_request.getDel_days_processhistory().intValue()) IsChange = true;
            if (AppEnv.getDel_days_historyrecord().intValue() != AppEnv_request.getDel_days_historyrecord().intValue()) IsChange = true;
            if (AppEnv.getDel_days_web_screenshot().intValue() != AppEnv_request.getDel_days_web_screenshot().intValue()) IsChange = true;
            if (AppEnv.getNum_gm().intValue() != AppEnv_request.getNum_gm().intValue()) IsChange = true;
            if (AppEnv.getTimeout_m().intValue() != AppEnv_request.getTimeout_m().intValue()) IsChange = true;
            if (AppEnv.getLog_cut().intValue() != AppEnv_request.getLog_cut().intValue()) IsChange = true;
            if (AppEnv.getTc_svn_update() != AppEnv_request.getTc_svn_update()) IsChange = true;
            // 変更が無い場合は何もしない
            if (!IsChange) return AppEnv;

            // 更新回数+1
            AppEnv.setModify_count(AppEnv_request.getModify_count() + 1);
        }
        AppEnv.setDir_parametersheet(AppEnv_request.getDir_parametersheet());
        AppEnv.setDir_processingscenario(AppEnv_request.getDir_processingscenario());
        AppEnv.setDir_processedscenario(AppEnv_request.getDir_processedscenario());
        AppEnv.setDir_testdata(AppEnv_request.getDir_testdata());
        AppEnv.setDir_retentionperiod(AppEnv_request.getDir_retentionperiod());
        AppEnv.setDir_generationmanagement(AppEnv_request.getDir_generationmanagement());
        AppEnv.setDir_tmp(AppEnv_request.getDir_tmp());
        AppEnv.setDir_web_screenshot(AppEnv_request.getDir_web_screenshot());
        AppEnv.setPath_webdriver_edge(AppEnv_request.getPath_webdriver_edge());
        AppEnv.setPath_webdriver_firefox(AppEnv_request.getPath_webdriver_firefox());
        AppEnv.setPath_webdriver_chrome(AppEnv_request.getPath_webdriver_chrome());
        AppEnv.setPath_binary_edge(AppEnv_request.getPath_binary_edge());
        AppEnv.setPath_binary_firefox(AppEnv_request.getPath_binary_firefox());
        AppEnv.setPath_binary_chrome(AppEnv_request.getPath_binary_chrome());
        AppEnv.setDir_testdataabbreviation(AppEnv_request.getDir_testdataabbreviation());
        AppEnv.setDir_retentionperiodabbreviation(AppEnv_request.getDir_retentionperiodabbreviation());
        AppEnv.setDir_tmpabbreviation(AppEnv_request.getDir_tmpabbreviation());
        AppEnv.setDel_days_tmp(AppEnv_request.getDel_days_tmp());
        AppEnv.setDel_days_retentionperiod(AppEnv_request.getDel_days_retentionperiod());
        AppEnv.setDel_days_generationmanagement(AppEnv_request.getDel_days_generationmanagement());
        AppEnv.setDel_days_processedscenario(AppEnv_request.getDel_days_processedscenario());
        AppEnv.setDel_days_log(AppEnv_request.getDel_days_log());
        AppEnv.setDel_days_processhistory(AppEnv_request.getDel_days_processhistory());
        AppEnv.setDel_days_historyrecord(AppEnv_request.getDel_days_historyrecord());
        AppEnv.setDel_days_web_screenshot(AppEnv_request.getDel_days_web_screenshot());
        AppEnv.setNum_gm(AppEnv_request.getNum_gm());
        AppEnv.setTimeout_m(AppEnv_request.getTimeout_m());
        AppEnv.setLog_cut(AppEnv_request.getLog_cut());
        AppEnv.setTc_svn_update(AppEnv_request.getTc_svn_update());
        AppEnv.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        AppEnv.setUpdate_u_id(user_id);

        // delete & insert
        jdbcTemplate.update("DELETE FROM app_env_a");

        AppEnv = AppEnv_rep.save(AppEnv);

        // 履歴の登録:SQL実行
        String sql = "insert into app_env_b select * from app_env_a ";
        jdbcTemplate.update(sql);

        return AppEnv;
    }

    /**
     * 主キー検索
     */
    public AppEnv findById() {
        String sql = "select * from app_env_a";
        RowMapper<AppEnv> rowMapper = new BeanPropertyRowMapper<AppEnv>(AppEnv.class);
        AppEnv AppEnv = jdbcTemplate.queryForObject(sql, rowMapper);
        return AppEnv;
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
