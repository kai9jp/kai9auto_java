package kai9.auto.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.model.syori_rireki3;
import kai9.auto.repository.syori_rireki3_Repository;

/**
 * 処理履歴_孫 :サービス
 */
@Service
@Scope("prototype")
public class syori_rireki3_Service {

    @Autowired
    private Environment environment;// メモリ解放不要

    @Autowired
    private ApplicationContext context;// メモリ解放不要

    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate jdbcTemplate_com;

    private syori_rireki3_Repository syori_rireki3_rep;

    // プロトタイプスコープなので、インスタンスを自前生成する
    public void CreateBeans() {

        // syori_rireki3_Repository生成
        this.syori_rireki3_rep = context.getBean(syori_rireki3_Repository.class);

        // jdbcTemplate生成
        String db_Url = environment.getProperty("spring.datasource.primary.url");
        String db_Username = environment.getProperty("spring.datasource.primary.username");
        String db_Password = environment.getProperty("spring.datasource.primary.password");
        // データソースとJdbcTemplateを作成する
        DriverManagerDataSource dataSource = new DriverManagerDataSource(db_Url, db_Username, db_Password);
        this.setJdbcTemplate(new JdbcTemplate(dataSource));
        try (Connection connection = dataSource.getConnection()) {
            // データベースに接続成功
        } catch (SQLException e) {
            throw new RuntimeException("データベースに接続失敗", e);
        }

        // jdbcTemplate_com生成
        String db_Url_com = environment.getProperty("spring.datasource.common.url");
        String db_Username_com = environment.getProperty("spring.datasource.common.username");
        String db_Password_com = environment.getProperty("spring.datasource.common.password");
        // データソースとJdbcTemplateを作成する
        DriverManagerDataSource dataSource_com = new DriverManagerDataSource(db_Url_com, db_Username_com, db_Password_com);
        this.jdbcTemplate_com = new JdbcTemplate(dataSource_com);
        try (Connection connection = dataSource_com.getConnection()) {
            // データベースに接続成功
        } catch (SQLException e) {
            throw new RuntimeException("データベースに接続失敗", e);
        }
    }

    // プロトタイプスコープなので、インスタンスを自前破棄する
    public void destroy() throws SQLException {
        this.syori_rireki3_rep = null;

        // jdbcTemplateおよびjdbcTemplate_comを解放
        if (this.getJdbcTemplate() != null) {
            this.getJdbcTemplate().getDataSource().getConnection().close();
            this.setJdbcTemplate(null);
        }
        if (this.jdbcTemplate_com != null) {
            this.jdbcTemplate_com.getDataSource().getConnection().close();
            this.jdbcTemplate_com = null;
        }
    }

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori_rireki3> searchAll() {
        String sql = "select * from syori_rireki3 order by s1_id,s_count,s2_id,s3_id";
        RowMapper<syori_rireki3> rowMapper = new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class);
        return getJdbcTemplate().query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public syori_rireki3 create(syori_rireki3 sr3) throws CloneNotSupportedException {
        syori_rireki3 syori_rireki3 = new syori_rireki3();
        syori_rireki3.setS1_id(sr3.getS1_id());
        syori_rireki3.setS_count(sr3.getS_count());
        syori_rireki3.setS2_id(sr3.getS2_id());
        syori_rireki3.setS3_id(sr3.getS3_id());
        syori_rireki3.setResult_type(sr3.getResult_type());
        syori_rireki3.setIs_ok(sr3.getIs_ok());
        syori_rireki3.setPercent3(sr3.getPercent3());
        syori_rireki3.setLog(sr3.getLog());
        syori_rireki3.setScreen_shot_filepath(sr3.getScreen_shot_filepath());
        syori_rireki3.setStart_time(sr3.getStart_time());
        syori_rireki3.setEnd_time(sr3.getEnd_time());
        syori_rireki3.setIs_timeout(sr3.getIs_timeout());
        syori_rireki3.setIs_suspension(sr3.getIs_suspension());
        syori_rireki3.setCreate_date(sr3.getCreate_date());
        syori_rireki3.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki3.setUpdate_u_id(user_id);
        syori_rireki3 = syori_rireki3_rep.save(syori_rireki3);

        return syori_rireki3;
    }

    /**
     * 更新
     * 
     * @throws IOException
     */
    public syori_rireki3 update(syori_rireki3 sr3) throws IOException {
        syori_rireki3 syori_rireki3 = findById(sr3.getS1_id(), sr3.getS_count(), sr3.getS2_id(), sr3.getS3_id());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori_rireki3.getS1_id() != sr3.getS1_id()) IsChange = true;
        if (syori_rireki3.getS_count() != sr3.getS_count()) IsChange = true;
        if (syori_rireki3.getS2_id() != sr3.getS2_id()) IsChange = true;
        if (syori_rireki3.getS3_id() != sr3.getS3_id()) IsChange = true;
        if (syori_rireki3.getResult_type() != sr3.getResult_type()) IsChange = true;
        if (syori_rireki3.getIs_ok() != sr3.getIs_ok()) IsChange = true;
        if (syori_rireki3.getPercent3() != sr3.getPercent3()) IsChange = true;
        if (!syori_rireki3.getLog().equals(sr3.getLog())) IsChange = true;
        if (!syori_rireki3.getScreen_shot_filepath().equals(sr3.getScreen_shot_filepath())) IsChange = true;
        if (syori_rireki3.getStart_time() != sr3.getStart_time()) IsChange = true;
        if (syori_rireki3.getEnd_time() != sr3.getEnd_time()) IsChange = true;
        if (syori_rireki3.getIs_timeout() != sr3.getIs_timeout()) IsChange = true;
        if (syori_rireki3.getIs_suspension() != sr3.getIs_suspension()) IsChange = true;
        if (syori_rireki3.getCreate_date() != sr3.getCreate_date()) IsChange = true;
        if (!IsChange) return syori_rireki3;

        // 更新処理
        syori_rireki3.setResult_type(sr3.getResult_type());
        syori_rireki3.setIs_ok(sr3.getIs_ok());
        syori_rireki3.setPercent3(sr3.getPercent3());
        syori_rireki3.setLog(sr3.getLog());
        syori_rireki3.setScreen_shot_filepath(sr3.getScreen_shot_filepath());
        syori_rireki3.setStart_time(sr3.getStart_time());
        syori_rireki3.setEnd_time(sr3.getEnd_time());
        syori_rireki3.setIs_timeout(sr3.getIs_timeout());
        syori_rireki3.setIs_suspension(sr3.getIs_suspension());
        syori_rireki3.setCreate_date(sr3.getCreate_date());
        syori_rireki3.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        syori_rireki3.setUpdate_u_id(user_id);
        syori_rireki3 = syori_rireki3_rep.save(syori_rireki3);

        return syori_rireki3;
    }

    /**
     * エラー登録
     * 
     * @throws IOException
     */
    public void updateError(syori_rireki3 sr3, String ErrorMsg) throws IOException {
        try {
            syori_rireki3 syori_rireki3 = findById(sr3.getS1_id(), sr3.getS_count(), sr3.getS2_id(), sr3.getS3_id());

            syori_rireki3.setScreen_shot_filepath(sr3.getScreen_shot_filepath());
            syori_rireki3.addLog(ErrorMsg);
            syori_rireki3.setIs_ok(false);
            syori_rireki3.setPercent3(100);
            syori_rireki3.setEnd_time(new Date());
            syori_rireki3.setIs_timeout(false);
            syori_rireki3.setUpdate_date(new Date());

            // 認証ユーザ取得
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String name = auth.getName();
            int user_id = getUserIDByLoginID(name);
            syori_rireki3.setUpdate_u_id(user_id);
            syori_rireki3 = syori_rireki3_rep.save(syori_rireki3);
            BeanUtils.copyProperties(syori_rireki3, sr3);
        } catch (Exception e) {
            throw new RuntimeException(Kai9Utils.GetException(e));
        }
    }

    /**
     * 成功登録
     * 
     * @throws IOException
     */
    public void updateSuccess(String callerClassName, Integer Percent3, Syori3Param s3p, String SuccessMsg) throws IOException {
        String crlf = System.lineSeparator();
        try {
            syori_rireki3 syori_rireki3 = findById(s3p.sr3.getS1_id(), s3p.sr3.getS_count(), s3p.sr3.getS2_id(), s3p.sr3.getS3_id());

            if (callerClassName.startsWith("web") && s3p.driver != null) {
                // webの場合はスクリーンショットを取得する
                String filename = s3p.takeScreenshot(s3p.sr3);
                s3p.sr3.setScreen_shot_filepath(filename);
                syori_rireki3.addLog(SuccessMsg + crlf + "[スナップショット保存場所]" + crlf + filename + crlf);
                syori_rireki3.setScreen_shot_filepath(filename);
            } else {
                syori_rireki3.addLog(SuccessMsg + crlf);
            }

            syori_rireki3.setIs_ok(true);
            syori_rireki3.setPercent3(Percent3);
            if (Percent3 == 100) syori_rireki3.setEnd_time(new Date());
            syori_rireki3.setIs_timeout(false);
            syori_rireki3.setUpdate_date(new Date());

            // 認証ユーザ取得
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String name = auth.getName();
            int user_id = getUserIDByLoginID(name);
            syori_rireki3.setUpdate_u_id(user_id);
            syori_rireki3 = syori_rireki3_rep.save(syori_rireki3);
            BeanUtils.copyProperties(syori_rireki3, s3p.sr3);
        } catch (Exception e) {
            throw new RuntimeException(Kai9Utils.GetException(e));
        }
    }

    /**
     * 主キー検索
     * 
     * @throws IOException
     */
//    public syori_rireki3 findById(int s1_id, int s_count, int s2_id, int s3_id) throws IOException {
//        try {
//	        String sql = "select * from syori_rireki3 where s1_id = ? and s_count = ? and s2_id = ? and s3_id = ?";
//	        RowMapper<syori_rireki3> rowMapper = new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class);
//	        syori_rireki3 syori_rireki3 = jdbcTemplate.queryForObject(sql,rowMapper,s1_id, s_count, s2_id, s3_id);
//	        //ここで、ヌルポになる。DBに存在しない場合、アベンドしている？
//	        return syori_rireki3; 
//        }catch(Exception e) {
//     		 throw new RuntimeException(Kai9Utils.GetException(e));
//        }    
//    }    
    public syori_rireki3 findById(int s1_id, int s_count, int s2_id, int s3_id) throws IOException {
        try {
            String sql = "select * from syori_rireki3 where s1_id = ? and s_count = ? and s2_id = ? and s3_id = ?";
            RowMapper<syori_rireki3> rowMapper = new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class);

            List<syori_rireki3> syori_rireki3_list = getJdbcTemplate().query(sql, rowMapper, s1_id, s_count, s2_id, s3_id);

            if (!syori_rireki3_list.isEmpty()) {
                return syori_rireki3_list.get(0);
            } else {
                // 該当するレコードが見つからなかった場合
                throw new RuntimeException("システムエラー:syori_rireki3 findById:存在するはずのレコードが見つかりませんでした");
            }
        } catch (Exception e) {
            throw new RuntimeException(Kai9Utils.GetException(e));
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
     * 
     * @throws IOException
     */
    public void delete(int s1_id, int s_count, int s2_id, int s3_id) throws IOException {
        syori_rireki3 syori_rireki3 = findById(s1_id, s_count, s2_id, s3_id);
        syori_rireki3_rep.delete(syori_rireki3);
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
