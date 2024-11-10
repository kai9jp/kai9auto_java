package kai9.auto.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kai9.auto.dto.syori2_Request;
import kai9.auto.model.syori2;
import kai9.auto.repository.syori2_Repository;

/**
 * 処理設定_親 :サービス
 */
@Service
public class syori2_Service {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    private syori2_Repository syori2_rep;

    /**
     * 全検索
     */
    @Transactional(readOnly = true)
    public List<syori2> searchAll() {
        String sql = "select * from syori2_a order by s1_id,s2_id";
        RowMapper<syori2> rowMapper = new BeanPropertyRowMapper<syori2>(syori2.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

//    public syori2 create(syori2_Request syori2_request) throws CloneNotSupportedException {
//      syori2 syori2 = new syori2();
//      syori2.setS1_id(syori2_request.getS1_id());
//      syori2.setModify_count(syori2_request.getModify_count());
//      syori2.setS2_id(syori2_request.getS2_id());
//      syori2.setRun_order(syori2_request.getRun_order());
//      syori2.setsheetname(syori2_request.getsheetname());
//      syori2.setIs_do(syori2_request.is_do());
//      syori2.setIs_normal(syori2_request.is_normal());
//      syori2.setNg_stop(syori2_request.isNg_stop());
//      syori2.setForced_run(syori2_request.isForced_run());
//      syori2.setScenario(syori2_request.getScenario());
//      syori2.setS_outline(syori2_request.getS_outline());
//      syori2.setStep_count(syori2_request.getStep_count());
//      syori2.setCol_step(syori2_request.getCol_step());
//      syori2.setCol_proc_cont(syori2_request.getCol_proc_cont());
//      syori2.setCol_comment(syori2_request.getCol_comment());
//      syori2.setCol_sum(syori2_request.getCol_sum());
//      syori2.setCol_keyword(syori2_request.getCol_keyword());
//      syori2.setCol_value1(syori2_request.getCol_value1());
//      syori2.setCol_value2(syori2_request.getCol_value2());
//      syori2.setCol_value3(syori2_request.getCol_value3());
//      syori2.setCol_variable1(syori2_request.getCol_variable1());
//      syori2.setCol_ass_result(syori2_request.getCol_ass_result());
//      syori2.setCol_run_result(syori2_request.getCol_run_result());
//      syori2.setCol_ng_stop(syori2_request.getCol_ng_stop());
//      syori2.setCol_start_time(syori2_request.getCol_start_time());
//      syori2.setCol_end_time(syori2_request.getCol_end_time());
//      syori2.setCol_sum_time(syori2_request.getCol_sum_time());
//      syori2.setCol_log(syori2_request.getCol_log());
//      syori2.setRow_log(syori2_request.getRow_log());
//      
//      //＠Transactionは、リポジトリと、jdbcTemplateを、別トランザクションとして扱ってしまうので、履歴登録が空振りしてしまうため、明示的なトランザクションを発行する
    // と思ったら、@Transactionalを消したら動いた・・・・
//      TransactionStatus ts = Kai9Utils.beginTransaction(transactionTemplate);
//      try {
//          syori2 = syori2_rep.save(syori2);
//          Kai9Utils.commitTransaction(transactionTemplate,ts);
//      } catch (Exception e) {
//    	  Kai9Utils.rollbackTransaction(transactionTemplate,ts);
//          throw e;
//      }
//
//      ts = Kai9Utils.beginTransaction(transactionTemplate);
//      try {
//          //履歴の登録:SQL実行
//          String sql = "insert into syori2_b select * from syori2_a where s1_id = ? and s2_id = ?";
//          jdbcTemplate.update(sql, syori2.getS1_id(),syori2.getS2_id());      
//    	  Kai9Utils.commitTransaction(transactionTemplate,ts);
//      } catch (Exception e) {
//    	  Kai9Utils.rollbackTransaction(transactionTemplate,ts);
//          throw e;
//      }
//
//      return syori2; 
//    }    

    /**
     * 新規登録
     */
//    @Transactional ※これがあると妙なトランザクション制御になり履歴が登録できないので注意
    public syori2 create(syori2_Request syori2_request) throws CloneNotSupportedException {
        syori2 syori2 = new syori2();
        syori2.setS1_id(syori2_request.getS1_id());
        syori2.setModify_count(syori2_request.getModify_count());
        syori2.setS2_id(syori2_request.getS2_id());
        syori2.setRun_order(syori2_request.getRun_order());
        syori2.setSheetname(syori2_request.getSheetname());
        syori2.setIs_do(syori2_request.is_do());
        syori2.setIs_normal(syori2_request.is_normal());
        syori2.setNg_stop(syori2_request.isNg_stop());
        syori2.setForced_run(syori2_request.isForced_run());
        syori2.setScenario(syori2_request.getScenario());
        syori2.setS_outline(syori2_request.getS_outline());
        syori2.setStep_count(syori2_request.getStep_count());
        syori2.setCol_step(syori2_request.getCol_step());
        syori2.setCol_proc_cont(syori2_request.getCol_proc_cont());
        syori2.setCol_comment(syori2_request.getCol_comment());
        syori2.setCol_sum(syori2_request.getCol_sum());
        syori2.setCol_keyword(syori2_request.getCol_keyword());
        syori2.setCol_value1(syori2_request.getCol_value1());
        syori2.setCol_value2(syori2_request.getCol_value2());
        syori2.setCol_value3(syori2_request.getCol_value3());
        syori2.setCol_variable1(syori2_request.getCol_variable1());
        syori2.setCol_ass_result(syori2_request.getCol_ass_result());
        syori2.setCol_run_result(syori2_request.getCol_run_result());
        syori2.setCol_ass_diff(syori2_request.getCol_ass_diff());
        syori2.setCol_ng_stop(syori2_request.getCol_ng_stop());
        syori2.setCol_start_time(syori2_request.getCol_start_time());
        syori2.setCol_end_time(syori2_request.getCol_end_time());
        syori2.setCol_sum_time(syori2_request.getCol_sum_time());
        syori2.setCol_log(syori2_request.getCol_log());
        syori2.setRow_log(syori2_request.getRow_log());
        syori2 = syori2_rep.save(syori2);

        // 履歴の登録:SQL実行
        String sql = "insert into syori2_b select * from syori2_a where s1_id = ? and s2_id = ?";
        jdbcTemplate.update(sql, syori2.getS1_id(), syori2.getS2_id());

        return syori2;
    }

    /**
     * 更新
     */
    public syori2 update(syori2_Request syori2_Request) {
        syori2 syori2 = findById(syori2_Request.getS1_id(), syori2_Request.getS2_id());
        // 変更対象が無い場合、更新しない
        boolean IsChange = false;
        if (syori2.getS1_id() != syori2_Request.getS1_id()) IsChange = true;
        if (syori2.getS2_id() != syori2_Request.getS2_id()) IsChange = true;
        if (syori2.getRun_order() != syori2_Request.getRun_order()) IsChange = true;
        if (!syori2.getSheetname().equals(syori2_Request.getSheetname())) IsChange = true;
        if (syori2.getIs_do() != syori2_Request.is_do()) IsChange = true;
        if (syori2.getIs_normal() != syori2_Request.is_normal()) IsChange = true;
        if (syori2.getNg_stop() != syori2_Request.isNg_stop()) IsChange = true;
        if (syori2.getForced_run() != syori2_Request.isForced_run()) IsChange = true;
        if (!syori2.getScenario().equals(syori2_Request.getScenario())) IsChange = true;
        if (!syori2.getS_outline().equals(syori2_Request.getS_outline())) IsChange = true;
        if (syori2.getStep_count() != syori2_Request.getStep_count()) IsChange = true;
        if (syori2.getCol_step() != syori2_Request.getCol_step()) IsChange = true;
        if (syori2.getCol_proc_cont() != syori2_Request.getCol_proc_cont()) IsChange = true;
        if (syori2.getCol_comment() != syori2_Request.getCol_comment()) IsChange = true;
        if (syori2.getCol_sum() != syori2_Request.getCol_sum()) IsChange = true;
        if (syori2.getCol_keyword() != syori2_Request.getCol_keyword()) IsChange = true;
        if (syori2.getCol_value1() != syori2_Request.getCol_value1()) IsChange = true;
        if (syori2.getCol_value2() != syori2_Request.getCol_value2()) IsChange = true;
        if (syori2.getCol_value3() != syori2_Request.getCol_value3()) IsChange = true;
        if (syori2.getCol_variable1() != syori2_Request.getCol_variable1()) IsChange = true;
        if (syori2.getCol_ass_result() != syori2_Request.getCol_ass_result()) IsChange = true;
        if (syori2.getCol_run_result() != syori2_Request.getCol_run_result()) IsChange = true;
        if (syori2.getCol_ass_diff() != syori2_Request.getCol_ass_diff()) IsChange = true;
        if (syori2.getCol_ng_stop() != syori2_Request.getCol_ng_stop()) IsChange = true;
        if (syori2.getCol_start_time() != syori2_Request.getCol_start_time()) IsChange = true;
        if (syori2.getCol_end_time() != syori2_Request.getCol_end_time()) IsChange = true;
        if (syori2.getCol_sum_time() != syori2_Request.getCol_sum_time()) IsChange = true;
        if (syori2.getCol_log() != syori2_Request.getCol_log()) IsChange = true;
        if (syori2.getRow_log() != syori2_Request.getRow_log()) IsChange = true;
        if (!IsChange) return syori2;

        // 更新処理
        syori2.setModify_count(syori2_Request.getModify_count() + 1);// 更新回数+1
        syori2.setRun_order(syori2_Request.getRun_order());
        syori2.setSheetname(syori2_Request.getSheetname());
        syori2.setIs_do(syori2_Request.is_do());
        syori2.setIs_normal(syori2_Request.is_normal());
        syori2.setNg_stop(syori2_Request.isNg_stop());
        syori2.setForced_run(syori2_Request.isForced_run());
        syori2.setScenario(syori2_Request.getScenario());
        syori2.setS_outline(syori2_Request.getS_outline());
        syori2.setStep_count(syori2_Request.getStep_count());
        syori2.setCol_step(syori2_Request.getCol_step());
        syori2.setCol_proc_cont(syori2_Request.getCol_proc_cont());
        syori2.setCol_comment(syori2_Request.getCol_comment());
        syori2.setCol_sum(syori2_Request.getCol_sum());
        syori2.setCol_keyword(syori2_Request.getCol_keyword());
        syori2.setCol_value1(syori2_Request.getCol_value1());
        syori2.setCol_value2(syori2_Request.getCol_value2());
        syori2.setCol_value3(syori2_Request.getCol_value3());
        syori2.setCol_variable1(syori2_Request.getCol_variable1());
        syori2.setCol_ass_result(syori2_Request.getCol_ass_result());
        syori2.setCol_run_result(syori2_Request.getCol_run_result());
        syori2.setCol_ass_diff(syori2_Request.getCol_ass_diff());
        syori2.setCol_ng_stop(syori2_Request.getCol_ng_stop());
        syori2.setCol_start_time(syori2_Request.getCol_start_time());
        syori2.setCol_end_time(syori2_Request.getCol_end_time());
        syori2.setCol_sum_time(syori2_Request.getCol_sum_time());
        syori2.setCol_log(syori2_Request.getCol_log());
        syori2.setRow_log(syori2_Request.getRow_log());
        syori2 = syori2_rep.save(syori2);

        // 履歴の登録:SQL実行
        String sql = "insert into syori2_b select * from syori2_a where s1_id = ? and s2_id = ?";
        jdbcTemplate.update(sql, syori2.getS1_id(), syori2.getS2_id());

        return syori2;
    }

    /**
     * 削除
     */
    public syori2 delete(syori2_Request syori2_Request) {
        syori2 syori2 = findById(syori2_Request.getS1_id(), syori2_Request.getS2_id());
        syori2.setModify_count(syori2_Request.getModify_count() + 1);// 更新回数+1
        syori2 = syori2_rep.save(syori2);

        // 履歴の登録:SQL実行
        String sql = "insert into syori2_b select * from syori2_a where s1_id = ? and s2_id = ?";
        jdbcTemplate.update(sql, syori2.getS1_id(), syori2.getS2_id());

        return syori2;
    }

    /**
     * 主キー検索
     */
    public syori2 findById(int s1_id, int s2_id) {
        String sql = "select * from syori2_a where s1_id = ? and s2_id = ?";
        RowMapper<syori2> rowMapper = new BeanPropertyRowMapper<syori2>(syori2.class);
        syori2 syori2 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, s2_id);
        return syori2;
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
    public void delete(int s1_id, int s2_id) {
        syori2 syori2 = findById(s1_id, s2_id);
        syori2_rep.delete(syori2);
    }
}
