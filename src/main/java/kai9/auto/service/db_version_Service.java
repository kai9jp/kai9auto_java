package kai9.auto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

import kai9.auto.model.db_version;

/**
 * DBバージョン :サービス
 */
@Service
public class db_version_Service {

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<db_version> searchAll(JdbcTemplate db_jdbcTemplate) {
        String sql = "select * from db_version_a order by ";
        RowMapper<db_version> rowMapper = new BeanPropertyRowMapper<db_version>(db_version.class);
        return db_jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 新規登録
     */
    public db_version create(db_version dbv, JdbcTemplate db_jdbcTemplate) throws CloneNotSupportedException {
        if (dbv.getModify_count() == null || dbv.getModify_count() == 0) dbv.setModify_count(1);// 新規登録は1固定
        dbv.setUpdate_date(new Date());
        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        dbv.setUpdate_u_id(user_id);
//      db_version = db_version_rep.save(db_version);

//      HashMap<String, Object> dbVersionMap = new HashMap<String, Object>();
//      dbVersionMap.put("modify_count", db_version.getModify_count());
//      dbVersionMap.put("db_version", db_version.getDb_version());
//      dbVersionMap.put("db_version_app", db_version.getDb_version_app());
//      dbVersionMap.put("update_u_id", db_version.getUpdate_u_id());
//      dbVersionMap.put("update_date", db_version.getUpdate_date());
//      String sql1 = "INSERT INTO db_version_a (modify_count, db_version, db_version_app, update_u_id, update_date) VALUES (:modify_count, :db_version, :db_version_app, :update_u_id, :update_date)";
//      db_jdbcTemplate.update(sql1, dbVersionMap);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
        String sql1 = "INSERT INTO db_version_a (modify_count, db_version, db_version_app, update_u_id, update_date) VALUES (?, ?, ?, ?, ?)";
        db_jdbcTemplate.update(sql1, new Object[] {
                Integer.parseInt(String.valueOf(dbv.getModify_count())),
                Integer.parseInt(String.valueOf(dbv.getDb_version())),
                Integer.parseInt(String.valueOf(dbv.getDb_version_app())),
                Integer.parseInt(String.valueOf(dbv.getUpdate_u_id())),
                LocalDateTime.parse(String.valueOf(dbv.getUpdate_date()), formatter)
        });

        // 履歴の登録:SQL実行
        String sql = "insert into db_version_b select * from db_version_a";
        db_jdbcTemplate.update(sql);

        return dbv;
    }

    /**
     * 更新
     * 
     * @throws CloneNotSupportedException
     */
    public db_version update(db_version dbv, JdbcTemplate db_jdbcTemplate) throws CloneNotSupportedException {

        // 更新処理
        dbv.setModify_count(dbv.getModify_count() + 1);// 更新回数+1
        dbv.setUpdate_date(new Date());

        // 認証ユーザ取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        int user_id = getUserIDByLoginID(name);
        dbv.setUpdate_u_id(user_id);
//      db_version = db_version_rep.save(db_version); ※違うDBを用いるのでリポジトリを使わない

        // プライマリキー(更新回数)を変えるのでUPDATE文は使えない。毎回Delete&Insertする
        db_jdbcTemplate.execute("delete from db_version_a");
        return create(dbv, db_jdbcTemplate);

        // 履歴の登録:SQL実行 ※create内で実施
//      String sql = "insert into db_version_b select * from db_version_a";
//      db_jdbcTemplate.update(sql);      
    }

    /**
     * 主キー検索
     */
    public db_version findById(JdbcTemplate db_jdbcTemplate) {
        String sql = "select * from db_version_a";
        RowMapper<db_version> rowMapper = new BeanPropertyRowMapper<db_version>(db_version.class);
        db_version db_version = db_jdbcTemplate.queryForObject(sql, rowMapper);
        return db_version;
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
