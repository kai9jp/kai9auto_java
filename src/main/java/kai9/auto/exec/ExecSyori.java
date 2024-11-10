package kai9.auto.exec;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.jasypt.encryption.StringEncryptor;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kai9.auto.common.Export_s_excel;
import kai9.libs.JsonResponse;
import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.dto.syori_queue_Request;
import kai9.auto.keyword.CreationSymbolicLink;
import kai9.auto.keyword.DBVersionSync;
import kai9.auto.keyword.DatabaseCreate;
import kai9.auto.keyword.DatabaseDrop;
import kai9.auto.keyword.DatabaseSwitching;
import kai9.auto.keyword.DeleteFolder;
import kai9.auto.keyword.FileCopy;
import kai9.auto.keyword.FileDelete;
import kai9.auto.keyword.FileMonitoring;
import kai9.auto.keyword.FolderCopy;
import kai9.auto.keyword.FolderExist;
import kai9.auto.keyword.LogCheck;
import kai9.auto.keyword.MakeFolder;
import kai9.auto.keyword.NpmInstall;
import kai9.auto.keyword.ReactCompile;
import kai9.auto.keyword.ReactStart;
import kai9.auto.keyword.ReactStop;
import kai9.auto.keyword.SQLExec;
import kai9.auto.keyword.SQLResult;
import kai9.auto.keyword.S_Linking;
import kai9.auto.keyword.ServiceStart;
import kai9.auto.keyword.ServiceStop;
import kai9.auto.keyword.SpringCompile;
import kai9.auto.keyword.SpringStartJar;
import kai9.auto.keyword.SpringStopJar;
import kai9.auto.keyword.SvnCheckOut;
import kai9.auto.keyword.SvnUpdate;
import kai9.auto.keyword.TableExportExcelKw;
import kai9.auto.keyword.TestDataImport;
import kai9.auto.keyword.applicationYmlEdit;
import kai9.auto.keyword.selenium;
import kai9.auto.keyword.web_CheckValue;
import kai9.auto.keyword.web_CheckValue_ClassName;
import kai9.auto.keyword.web_CheckValue_ID;
import kai9.auto.keyword.web_Click_Id;
import kai9.auto.keyword.web_Click_Xpath;
import kai9.auto.keyword.web_DisappearUntilWait;
import kai9.auto.keyword.web_ElementDisplaySwitching;
import kai9.auto.keyword.web_FileUpload;
import kai9.auto.keyword.web_GetURL;
import kai9.auto.keyword.web_QuitWebDriver;
import kai9.auto.keyword.web_SetCheckBox_Id;
import kai9.auto.keyword.web_SetPageSize;
import kai9.auto.keyword.web_SetPageTimeout;
import kai9.auto.keyword.web_SetSelect_Id;
import kai9.auto.keyword.web_SetText_Id;
import kai9.auto.keyword.web_SetWebDriver;
import kai9.auto.keyword.web_Table_Find_Click;
import kai9.auto.keyword.web_element_is_exist_ID;
import kai9.auto.model.AppEnv;
import kai9.auto.model.m_keyword2;
import kai9.auto.model.syori1;
import kai9.auto.model.syori2;
import kai9.auto.model.syori3;
import kai9.auto.model.syori_rireki1;
import kai9.auto.model.syori_rireki2;
import kai9.auto.model.syori_rireki3;
import kai9.auto.service.m_keyword2_Service;
import kai9.auto.service.syori_queue_Service;
import kai9.auto.service.syori_rireki1_Service;
import kai9.auto.service.syori_rireki2_Service;
import kai9.auto.service.syori_rireki3_Service;
import kai9.som.keyword.DiskCapacityCheck;
import kai9.som.keyword.SOM_PatchApplyingJobExecutionResultCheck;
import kai9.som.keyword.SOM_PatchApplyingResultCheck;
import kai9.som.keyword.SOM_PatchApplyingResultNotification;
import kai9.som.keyword.SOM_TeamsWrite;

/**
 * キーワード実行:コントローラ
 */
@RestController
public class ExecSyori {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private m_keyword2_Service m_keyword2_service;

    @Autowired
    private syori_rireki1_Service syori_rireki1_Service;

    @Autowired
    private syori_rireki2_Service syori_rireki2_Service;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private StringEncryptor encryptor;

    @Autowired
    private syori_queue_Service syori_queue_service;

    // ホスト名+IPのリスト
    List<String> hostAndIps = null;

    /**
     * 処理回数の採番
     */
    @Transactional
    /**
     * 指定された s1_id に対応する s_counter テーブルの s_count 値を取得し、+1 した値を更新する。
     * s1_id に対応するレコードが存在しない場合は新しいレコードを挿入する。
     *
     * @param s1_id 更新するレコードの s1_id
     * @return 更新後の s_count 値
     */
    public int getNextCountForS1Id(int s1_id) {
        // SELECT クエリを定義
        String sqlSelect = "SELECT s_count FROM s_counter WHERE s1_id = :s1_id FOR UPDATE";
        // INSERT クエリを定義
        String sqlInsert = "INSERT INTO s_counter (s1_id, s_count) VALUES (:s1_id, 0)";
        // UPDATE クエリを定義
        String sqlUpdate = "UPDATE s_counter SET s_count = :newSCount WHERE s1_id = :s1_id";
        // パラメータをまとめる Map を作成
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("s1_id", s1_id);
        paramMap.put("newSCount", 0);
        // NamedParameterJdbcTemplate を生成
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        try {
            // SELECT クエリを実行
            int sCount = namedParameterJdbcTemplate.queryForObject(sqlSelect, paramMap, Integer.class);
            // 取得した s_count 値に +1 した値を計算
            int newSCount = sCount + 1;
            // パラメータマップに新しい s_count 値を設定
            paramMap.put("newSCount", newSCount);
            // UPDATE クエリを実行
            namedParameterJdbcTemplate.update(sqlUpdate, paramMap);
            // 更新後の s_count 値を返す
            return newSCount;
        } catch (EmptyResultDataAccessException e) {
            // s1_id に対応するレコードが存在しない場合は新しいレコードを挿入
            namedParameterJdbcTemplate.update(sqlInsert, paramMap);
            // s_count の初期値 1 を返す
            return 1;
        }
    }

    @PostMapping(value = "/api/FileCopy2", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void hoge(HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        System.out.print("bbb");
    }

    /**
     * 処理実行
     */
    @PostMapping(value = "/api/exec_syori", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void exec_syori(Integer s1_id, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        boolean onDebugLog = Boolean.valueOf(Kai9Utils.getPropertyFromYaml("kai9.on_debug_log"));
        //メモリ利用量の情報出力
        if (onDebugLog) {
            Kai9Utils.makeLog("info", "exec_syori：try", this.getClass());
            Kai9Utils.logMemoryInfo(this.getClass());
            Kai9Utils.makeLog("info", "----------------------------------", this.getClass());
        }
        try {

            // execute_uuidを受け取る(処理連結、kai9som等の外部APIコール用)
            String execute_uuid = request.getParameter("execute_uuid");
            if (execute_uuid == null) execute_uuid = "";

            // s2_idsをカンマ区切りの文字列として受け取る
            String s2_idsParam = request.getParameter("s2_ids");
            String[] s2_idsArray = null;
            if (s2_idsParam != null && !s2_idsParam.isEmpty()) {
                s2_idsArray = s2_idsParam.split(",");
            }
            // s3_idsをカンマ区切りの文字列として受け取る
            String s3_idsParam = request.getParameter("s3_ids");
            final String[] s3_idsArray = (s3_idsParam != null && !s3_idsParam.isEmpty()) ? s3_idsParam.split(",") : new String[0];

            // 処理連結NGフラグ
            Boolean s_linking_ng = Boolean.valueOf(request.getParameter("s_linking_ng"));

            // 任意のパラメータ
            String optionalParam = request.getParameter("optionalParam");

            // 処理対象の存在確認
            syori1 syori1_pre = null;
            try {
                String sql0 = "SELECT * FROM syori1_a WHERE s1_id = :s1_id";
                Map<String, Object> paramMap0 = new HashMap<>();
                paramMap0.put("s1_id", s1_id);
                NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
                syori1_pre = namedTemplate.queryForObject(
                        sql0,
                        paramMap0,
                        new BeanPropertyRowMapper<syori1>(syori1.class));
            } catch (EmptyResultDataAccessException e) {
                // レコードがヒットしなかった場合
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象処理が存在しません");
                json.SetJsonResponse(res);
                return;
            }

            // ホスト名とIPを取得
            //String localHost = InetAddress.getLocalHost().getHostName().toUpperCase();  //AWSでハングアップする事象が確認できたので環境変数からの取得に変更
            String localHost = System.getenv("COMPUTERNAME").toUpperCase();;
            String runHost = syori1_pre.getRun_host().trim().toUpperCase();
            if (!runHost.isEmpty() && !localHost.equals(runHost)) {
                // 実行ホストが異なる場合、処理キューを登録し、終了する
                syori_queue_Request syori_queue_Request = new syori_queue_Request();
                syori_queue_Request.setS1_id(s1_id);
                syori_queue_Request.setS2_ids(s2_idsParam);
                syori_queue_Request.setS3_ids(s3_idsParam);
                syori_queue_Request.setRun_host(runHost);
                syori_queue_service.create(syori_queue_Request);
                return;
            }
            
            // ----------------------------------------------------------
            // 処理1～3をDBから取得
            // ----------------------------------------------------------
            String sql = "SELECT 結果 FROM syori1_a a"
                    + " LEFT JOIN syori2_a b ON a.s1_id = b.s1_id"
                    + " LEFT JOIN syori3_a c ON a.s1_id = c.s1_id and b.s2_id = c.s2_id"
                    + " WHERE a.s1_id = :s1_id";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            List<syori1> syori1_list = new ArrayList<syori1>();
            // BeanPropertyRowMapperでsyori1,2,3クラスのオブジェクトを作成するためのRowMapperを作成する
            RowMapper<syori1> rowMapper1 = new BeanPropertyRowMapper<>(syori1.class);
            RowMapper<syori2> rowMapper2 = new BeanPropertyRowMapper<>(syori2.class);
            RowMapper<syori3> rowMapper3 = new BeanPropertyRowMapper<>(syori3.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<syori1> comparator1 = Comparator.comparing(syori1::getS1_id);
            Comparator<syori2> comparator2 = Comparator.comparing(syori2::getS1_id).thenComparing(syori2::getS2_id);
            Comparator<syori3> comparator3 = Comparator.comparing(syori3::getS1_id).thenComparing(syori3::getS2_id).thenComparing(syori3::getS3_id);


            // Setを生成する
            Set<syori1> set = new TreeSet<>(comparator1);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            //メモリ利用量が上がる直前個所なので情報出力
            if (onDebugLog) {
                Kai9Utils.makeLog("info", "exec_syori：1", this.getClass());
                Kai9Utils.logMemoryInfo(this.getClass());
                Kai9Utils.makeLog("info", "----------------------------------", this.getClass());
            }

            // syori1検索
            String sql1 = sql.replace("結果", "a.*");
            namedTemplate.query(sql1, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori1オブジェクトを作成する
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    data1 = rowMapper1.mapRow(rs, rowNum);
                    data1.syori2s = new TreeSet<>(comparator2); // Comparatorをセットする
                    set.add(data1);
                }

                return null;
            });
            //メモリ利用量が上がる直後個所なので情報出力
            if (onDebugLog) {
                Kai9Utils.makeLog("info", "exec_syori：2", this.getClass());
                Kai9Utils.logMemoryInfo(this.getClass());
                Kai9Utils.makeLog("info", "----------------------------------", this.getClass());
            }

            // syori2検索
            String sql2 = sql.replace("結果", "b.*");
            namedTemplate.query(sql2, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori1オブジェクトを作成する
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                syori2 data2 = new syori2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));

                if (data1.syori2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori2>) data1.syori2s).ceiling(data2);
                } else {
                    data2 = rowMapper2.mapRow(rs, rowNum);
                    data2.syori3s = new TreeSet<>(comparator3); // Comparatorをセットする
                    data1.syori2s.add(data2);
                }
                return null;
            });
            // syori3検索
            String sql3 = sql.replace("結果", "c.*");
            namedTemplate.query(sql3, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori1オブジェクトを作成する
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                syori2 data2 = new syori2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));

                if (data1.syori2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori2>) data1.syori2s).ceiling(data2);
                } else {
                    return null;
                }

                // 孫(syori3)を子(syori2)に紐付ける
                syori3 data3 = new syori3();
                data3.setS1_id(rs.getInt("s1_id"));
                data3.setS2_id(rs.getInt("s2_id"));
                data3.setS3_id(rs.getInt("s3_id"));

                if (data2.syori3s.contains(data3)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data3 = ((TreeSet<syori3>) data2.syori3s).ceiling(data3);
                } else {
                    data3 = rowMapper3.mapRow(rs, rowNum);
                    data2.syori3s.add(data3);
                }

                return null;
            });
            syori1_list = new ArrayList<>(set);
            if (syori1_list == null || syori1_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象処理が存在しません");
                json.SetJsonResponse(res);
                return;
            }
            set.clear();// リーク予防
            syori1 syori1 = syori1_list.get(0);
            syori_rireki1 syori_rireki1 = new syori_rireki1();
            syori_rireki1.setS1_id(syori1.getS1_id());
            syori_rireki1.setS_count(syori1.getS1_id());
            syori_rireki1.setSyori_modify_count(syori1.getModify_count());
            // キーワードマスタの更新回数を記憶
            String sq_kw = "SELECT modify_count FROM m_keyword1_a";
            Integer kw_modifyCount = jdbcTemplate.queryForObject(sq_kw, Integer.class);
            syori_rireki1.setM_keyword_modify_count(kw_modifyCount);
            syori1_list = null;// リーク防止


            // execute_uuidを(処理連結、kai9som等の外部APIコール用)
            syori_rireki1.setExecute_uuid(execute_uuid);
            // 処理回数採番
            syori_rireki1.setS_count(getNextCountForS1Id(syori1.getS1_id()));
            syori_rireki1.setUpdate_date(new Date());
            syori_rireki1.setStart_time(new Date());
            syori_rireki1.setPercent(0);
            syori_rireki1.setS_linking_ng(s_linking_ng);
            syori_rireki1.setExecute_ip(localHost);
            syori_rireki1_Service.create(syori_rireki1);

            // ----------------------------------------------------------
            // 実行準備
            // ----------------------------------------------------------

            // 実行順でソート
            // syori1のsyori2sをソートするためのComparatorを定義する
            Comparator<syori2> comparatorRun_order = Comparator.comparing(syori2::getRun_order).thenComparing(syori2::getS2_id);
            ;
            // ソート用のTreeSetを作成し、syori1のsyori2sの要素を追加する
            TreeSet<syori2> sortedSyori2s = new TreeSet<>(comparatorRun_order);
            sortedSyori2s.addAll(syori1.syori2s);
            // syori1のsyori2sをソート済みのTreeSetに置き換える
            syori1.syori2s = sortedSyori2s;

            // 実行
            // ----------------------------------------------------------
            ConcurrentHashMap<Integer, String> syori2_hashMap = new ConcurrentHashMap<>();
            int run_order = 0;
            // スレッドセーフに変更
            Object lock = new Object();
            AtomicInteger is_doing_count = new AtomicInteger(0);
            AtomicInteger ok_count1 = new AtomicInteger(0);
            AtomicInteger ng_count1 = new AtomicInteger(0);
            AtomicInteger ng_s_count1 = new AtomicInteger(0);
            AtomicBoolean is_ng_stop_ng = new AtomicBoolean(false);
            AtomicBoolean is_suspension2 = new AtomicBoolean(false);

            // 処理2指定、及び、処理連結を考慮し実行準備
            List<syori2> execSyori2s = new ArrayList<>();
            if (s2_idsArray != null) {
                // 処理No2が指定されている場合(処理2指定、又は、処理連結モード)

                // まず、s2_idsArrayに含まれるs2_idの順番で追加
                int ｒun_order = 0;
                for (String s2_id : s2_idsArray) {
                    for (syori2 item : syori1.syori2s) {
                        if (item.getS2_id().equals(Integer.parseInt(s2_id))) {
                            // 強制的に「実行させる」状態にする
                            item.setIs_do(true);
                            // 実行順を付け替え
                            ｒun_order++;
                            item.setRun_order(ｒun_order);
                            execSyori2s.add(item);
                            break;
                        }
                    }
                }
                // 残りの要素を元の順番で追加
                for (syori2 item : syori1.syori2s) {
                    if (!execSyori2s.contains(item)) {
                        // 強制的に「実行させない」状態にする
                        item.setIs_do(false);
                        // 実行順を付け替え
                        ｒun_order++;
                        item.setRun_order(ｒun_order);
                        execSyori2s.add(item);
                    }
                }
            } else {
                // 単純に追加
                for (syori2 item : syori1.syori2s) {
                    execSyori2s.add(item);
                }
            }

            // ループする要素の総数(進捗表示用)
            Integer total1 = execSyori2s.size();
            AtomicInteger current1 = new AtomicInteger(0);

            // 認証引継ぎ
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            while (true) {
                // syori2_hashMapの値が全て処理済ならbreak
                if (execSyori2s.size() == syori2_hashMap.size() && Collections.frequency(syori2_hashMap.values(), "処理済") == syori2_hashMap.size()) {
                    break;
                }
                // 中止された場合、処理を抜ける
                int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM syori_rireki1 WHERE s1_id = ? AND s_count = ? AND is_suspension = true",
                        Integer.class, syori_rireki1.getS1_id(), syori_rireki1.getS_count());
                if (count > 0) { // フラグがONになった場合
                    syori_rireki1.setIs_suspension(true);
                    syori_rireki1.addLog("中止されました");
                    break;
                }

                // 処理実行
                for (syori2 syori2 : execSyori2s) {
                    // 実行中、又は処理済の場合は無視
                    if ("実行中".equals(syori2_hashMap.get(syori2.getS2_id()))) continue;
                    if ("処理済".equals(syori2_hashMap.get(syori2.getS2_id()))) continue;

                    // 実行対象外は処理済に変更して無視
                    if (!syori2.getIs_do()) {
                        syori2_hashMap.put(syori2.getS2_id(), "処理済");

                        // 進捗率の計算と更新
                        synchronized (lock) {
                            current1.incrementAndGet();
                            syori_rireki1.setPercent((current1.get() * 100) / total1);
                            syori_rireki1_Service.updatePercent(syori_rireki1);
                            lock.notifyAll(); // 待機中のスレッドに通知
                        }

                        continue;
                    }

                    if (run_order >= syori2.getRun_order()) {
                        // 自分の実行順なので実行

                        syori_rireki2 syori_rireki2 = new syori_rireki2();
                        syori_rireki2.setS1_id(syori2.getS1_id());
                        syori_rireki2.setS2_id(syori2.getS2_id());
                        syori_rireki2.setS_count(syori_rireki1.getS_count());
                        syori_rireki2.setRun_order(syori2.getRun_order());
                        syori_rireki2.setPercent2(0);
                        syori_rireki2.setStart_time(new Date());
                        syori_rireki2.setCreate_date(new Date());
                        syori_rireki2.setUpdate_date(new Date());
                        syori_rireki2_Service.create(syori_rireki2);

                        // run_orderを更新
                        run_order = syori2.getRun_order();

                        // ハッシュマップのステータスを実行中に変更
                        syori2_hashMap.put(syori2.getS2_id(), "実行中");
                        synchronized (lock) {
                            is_doing_count.incrementAndGet();
                            lock.notifyAll(); // 待機中のスレッドに通知
                        }

                        // 強制実行以外は、先行タスクが失敗していた場合、実行しない
                        boolean is_skip = false;
                        synchronized (lock) {
                            if (is_ng_stop_ng.get() && !syori2.getForced_run()) {
                                syori2_hashMap.put(syori2.getS2_id(), "処理済");
                                is_doing_count.decrementAndGet();
                                is_skip = true;
                            }
                            lock.notifyAll(); // 待機中のスレッドに通知
                        }
                        if (is_skip) {
                            syori_rireki2.setEnd_time(new Date());
                            syori_rireki2.setPercent2(0);
                            syori_rireki2.setUpdate_date(new Date());
                            syori_rireki2.addLog("先行タスクで「NGで停止」が指定されているため処理をスキップしました");
                            syori_rireki2_Service.update(syori_rireki2);

                            // 進捗率の計算と更新
                            synchronized (lock) {
                                current1.incrementAndGet();
                                syori_rireki1.setPercent((current1.get() * 100) / total1);
                                syori_rireki1_Service.updatePercent(syori_rireki1);
                                lock.notifyAll(); // 待機中のスレッドに通知
                            }

                            continue;
                        }

                        // 非同期で実行(完了を待たない)
                        String crlf = System.lineSeparator();
                        // クラスローダーをメインスレッドと揃える(指定しないと異なるクラスローダとして動作するのでclass nof foundが出る)
                        Executor executor = Executors.newFixedThreadPool(10); // 10多重迄は並走させる
                        CompletableFuture.runAsync(() -> {
                            // 子スレッドへ認証引継ぎ
                            SecurityContextHolder.getContext().setAuthentication(auth);

                            AtomicInteger ok_count2 = new AtomicInteger(0);
                            AtomicInteger ng_count2 = new AtomicInteger(0);
                            AtomicInteger ng_s_count2 = new AtomicInteger(0);

                            Boolean is_suspension3 = false;

                            // 処理連結を考慮し実行準備
                            List<syori3> execSyori3s = new ArrayList<>();
                            if (s3_idsArray.length != 0) {
                                // 処理No3が指定されている場合(処理連結モード)

                                // s3_idsArrayに含まれるs3_idの順番で追加
                                for (String s3_id : s3_idsArray) {
                                    for (syori3 item : syori2.syori3s) {
                                        if (item.getS3_id().equals(Integer.parseInt(s3_id))) {
                                            execSyori3s.add(item);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                // 単純に追加
                                for (syori3 item : syori2.syori3s) {
                                    execSyori3s.add(item);
                                }
                            }

                            Integer total2 = execSyori3s.size(); // ループする要素の総数
                            Integer current2 = 0; // 現在の要素のインデックス
                            boolean isForcedRunOnly = false;

                            // syori2単位でSyori3Paramとsyori_rireki3_ServiceをDIする
                            Syori3Param Syori3Param = context.getBean(Syori3Param.class);
                            Syori3Param.CreateBeans();
                            // System.out.println("Syori3Param ID: " + System.identityHashCode(Syori3Param));//プロトタイプスコープのIDが異なる事の確認用デバッグコード
                            syori_rireki3_Service syori_rireki3_Service = context.getBean(syori_rireki3_Service.class);
                            syori_rireki3_Service.CreateBeans();

                            try {

                                // SKIPフラグ
                                boolean skipFlg = false;

                                // コメントアウトフラグ
                                boolean ｃommentOutFlg = false;

                                for (syori3 syori3 : execSyori3s) {
                                    // 中止された場合、処理を抜ける
                                    // 処理1の中止
                                    int count1 = jdbcTemplate.queryForObject(
                                            "SELECT COUNT(*) FROM syori_rireki1 WHERE s1_id = ? AND s_count = ? AND is_suspension = true",
                                            Integer.class, syori_rireki1.getS1_id(), syori_rireki1.getS_count());
                                    if (count1 > 0) { // フラグがONになった場合
                                        syori_rireki1.setIs_suspension(true);
                                        syori_rireki1.addLog("中止されました");

                                        syori_rireki2.setIs_suspension(true);
                                        syori_rireki2.addLog("中止されました");
                                        synchronized (lock) {
                                            is_suspension2.set(true);
                                            lock.notifyAll(); // 待機中のスレッドに通知
                                        }
                                        break;
                                    }
                                    // 処理2の中止
                                    int count2 = jdbcTemplate.queryForObject(
                                            "SELECT COUNT(*) FROM syori_rireki2 WHERE s1_id = ? AND s2_id = ? AND s_count = ? AND is_suspension = true",
                                            Integer.class, syori_rireki2.getS1_id(), syori_rireki2.getS2_id(), syori_rireki2.getS_count());
                                    if (count2 > 0 || is_suspension3) { // フラグがONになった場合(又は他の処理3が中止された場合)
                                        syori_rireki2.setIs_suspension(true);
                                        syori_rireki2.addLog("中止されました");
                                        synchronized (lock) {
                                            is_suspension2.set(true);
                                            lock.notifyAll(); // 待機中のスレッドに通知
                                        }
                                        break;
                                    }

                                    // 進捗率の計算と更新
                                    current2++;
                                    syori_rireki2.setPercent2((current2 * 100) / total2);
                                    syori_rireki2_Service.updatePercent(syori_rireki2);

                                    // 既に「NGで停止」タスクでNGになっている場合、「強制実行」以外はスキップする
                                    if (isForcedRunOnly && !syori3.getForced_run()) {
                                        syori_rireki2.addLog("【STEP" + syori3.getStep() + "】先行タスクで「NGで停止」が指定されているため処理をスキップしました");
                                        continue;
                                    }

                                    // キーワードマスタ検索入手
                                    m_keyword2 m_keyword2 = m_keyword2_service.findById(syori3.getKeyword());

                                    // コメントアウト終了
                                    if (m_keyword2 != null) {
                                        if (!m_keyword2.getFunc_name().equals("CommentOutTo") && ｃommentOutFlg) {
                                            // コメントアウト中はスキップする
                                            syori_rireki2.addLog("【STEP" + syori3.getStep() + "】コメントアウト中なので処理を無視しました");
                                            continue;
                                        }
                                    }

                                    // 複合化
                                    syori3.setValue1(replaceEncryptedStrings(syori3.getValue1()));
                                    syori3.setValue2(replaceEncryptedStrings(syori3.getValue2()));
                                    syori3.setValue3(replaceEncryptedStrings(syori3.getValue3()));
                                    Syori3Param.s3 = syori3;

                                    // 「DIR_テストデータ略称」の置換
                                    syori3.setValue1(abbreviation_substitution(syori3.getValue1(), syori2, syori3, Syori3Param.getAppEnv()));
                                    syori3.setValue2(abbreviation_substitution(syori3.getValue2(), syori2, syori3, Syori3Param.getAppEnv()));
                                    syori3.setValue3(abbreviation_substitution(syori3.getValue3(), syori2, syori3, Syori3Param.getAppEnv()));

                                    Syori3Param.sr3 = new syori_rireki3();
                                    try {
                                        // 履歴3を新規登録
                                        Syori3Param.sr3.setS1_id(syori_rireki1.getS1_id());
                                        Syori3Param.sr3.setS_count(syori_rireki1.getS_count());
                                        Syori3Param.sr3.setS2_id(syori_rireki2.getS2_id());
                                        Syori3Param.sr3.setS3_id(syori3.getS3_id());
                                        Syori3Param.sr3.setStart_time(new Date());
                                        Syori3Param.sr3.setCreate_date(new Date());
                                        Syori3Param.setOptionalParam(optionalParam);
                                        syori_rireki3_Service.create(Syori3Param.sr3);

                                        // キーワードマスタ確認
                                        if (m_keyword2 == null) {
                                            // キーワードが無い場合はエラー
                                            syori_rireki3_Service.updateError(Syori3Param.sr3, "キーワードが存在しません" + crlf + "キーワード=" + syori3.getKeyword());
                                        } else {

                                            if (m_keyword2.getFunc_name().equals("SkipTo")) {
                                                // SKIP(終了)
                                                skipFlg = false;
                                                syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "SKIP終了");
                                            } else if (m_keyword2.getFunc_name().equals("CommentOutTo")) {
                                                // コメントアウト(終了)
                                                ｃommentOutFlg = false;
                                                syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "コメントアウト終了");
                                            } else {

                                                // SKIP中のログは「SKIP」と出力する
                                                if (skipFlg == true) {
                                                    syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "SKIP");
                                                } else {

                                                    // キーワードをコール
                                                    if (m_keyword2.getFunc_name().equals("Stop")) {
                                                        // 強制終了なのでループを抜ける
                                                        syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "強制終了しました");
                                                        break;
                                                    } else if (m_keyword2.getFunc_name().equals("SkipFrom")) {
                                                        // SKIP(開始)
                                                        skipFlg = true;
                                                        syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "SKIP開始");
                                                    } else if (m_keyword2.getFunc_name().equals("CommentOutFrom")) {
                                                        // コメントアウト(開始)
                                                        ｃommentOutFlg = true;
                                                        syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "コメントアウト開始");
                                                    } else if (m_keyword2.getFunc_name().equals("FileDelete")) {
                                                        // ファイル削除
                                                        FileDelete FileDelete = context.getBean(FileDelete.class);
                                                        FileDelete.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("FileCopy")) {
                                                        // ファイルコピー
                                                        FileCopy FileCopy = context.getBean(FileCopy.class);
                                                        FileCopy.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("FileMonitoring")) {
                                                        // ファイル監視
                                                        FileMonitoring FileMonitoring = context.getBean(FileMonitoring.class);
                                                        FileMonitoring.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("FolderCopy")) {
                                                        FolderCopy FolderCopy = context.getBean(FolderCopy.class);
                                                        FolderCopy.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("FolderExist")) {
                                                        // フォルダ存在確認
                                                        FolderExist FolderExist = context.getBean(FolderExist.class);
                                                        FolderExist.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("MakeFolder")) {
                                                        MakeFolder MakeFolder = context.getBean(MakeFolder.class);
                                                        MakeFolder.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("CreationSymbolicLink")) {
                                                        //シンボリックリンク作成
                                                        CreationSymbolicLink CreationSymbolicLink = context.getBean(CreationSymbolicLink.class);
                                                        CreationSymbolicLink.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("DeleteFolder")) {
                                                        DeleteFolder DeleteFolder = context.getBean(DeleteFolder.class);
                                                        DeleteFolder.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("selenium")) {
                                                        selenium selenium = context.getBean(selenium.class);
                                                        selenium.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("DatabaseCreate")) {
                                                        // DB作成
                                                        DatabaseCreate DatabaseCreate = context.getBean(DatabaseCreate.class);
                                                        DatabaseCreate.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("DatabaseDrop")) {
                                                        // DB削除
                                                        DatabaseDrop DatabaseDrop = context.getBean(DatabaseDrop.class);
                                                        DatabaseDrop.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("DatabaseSwitching")) {
                                                        // DB切替
                                                        DatabaseSwitching DatabaseSwitching = context.getBean(DatabaseSwitching.class);
                                                        DatabaseSwitching.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("DatabaseSwitchingBack")) {
                                                        // DB切戻し
                                                        synchronized (lock) {
                                                            String result = Syori3Param.ChangeDBBack();
                                                            syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "DB切戻しに成功しました" + crlf + "[存在するテーブル]" + crlf + result);

                                                            lock.notifyAll(); // 待機中のスレッドに通知
                                                        }
                                                    } else if (m_keyword2.getFunc_name().equals("DBVersionSync")) {
                                                        // DBバージョン同期
                                                        DBVersionSync DBVersionSync = context.getBean(DBVersionSync.class);
                                                        DBVersionSync.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("LogCheck")) {
                                                        // ログ確認(全行)
                                                        LogCheck LogCheck = context.getBean(LogCheck.class);
                                                        LogCheck.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("S_Linking")) {
                                                        // 処理連結
                                                        S_Linking S_Linking = context.getBean(S_Linking.class);
                                                        S_Linking.exec(Syori3Param, m_keyword2.getFunc_name());
                                                    } else if (m_keyword2.getFunc_name().equals("S_Linking_nowait")) {
                                                        // 処理連結(待機せず)
                                                        S_Linking S_Linking = context.getBean(S_Linking.class);
                                                        S_Linking.exec(Syori3Param, m_keyword2.getFunc_name());
                                                    } else if (m_keyword2.getFunc_name().equals("S_LinkingNG")) {
                                                        // 処理連結NG版
                                                        S_Linking S_Linking = context.getBean(S_Linking.class);
                                                        S_Linking.exec(Syori3Param, m_keyword2.getFunc_name());
                                                    } else if (m_keyword2.getFunc_name().equals("S_LinkingNG_nowait")) {
                                                        // 処理連結NG版(待機せず)
                                                        S_Linking S_Linking = context.getBean(S_Linking.class);
                                                        S_Linking.exec(Syori3Param, m_keyword2.getFunc_name());
                                                    } else if (m_keyword2.getFunc_name().equals("Sleep")) {
                                                        // 待機(秒指定)
                                                        synchronized (lock) {
                                                            try {
                                                                Thread.sleep(Integer.valueOf(syori3.getValue1()) * 1000);
                                                                syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "待機しました" + crlf + "[秒数]" + crlf + syori3.getValue1());
                                                            } catch (Exception e) {
                                                                syori_rireki3_Service.updateError(Syori3Param.sr3, e.getMessage());
                                                            } finally {
                                                                lock.notifyAll(); // 待機中のスレッドに通知
                                                            }
                                                        }
                                                    } else if (m_keyword2.getFunc_name().equals("SQLResult")) {
                                                        // SQL結果取得
                                                        SQLResult SQLResult = context.getBean(SQLResult.class);
                                                        SQLResult.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SQLExec")) {
                                                        // SQL発行
                                                        SQLExec SQLExec = context.getBean(SQLExec.class);
                                                        SQLExec.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("TestDataImport")) {
                                                        // テストデータ投入
                                                        TestDataImport TestDataImport = context.getBean(TestDataImport.class);
                                                        TestDataImport.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("TableExportExcelKw")) {
                                                        // テーブル出力(エクセル)
                                                        TableExportExcelKw TableExportExcel = context.getBean(TableExportExcelKw.class);
                                                        TableExportExcel.exec(Syori3Param, true);
                                                    } else if (m_keyword2.getFunc_name().equals("SpringCompile")) {
                                                        // springコンパイル
                                                        SpringCompile SpringCompile = context.getBean(SpringCompile.class);
                                                        SpringCompile.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("applicationYmlEdit")) {
                                                        // pplication.yml更新
                                                        applicationYmlEdit applicationYmlEdit = context.getBean(applicationYmlEdit.class);
                                                        applicationYmlEdit.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SpringStartJar")) {
                                                        // jar起動
                                                        SpringStartJar SpringStartJar = context.getBean(SpringStartJar.class);
                                                        SpringStartJar.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SpringStopJar")) {
                                                        // jar停止
                                                        SpringStopJar SpringStopJar = context.getBean(SpringStopJar.class);
                                                        SpringStopJar.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("ReactStart")) {
                                                        // React起動
                                                        ReactStart ReactStart = context.getBean(ReactStart.class);
                                                        ReactStart.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("ReactStop")) {
                                                        // React停止
                                                        ReactStop ReactStop = context.getBean(ReactStop.class);
                                                        ReactStop.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("ReactCompile")) {
                                                        // Reactコンパイル
                                                        ReactCompile ReactCompile = context.getBean(ReactCompile.class);
                                                        ReactCompile.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("NpmInstall")) {
                                                        // NPMインストール
                                                        NpmInstall NpmInstall = context.getBean(NpmInstall.class);
                                                        NpmInstall.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SvnCheckOut")) {
                                                        // SVNチェックアウト
                                                        SvnCheckOut SvnCheckOut = context.getBean(SvnCheckOut.class);
                                                        SvnCheckOut.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SvnUpdate")) {
                                                        // SVN更新
                                                        SvnUpdate SvnUpdate = context.getBean(SvnUpdate.class);
                                                        SvnUpdate.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("ServiceStart")) {
                                                        // サービス起動
                                                        ServiceStart ServiceStart = context.getBean(ServiceStart.class);
                                                        ServiceStart.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("ServiceStop")) {
                                                        // サービス停止
                                                        ServiceStop ServiceStop = context.getBean(ServiceStop.class);
                                                        ServiceStop.exec(Syori3Param);
                                                    } else

                                                    // WEB系-----------------------------
                                                    if (m_keyword2.getFunc_name().equals("web_SetWebDriver")) {
                                                        // [web]ブラウザ起動
                                                        web_SetWebDriver web_SetWebDriver = context.getBean(web_SetWebDriver.class);
                                                        web_SetWebDriver.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_QuitWebDriver")) {
                                                        // [web]ブラウザ終了
                                                        web_QuitWebDriver web_QuitWebDriver = context.getBean(web_QuitWebDriver.class);
                                                        web_QuitWebDriver.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_GetURL")) {
                                                        // [web]URL遷移
                                                        web_GetURL web_GetURL = context.getBean(web_GetURL.class);
                                                        web_GetURL.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_SetPageTimeout")) {
                                                        // [web]ページロートタイムアウト設定(秒)
                                                        web_SetPageTimeout web_SetPageTimeout = context.getBean(web_SetPageTimeout.class);
                                                        web_SetPageTimeout.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_SetPageSize")) {
                                                        // [web]画面サイズ調整(横,縦)
                                                        web_SetPageSize web_SetPageSize = context.getBean(web_SetPageSize.class);
                                                        web_SetPageSize.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_Click_Id")) {
                                                        // [web]押下(id)
                                                        web_Click_Id web_Click_Id = context.getBean(web_Click_Id.class);
                                                        web_Click_Id.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_Click_Xpath")) {
                                                        // [web]押下(Xpath)
                                                        web_Click_Xpath web_Click_Xpath = context.getBean(web_Click_Xpath.class);
                                                        web_Click_Xpath.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_SetText_Id")) {
                                                        // [web]テキスト入力(id)
                                                        web_SetText_Id web_SetText_Id = context.getBean(web_SetText_Id.class);
                                                        web_SetText_Id.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_CheckValue_ClassName")) {
                                                        // [web]value値の確認(ClassName)
                                                        web_CheckValue_ClassName web_CheckValue_ClassName = context.getBean(web_CheckValue_ClassName.class);
                                                        web_CheckValue_ClassName.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_CheckValue_ID")) {
                                                        // [web]value値の確認(id)
                                                        web_CheckValue_ID web_CheckValue_ID = context.getBean(web_CheckValue_ID.class);
                                                        web_CheckValue_ID.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_CheckValue")) {
                                                        // [web]value値の確認(全エレメント)
                                                        web_CheckValue web_CheckValue = context.getBean(web_CheckValue.class);
                                                        web_CheckValue.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_SetSelect_Id")) {
                                                        // [web]セレクトボックス選択(id)
                                                        web_SetSelect_Id web_SetSelect_Id = context.getBean(web_SetSelect_Id.class);
                                                        web_SetSelect_Id.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_SetCheckBox_Id")) {
                                                        // [web]セレクトボックス選択(id)
                                                        web_SetCheckBox_Id web_SetCheckBox_Id = context.getBean(web_SetCheckBox_Id.class);
                                                        web_SetCheckBox_Id.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_Table_Find_Click")) {
                                                        // [web]テーブル検索クリック
                                                        web_Table_Find_Click web_Table_Find_Click = context.getBean(web_Table_Find_Click.class);
                                                        web_Table_Find_Click.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_element_is_exist_ID")) {
                                                        // [web]テーブル検索クリック
                                                        web_element_is_exist_ID web_element_is_exist_ID = context.getBean(web_element_is_exist_ID.class);
                                                        web_element_is_exist_ID.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_FileUpload")) {
                                                        // [web]ファイルアップロード
                                                        web_FileUpload web_FileUpload = context.getBean(web_FileUpload.class);
                                                        web_FileUpload.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_DisappearUntilWait")) {
                                                        // [web]消えるまで待機(xpath)
                                                        web_DisappearUntilWait web_DisappearUntilWait = context.getBean(web_DisappearUntilWait.class);
                                                        web_DisappearUntilWait.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_ElementDisplaySwitching")) {
                                                        // [web]エレメント表示切替
                                                        web_ElementDisplaySwitching web_ElementDisplaySwitching = context.getBean(web_ElementDisplaySwitching.class);
                                                        web_ElementDisplaySwitching.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("web_sleep")) {
                                                        // [web]待機(秒)
                                                        synchronized (lock) {
                                                            try {
                                                                Thread.sleep(Integer.valueOf(syori3.getValue1()) * 1000);
                                                                syori_rireki3_Service.updateSuccess(this.getClass().getSimpleName(), 100, Syori3Param, "待機しました" + crlf + "[秒数]" + crlf + syori3.getValue1());
                                                            } catch (Exception e) {
                                                                syori_rireki3_Service.updateError(Syori3Param.sr3, e.getMessage());
                                                            } finally {
                                                                lock.notifyAll(); // 待機中のスレッドに通知
                                                            }
                                                        }
                                                    } else

                                                    // SOM系-----------------------------
                                                    if (m_keyword2.getFunc_name().equals("DiskCapacityCheck")) {
                                                        // [som]ディスク空き容量確認
                                                        DiskCapacityCheck DiskCapacityCheck = context.getBean(DiskCapacityCheck.class);
                                                        DiskCapacityCheck.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SOM_TeamsWrite")) {
                                                        // [som]Teams書込
                                                        SOM_TeamsWrite SOM_TeamsWrite = context.getBean(SOM_TeamsWrite.class);
                                                        SOM_TeamsWrite.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SOM_PatchApplyingJobExecutionResultCheck")) {
                                                        // [som]パッチ適用ジョブ実行結果確認
                                                        SOM_PatchApplyingJobExecutionResultCheck SOM_PatchApplyingJobExecutionResultCheck = context.getBean(SOM_PatchApplyingJobExecutionResultCheck.class);
                                                        SOM_PatchApplyingJobExecutionResultCheck.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SOM_PatchApplyingResultCheck")) {
                                                        // [som]パッチ適用結果確認
                                                        SOM_PatchApplyingResultCheck SOM_PatchApplyingResultCheck = context.getBean(SOM_PatchApplyingResultCheck.class);
                                                        SOM_PatchApplyingResultCheck.exec(Syori3Param);
                                                    } else if (m_keyword2.getFunc_name().equals("SOM_PatchApplyingResultNotification")) {
                                                        // [som]パッチ適用結果通知
                                                        SOM_PatchApplyingResultNotification SOM_PatchApplyingResultNotification = context.getBean(SOM_PatchApplyingResultNotification.class);
                                                        SOM_PatchApplyingResultNotification.exec(Syori3Param);
                                                    } else

                                                    if (m_keyword2.getFunc_name().equals("Separator")) {
                                                        // セパレータ ※特に何もしない
                                                    } else {
                                                        syori_rireki3_Service.updateError(Syori3Param.sr3, "バックエンド側が未実装です" + crlf + "キーワード=" + syori3.getKeyword());
                                                    }

                                                }
                                            }
                                            // 結果に応じ、キーワードマスタで指定された結果文字列をセット
                                            if (Syori3Param.sr3.getIs_ok()) {
                                                syori3.setRun_result(m_keyword2.getOk_result());
                                            } else {
                                                syori3.setRun_result(m_keyword2.getNg_result());
                                            }

                                        }

                                        String assResult = syori3.getAss_result().replace("想定相違", "");
                                        if (skipFlg == true) {
                                            // SKIP
                                            synchronized (lock) {
                                                ok_count2.incrementAndGet();
                                                Syori3Param.sr3.setResult_type(4);
                                                syori_rireki3_Service.update(Syori3Param.sr3);
                                                lock.notifyAll(); // 待機中のスレッドに通知
                                            }
                                        } else if (syori3.getAss_result().isEmpty() || syori3.getRun_result().equals(assResult)) {
                                            // 想定通りの場合(想定結果が空の場合もNGでは無いと判定する)
                                            synchronized (lock) {
                                                ok_count2.incrementAndGet();
                                                Syori3Param.sr3.setResult_type(0);
                                                syori_rireki3_Service.update(Syori3Param.sr3);
                                                lock.notifyAll(); // 待機中のスレッドに通知
                                            }
                                        } else {
                                            // 想定と異なる場合、記憶
                                            synchronized (lock) {
                                                if (syori3.getAss_result().contains("想定相違")) {
                                                    // 想定通りの相違を記憶
                                                    ng_s_count2.incrementAndGet();
                                                    Syori3Param.sr3.setResult_type(2);
                                                    syori_rireki3_Service.update(Syori3Param.sr3);
                                                } else {
                                                    // 想定違い
                                                    ng_count2.incrementAndGet();
                                                    Syori3Param.sr3.setResult_type(1);
                                                    syori_rireki3_Service.update(Syori3Param.sr3);
                                                }
                                                lock.notifyAll(); // 待機中のスレッドに通知
                                            }

                                            // 「NGで停止」タスクが「想定結果と異なった場合」記録する
                                            if (syori3.getNg_stop()) isForcedRunOnly = true;
                                        }

                                    } catch (Exception e) {
                                        Syori3Param.sr3.addLog(Kai9Utils.GetException(e));
                                        Syori3Param.sr3.setEnd_time(new Date());
                                        Syori3Param.sr3.setUpdate_date(new Date());
                                        syori_rireki3_Service.update(Syori3Param.sr3);
                                    } finally {
                                        // 中止された場合、記憶する
                                        // WEB画面上で中止した場合、既に先の処理3に進んでしまう事が有るので、S3_idを除外し検索する
                                        int count3 = jdbcTemplate.queryForObject(
                                                "SELECT COUNT(*) FROM syori_rireki3 WHERE s1_id = ? AND s2_id = ? AND s_count = ? AND is_suspension = true",
                                                Integer.class, Syori3Param.sr3.getS1_id(), Syori3Param.sr3.getS2_id(), syori_rireki2.getS_count());
                                        if (count3 > 0 || Syori3Param.sr3.getIs_suspension()) { // フラグがONになった場合(又は他の処理3が中止された場合)
                                            is_suspension3 = true;
                                        }
                                    }
                                } // for (syori3 syori3: syori2.syori3s) のend

                                // ハッシュマップのステータスを処理済に変更
                                syori2_hashMap.put(syori2.getS2_id(), "処理済");
                                synchronized (lock) {
                                    is_doing_count.decrementAndGet();
                                    lock.notifyAll(); // 待機中のスレッドに通知
                                }

                                syori_rireki2.setOk_count(ok_count2.get());
                                syori_rireki2.setNg_count(ng_count2.get());
                                syori_rireki2.setS_ng_count(ng_s_count2.get());
                                if (ng_count2.get() + ng_s_count2.get() == 0) {
                                    // NGと想定NGが0の場合はOKとする
                                    syori_rireki2.setResult_type(0);
                                    ok_count1.incrementAndGet();
                                } else if (ng_count2.get() != 0) {
                                    // NGが1件以上存在する場合はNGとする
                                    syori_rireki2.setResult_type(1);
                                    ng_count1.incrementAndGet();

                                    // 「NGで停止」が指定されている場合、記憶させる
                                    if (syori2.getNg_stop()) {
                                        synchronized (lock) {
                                            is_ng_stop_ng.set(true);
                                            lock.notifyAll(); // 待機中のスレッドに通知
                                        }
                                    }
                                } else {
                                    // 想定相違だけの場合「想定相違」とする
                                    syori_rireki2.setResult_type(2);
                                    ng_s_count1.incrementAndGet();
                                }

                                // 中止された場合、3で上書き
                                if (is_suspension3) syori_rireki2.setResult_type(3);

                                syori_rireki2.setEnd_time(new Date());
                                syori_rireki2.setPercent2(100);
                                syori_rireki2.setUpdate_date(new Date());
                                syori_rireki2_Service.update(syori_rireki2);

                                // 進捗率の計算と更新
                                synchronized (lock) {
                                    current1.incrementAndGet();
                                    syori_rireki1.setPercent((current1.get() * 100) / total1);
                                    syori_rireki1_Service.updatePercent(syori_rireki1);
                                    lock.notifyAll(); // 待機中のスレッドに通知
                                }

                            } catch (Exception e) {
                                syori2_hashMap.put(syori2.getS2_id(), "処理済");
                                synchronized (lock) {
                                    is_doing_count.decrementAndGet();
                                    lock.notifyAll(); // 待機中のスレッドに通知
                                }

                                syori_rireki2.addLog(Kai9Utils.GetException(e));

                                // エラーとして記録
                                syori_rireki2.setResult_type(1);
                                syori_rireki2.setNg_count(1);
                                // エラーとして記録(親向け)
                                ng_count1.incrementAndGet();

                                syori_rireki2.setPercent2(100);
                                syori_rireki2.setEnd_time(new Date());
                                syori_rireki2.setUpdate_date(new Date());
                                syori_rireki2_Service.update(syori_rireki2);

                                // 進捗率の計算と更新
                                synchronized (lock) {
                                    current1.incrementAndGet();
                                    syori_rireki1.setPercent((current1.get() * 100) / total1);
                                    syori_rireki1_Service.updatePercent(syori_rireki1);
                                    lock.notifyAll(); // 待機中のスレッドに通知
                                }
                            } finally {
                                Syori3Param = null;// リーク予防
                                syori_rireki3_Service = null;// リーク予防
                                execSyori3s.clear();// リーク予防
                                execSyori3s = null;// リーク予防
                                ok_count2 = null;// リーク予防
                                ng_count2 = null;// リーク予防
                                ng_s_count2 = null;// リーク予防
                                is_suspension3 = null;// リーク予防 }
                            }

                        }, executor);// 子スレッド終了
                    }
                }

                boolean is_wait = false;
                synchronized (lock) {
                    if (is_doing_count.intValue() == 0) {
                        // 対象が無い場合、実行順をインクリメント
                        run_order++;
                    } else {
                        is_wait = true;
                    }
                }
                if (is_wait) Thread.sleep(1000);// 1秒待機

            }
            execSyori2s.clear();// リーク予防
            execSyori2s = null;// リーク予防

            syori_rireki1.setOk_count(ok_count1.get());
            syori_rireki1.setNg_count(ng_count1.get());
            syori_rireki1.setS_ng_count(ng_s_count1.get());
            if (is_suspension2.get()) {
                // 中止された場合
                syori_rireki1.setResult_type(3);
            } else if (ng_count1.get() + ng_s_count1.get() == 0) {
                // NGと想定NGが0の場合はOKとする
                syori_rireki1.setResult_type(0);
            } else if (ng_count1.get() != 0) {
                // NGが1件以上存在する場合はNGとする
                syori_rireki1.setResult_type(1);
            } else {
                // 想定相違だけの場合「想定相違」とする
                syori_rireki1.setResult_type(2);
            }
            syori_rireki1.setEnd_time(new Date());
            syori_rireki1.setPercent(100);
            syori_rireki1.setUpdate_date(new Date());
            syori_rireki1_Service.update(syori_rireki1);

            // ログ(エクセル)をローカルに保存
            Export_s_excel export_s_excel = context.getBean(Export_s_excel.class);
            export_s_excel.Export(syori_rireki1.getS1_id(), syori_rireki1.getS_count());

        } catch (Exception e) {
            // それ以外の例外の場合の処理
            Kai9Utils.handleException(e, res);
        }finally {
            //メモリ利用量の情報出力
            if (onDebugLog) {
                Kai9Utils.makeLog("info", "exec_syori：finally", this.getClass());
                Kai9Utils.logMemoryInfo(this.getClass());
                Kai9Utils.makeLog("info", "----------------------------------", this.getClass());
            }
        }
    }

    /**
     * 文字列中の <暗号化> タグで囲まれた文字列を複合化した文字列に置換します。
     *
     * @param str 置換対象の文字列
     * @return 置換後の文字列
     */
    public String replaceEncryptedStrings(String str) {
        // 正規表現パターンを定義します
        Pattern pattern = Pattern.compile("<暗号化>(.*?)</暗号化>");

        // パターンをマッチングさせます
        Matcher matcher = pattern.matcher(str);

        // 置換後の文字列を格納するためのバッファを作成します
        StringBuffer sb = new StringBuffer();

        // マッチングが成功する限り、置換処理を繰り返します
        while (matcher.find()) {
            // マッチングした部分文字列を複合化します
            String replacement = encryptor.decrypt(matcher.group(1));

            // 置換文字列に含まれる特殊文字をエスケープします
            String escapedReplacement = Matcher.quoteReplacement(replacement);

            // マッチングした部分文字列を置換します
            matcher.appendReplacement(sb, escapedReplacement);
        }

        // マッチングに成功しなかった残りの文字列をバッファに追加します
        matcher.appendTail(sb);

        // 置換後の文字列を返します
        return sb.toString();
    }

    // 略称置換
    public String abbreviation_substitution(String str, syori2 sr2, syori3 sr3, AppEnv AppEnv) {
        if (str.isEmpty()) return str;
        String result = str;
        result = replacement(result, AppEnv.getDir_testdataabbreviation(), AppEnv.getDir_testdata(), sr2, sr3);
        result = replacement(result, AppEnv.getDir_retentionperiodabbreviation(), AppEnv.getDir_retentionperiod(), sr2, sr3);
        result = replacement(result, AppEnv.getDir_tmpabbreviation(), AppEnv.getDir_tmp(), sr2, sr3);
        return result;
    }

    public String replacement(String str, String pS1, String pS2, syori2 sr2, syori3 sr3) {
        if (!pS2.endsWith("\\")) pS2 += "\\";// 末尾に\を付与
        String lID1 = String.format("%04d", sr3.getS1_id());
        String lID2 = sr2.getSheetname();// 処理No2は意図せぬ数字になり混乱を招く(実行順のシートに書いた順番になってしまう仕様)のでシート名にしておく
        String lID3 = Integer.toString(sr3.getS3_id());
        str = StringUtils.replace(str, "[" + pS1 + "]", pS2);
        str = StringUtils.replace(str, "[" + pS1 + "123]", pS2 + lID1 + "-" + lID2 + "-" + lID3 + "\\");
        str = StringUtils.replace(str, "[" + pS1 + "12]", pS2 + lID1 + "-" + lID2 + "\\");
        str = StringUtils.replace(str, "[" + pS1 + "1]", pS2 + lID1 + "\\");
        return str;
    }

//    function replacement(pResult,pS1,pS2:string):string;
//    var
//      lID1,lID2,lID3 : string;
//    begin
//      Result := pResult;
//      lID1 := Format('%.4d',[pSYORI3.S1_ID]);
//      //lID2 := IntToStr(FSYORI3.S2_ID);処理No2は意図せぬ数字になり混乱を招く(実行順のシートに書いた順番になってしまう仕様)のでシート名にしておく
//      lID2 := pSYORI2.sheetname;
//      lID3 := IntToStr(pSYORI3.S3_ID);
//      Result := StringReplace(Result,'[' + pS1+   ']',pS2                           ,[rfReplaceAll]);
//      Result := StringReplace(Result,'[' + pS1+'123]',pS2+lID1+'-'+lID2+'-'+lID3+'\',[rfReplaceAll]);
//      Result := StringReplace(Result,'[' + pS1+ '12]',pS2+lID1+'-'+lID2         +'\',[rfReplaceAll]);
//      Result := StringReplace(Result,'[' + pS1+  '1]',pS2+lID1                  +'\',[rfReplaceAll]);
//    end;

}
