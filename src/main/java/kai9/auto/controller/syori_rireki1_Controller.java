package kai9.auto.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kai9.auto.common.Export_s_excel;
import kai9.libs.JsonResponse;
import kai9.libs.Kai9Utils;
import kai9.auto.model.syori_rireki1;
import kai9.auto.model.syori_rireki2;
import kai9.auto.model.syori_rireki3;

/**
 * 処理履歴_親 :コントローラ
 */
@RestController
public class syori_rireki1_Controller {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    Export_s_excel export_s_excel;

    private String sql_h = "";

    /**
     * 件数検索
     * 
     */
    @PostMapping(value = "/api/syori_rireki1_find_count", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori_rireki1_find_count(
            Integer limit, Integer offset, String findstr, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime findDateTime,
            HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {

        try {
            // 前回検索タイミングからデータ変更が無い場合、結果を返さず省電力化する
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("update_date", findDateTime);
            String sql = "select count(*) from syori_rireki1 where update_date > :update_date";
            Integer count = namedJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
            if (count == 0) {
                // レスポンス生成
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("前回変更時から変更されたデータが存在しませんでした。");
                json.Add("NoChanged", String.valueOf(true));
                json.SetJsonResponse(res);
                return;
            }

            // 件数取得用のAPIを返す
            String sqlc = "SELECT COUNT(*) FROM (" +
                    "  SELECT a.s1_id, a.s_count" +
                    "  FROM syori_rireki1 a" +
                    "  LEFT JOIN syori1_a b ON a.s1_id = b.s1_id" +
                    "  WHERE b.s1_id IS NOT NULL " +
                    "  あいまい検索 " +
                    "  GROUP BY a.s1_id, a.s_count" +
                    "  ORDER BY a.update_date DESC" +
                    "  LIMIT :limit OFFSET :offset" +
                    ") as grouped_results ";
            String sql_count = sqlc.replace("LIMIT :limit OFFSET :offset", "");// limitとoffsetを除外

            if (findstr != null && !findstr.isEmpty()) {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs)
                    keys.add("%" + i + "%");
                paramMap.put("keys", keys);

                String like = "AND (b.s1_name LIKE ANY(ARRAY[ :keys ]) OR a.log LIKE ANY(ARRAY[ :keys ]) OR a.execute_ip LIKE ANY(ARRAY[ :keys ]) OR a.execute_port LIKE ANY(ARRAY[ :keys ]))";
                sql_count = sql_count.replace("あいまい検索", like);
            } else {
                sql_count = sql_count.replace("あいまい検索", "");
            }

            String all_count = namedJdbcTemplate.queryForObject(sql_count, paramMap, String.class);

            // レスポンス生成
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setMsg("正常に件数を取得しました");
            json.Add("all_count", String.valueOf(all_count));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 単一検索
     * 
     */
    @PostMapping(value = "/api/syori_rireki1_find_one", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori_rireki1_find_one(Integer limit, Integer offset, String findstr, Integer s1_id, Integer s2_id, Integer s_count,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime findDateTime, HttpServletResponse res, HttpServletRequest request)
            throws CloneNotSupportedException, IOException {

        try {
            String modeTmp = "単一検索";
            final String mode = modeTmp;

            // 処理時間計測
            long[] times = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            long start0 = System.nanoTime();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("limit", limit);
            paramMap.put("offset", offset);
            List<syori_rireki1> syori_rireki1_list = new ArrayList<>();

            // logのサイズが大きいので、logだけをnullに挿げ替えるSQLを動的作成する
            if (sql_h.isEmpty()) {
                sql_h = Kai9Utils.createSelectSqlWithNullColumn("syori_rireki3", "log", "h", "CASE WHEN h.log = '' THEN '' ELSE '有' END", jdbcTemplate);
            }

            String sql1 = "e.*, f.s1_name";
            String sql2 = "i.*, g.*"; // 同名カラムがある場合、後ろの方が採用されるので順番に注意
            String sql3 = "j.*, h.*";

            String sql0 =
                    // CTEで条件にマッチするs1_idとs_countのキーだけ取得
                    "  SELECT a.s1_id, a.s_count" +
                            "  FROM syori_rireki1 a" +
                            "  LEFT JOIN syori1_a b ON a.s1_id = b.s1_id" +
                            "  LEFT JOIN syori_rireki2 c ON a.s1_id = c.s1_id AND a.s_count = c.s_count" +
                            "  LEFT JOIN syori_rireki3 d ON a.s1_id = d.s1_id AND c.s2_id = d.s2_id AND a.s_count = d.s_count " +
                            "  LEFT JOIN syori2_b e ON a.s1_id = e.s1_id AND c.s2_id = e.s2_id AND a.syori_modify_count = e.modify_count " +
                            "  LEFT JOIN syori3_b f ON a.s1_id = f.s1_id AND c.s2_id = f.s2_id AND d.s3_id = f.s3_id AND a.syori_modify_count = e.modify_count " +
                            "  WHERE b.s1_id IS NOT NULL and c.s2_id IS NOT NULL AND e.s1_id IS NOT null  " +
                            "  あいまい検索 " +
                            "  GROUP BY a.s1_id, a.s_count" +
                            "  ORDER BY a.update_date DESC" +
                            "  LIMIT :limit OFFSET :offset";
            String sql = "WITH filtered_records AS (" + // CTE(Common Table Expression)で一時的なテーブルを作成し副問い合わせ代わりに利用(別に副問でもOK)
                    sql0 +
                    ") " +
                    // CTEに対してSQLを発行し、4テーブル分の外部結合レコードを取得
                    "SELECT 結果 " +
                    "FROM syori_rireki1 AS e " +
                    "LEFT JOIN syori1_a AS f ON e.s1_id = f.s1_id " +
                    "LEFT JOIN syori_rireki2 AS g ON e.s1_id = g.s1_id AND e.s_count = g.s_count " +
                    "LEFT JOIN syori_rireki3 AS h ON e.s1_id = h.s1_id AND g.s2_id = h.s2_id AND e.s_count = h.s_count " +
                    "LEFT JOIN syori2_b AS i ON e.s1_id = i.s1_id AND g.s2_id = i.s2_id  AND e.syori_modify_count = i.modify_count " +
                    "LEFT JOIN syori3_b AS j ON e.s1_id = j.s1_id AND g.s2_id = j.s2_id AND h.s3_id = j.s3_id AND e.syori_modify_count = j.modify_count " +
                    "WHERE (e.s1_id, e.s_count) IN (SELECT s1_id, s_count FROM filtered_records);";

            if (findstr.equals("")) {
                sql = sql.replace("あいまい検索", "");
            } else {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs)
                    keys.add("%" + i + "%");
                paramMap.put("keys", keys);

                String like = "AND (b.s1_name LIKE ANY(ARRAY[ :keys ]) OR a.log LIKE ANY(ARRAY[ :keys ]) OR a.execute_ip LIKE ANY(ARRAY[ :keys ]) OR a.execute_port LIKE ANY(ARRAY[ :keys ]) )";
                sql = sql.replace("あいまい検索", like);
            }

            // BeanPropertyRowMapperでsyori1, 2, 3クラスのオブジェクトを作成するためのRowMapperを作成する
            RowMapper<syori_rireki1> rowMapper1 = new BeanPropertyRowMapper<>(syori_rireki1.class);
            RowMapper<syori_rireki2> rowMapper2 = new BeanPropertyRowMapper<>(syori_rireki2.class);
            RowMapper<syori_rireki3> rowMapper3 = new BeanPropertyRowMapper<>(syori_rireki3.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<syori_rireki1> comparator1 = Comparator.comparing(syori_rireki1::getS1_id).thenComparing(syori_rireki1::getS_count);
            Comparator<syori_rireki2> comparator2 = Comparator.comparing(syori_rireki2::getS1_id).thenComparing(syori_rireki2::getS2_id).thenComparing(syori_rireki2::getS_count);
            Comparator<syori_rireki3> comparator3 = Comparator.comparing(syori_rireki3::getS1_id).thenComparing(syori_rireki3::getS2_id).thenComparing(syori_rireki3::getS3_id).thenComparing(syori_rireki3::getS_count);

            // Setを生成する
            Set<syori_rireki1> set = new TreeSet<>(comparator1);

            // jdbcTemplateを使用してSQLを実行し、ResultSetから取得したデータをSetに格納する
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            times[0] += System.nanoTime() - start0;

            // syori_rireki1の検索
            long start = System.nanoTime();
            sql1 = sql.replace("結果", sql1);
            namedTemplate.query(sql1, paramMap, (rs, rowNum) -> {
                if (mode.equals("単一検索")) {
                    // 単一レコード検索モードのレコードだけ返す
                    if (s1_id != 0 && s1_id != rs.getInt("s1_id")) return null;
                    if (s_count != 0 && s_count != rs.getInt("s_count")) return null;
                }

                // ResultSetからsyori1オブジェクトを作成する
                syori_rireki1 data1 = new syori_rireki1();
                data1.setS1_id(rs.getInt("s1_id"));
                data1.setS_count(rs.getInt("s_count"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori_rireki1>) set).ceiling(data1);
                } else {
                    data1 = rowMapper1.mapRow(rs, rowNum);
                    set.add(data1);
                    data1.syori_rireki2s = new TreeSet<>(comparator2); // Comparatorをセットする
                }
                return null;
            });
            times[1] += System.nanoTime() - start;

            // syori_rireki2の検索
            start = System.nanoTime();
            sql2 = sql.replace("結果", sql2);
            namedTemplate.query(sql2, paramMap, (rs, rowNum) -> {
                // 「単一検索」又は「行選択」されたレコード以外は中身を返さない
                if (s1_id != rs.getInt("s1_id")) return null;
                if (s_count != rs.getInt("s_count")) return null;

                // ResultSetからsyori1オブジェクトを作成する
                syori_rireki1 data1 = new syori_rireki1();
                data1.setS1_id(rs.getInt("s1_id"));
                data1.setS_count(rs.getInt("s_count"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori_rireki1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                syori_rireki2 data2 = new syori_rireki2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));
                data2.setS_count(rs.getInt("s_count"));

                if (data1.syori_rireki2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori_rireki2>) data1.syori_rireki2s).ceiling(data2);
                } else {
                    data2 = rowMapper2.mapRow(rs, rowNum);
                    data2.setIs_do(rs.getBoolean("is_do"));

                    data1.syori_rireki2s.add(data2);

                    data2.syori_rireki3s = new TreeSet<>(comparator3); // Comparatorをセットする
                }
                return null;
            });
            times[2] += System.nanoTime() - start;

            // syori_rireki3の検索
            sql3 = sql.replace("結果", sql3);
            namedTemplate.query(sql3, paramMap, (rs, rowNum) -> {
                // 「単一検索」又は「行選択」されたレコード以外は中身を返さない
                if (s1_id != rs.getInt("s1_id")) return null;
                if (s_count != rs.getInt("s_count")) return null;
                if (s2_id == 0) return null;
                if (s2_id != -1) {
                    if (s2_id != rs.getInt("s2_id")) return null;
                }

                // ResultSetからsyori1オブジェクトを作成する
                syori_rireki1 data1 = new syori_rireki1();
                data1.setS1_id(rs.getInt("s1_id"));
                data1.setS_count(rs.getInt("s_count"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori_rireki1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                syori_rireki2 data2 = new syori_rireki2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));
                data2.setS_count(rs.getInt("s_count"));

                if (data1.syori_rireki2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori_rireki2>) data1.syori_rireki2s).ceiling(data2);
                } else {
                    return null;
                }

                // 孫(syori3)を子(syori2)に紐付ける
                syori_rireki3 data3 = new syori_rireki3();
                data3.setS1_id(rs.getInt("s1_id"));
                data3.setS2_id(rs.getInt("s2_id"));
                data3.setS3_id(rs.getInt("s3_id"));
                data3.setS_count(rs.getInt("s_count"));

                if (data2.syori_rireki3s.contains(data3)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data3 = ((TreeSet<syori_rireki3>) data2.syori_rireki3s).ceiling(data3);
                } else {
                    long start3 = System.nanoTime();
                    data3 = rowMapper3.mapRow(rs, rowNum);
                    times[3] += System.nanoTime() - start3;
                    data2.syori_rireki3s.add(data3);
                }
                return null;
            });

            // SetをListに変換する
            start = System.nanoTime();
            syori_rireki1_list = new ArrayList<>(set);
            // 更新日時順に降順ソート
            Collections.sort(syori_rireki1_list, Comparator.comparing(syori_rireki1::getUpdate_date).reversed());

            // 実施者の反映----------------------------------------------------------------
            Set<Integer> updateUIds = new HashSet<>();
            for (syori_rireki1 data : syori_rireki1_list) {
                updateUIds.add(data.getUpdate_u_id());
            }
            if (updateUIds.size() != 0) {
                Map<String, Object> paramMap2 = new HashMap<>();
                paramMap2.put("keys", updateUIds);
                // SQLの発行
                String sql4 = "SELECT user_id, sei, mei FROM m_user_a WHERE user_id = ANY(ARRAY[ :keys ])";
                NamedParameterJdbcTemplate namedTemplate2 = new NamedParameterJdbcTemplate(jdbcTemplate_com);
                List<Map<String, Object>> userNames = namedTemplate2.queryForList(sql4, paramMap2);
                // syori_rireki1のupdate_userにuser_nameを書き戻す
                for (syori_rireki1 data : syori_rireki1_list) {
                    for (Map<String, Object> row : userNames) {
                        Integer userId = (Integer) row.get("user_id");
                        if (data.getUpdate_u_id().equals(userId)) {
                            String userName = (String) row.get("sei") + " " + (String) row.get("mei");
                            data.setUpdate_user(userName);
                            break;
                        }
                    }
                }
            }
            times[4] += System.nanoTime() - start;

            // 実施結果の反映----------------------------------------------------------------
            start = System.nanoTime();
            Set<String> Keydowds = new HashSet<>();
            for (syori_rireki1 data1 : syori_rireki1_list) {
                for (syori_rireki2 data2 : data1.syori_rireki2s) {
                    for (syori_rireki3 data3 : data2.syori_rireki3s) {
                        Keydowds.add(data3.getKeyword());
                    }
                }
            }
            if (Keydowds.size() != 0) {
                Map<String, Object> paramMap5 = new HashMap<>();
                paramMap5.put("keys", Keydowds);
                // SQLの発行
                String sql5 = "SELECT keyword, ok_result, ng_result FROM m_keyword2_a WHERE keyword = ANY(ARRAY[ :keys ])";
                NamedParameterJdbcTemplate namedTemplate5 = new NamedParameterJdbcTemplate(jdbcTemplate);
                List<Map<String, Object>> keywords = namedTemplate5.queryForList(sql5, paramMap5);
                for (syori_rireki1 data1 : syori_rireki1_list) {
                    for (syori_rireki2 data2 : data1.syori_rireki2s) {
                        for (syori_rireki3 data3 : data2.syori_rireki3s) {
                            for (Map<String, Object> row : keywords) {
                                String keyword = (String) row.get("keyword");
                                if (data3.getKeyword().equals(keyword)) {
                                    if (data3.getIs_ok()) {
                                        data3.setRun_result((String) row.get("ok_result"));
                                    } else {
                                        data3.setRun_result((String) row.get("ng_result"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            times[5] += System.nanoTime() - start;
            long total = Arrays.stream(times).sum();
            boolean is_show = false;// 時間計測結果を表示したければTrueに変える
            if (is_show) {
                for (int i = 0; i < times.length; i++) {
//  	                System.out.println("Time " + (i) + ": " + times[i] + "ns");
                    System.out.printf("Time %d: %-13dns%10.2f秒\n", i, times[i], times[i] / 1e9);
                }
//  	            System.out.println("Total: " + total + "ns");
                System.out.printf("Total: %-13dns%10.2f秒\n", total, total / 1e9);
            }

            // データが取得できなかった場合は、null値を返す
            if (syori_rireki1_list == null || syori_rireki1_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(syori_rireki1_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 検索(ページネーション対応)
     * 処理履歴１
     */
    @PostMapping(value = { "/api/syori_rireki1_tab_find", "/api/syori_rireki1_find_tab_count" }, produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori_rireki1_tab_find(Integer limit, Integer offset, String findstr, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    OffsetDateTime findDateTime, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {

        try {
            // 処理時間計測
            long[] times = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            long start = System.nanoTime();

            String modeTmp = "ノーマル検索";
            String URL = request.getRequestURI();
            if (URL.toLowerCase().contains("syori_rireki1_find_tab_count")) {
                modeTmp = "件数検索";
            }
            final String mode = modeTmp;

            // 前回検索タイミイングからデータ変更が無い場合、結果を返さず省電力化する
            if (modeTmp.equals("件数検索")) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("update_date", findDateTime);
                String sql = "select count(*) from syori_rireki1 where update_date > :update_date";
                Integer count = namedJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
                if (count == 0) {
                    // レスポンス生成
                    JsonResponse json = new JsonResponse();
                    json.setReturn_code(HttpStatus.OK.value());
                    json.setMsg("前回変更時から変更されたデータが存在しませんでした。");
                    json.Add("NoChanged", String.valueOf(true));
                    json.SetJsonResponse(res);
                    return;
                }
            }
            if (modeTmp.equals("ノーマル検索")) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("update_date", findDateTime);
                // 親
                String sql = "select count(*) from syori_rireki1 where update_date > :update_date";
                Integer count = namedJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
                if (count == 0) {
                    // レスポンス生成
                    JsonResponse json = new JsonResponse();
                    json.setReturn_code(HttpStatus.OK.value());
                    json.setMsg("前回変更時から変更されたデータが存在しませんでした。");
                    json.Add("NoChanged", String.valueOf(true));
                    json.SetJsonResponse(res);
                    return;
                }
            }

            times[1] += System.nanoTime() - start;
            start = System.nanoTime();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("limit", limit);
            paramMap.put("offset", offset);
            List<syori_rireki1> syori_rireki1_list = new ArrayList<syori_rireki1>();

            String sql_count = "count(*)";
            String sql = "  SELECT a.*,b.s1_name from syori_rireki1 a" +
                    "  LEFT JOIN syori1_b b" +
                    "  ON a.s1_id = b.s1_id" +
                    "  AND a.syori_modify_count = b.modify_count" +
                    " あいまい検索 " +
                    "  ORDER BY a.update_date DESC" +
                    "  LIMIT :limit OFFSET :offset";
            // 件数取得用
            String sqlc = "SELECT COUNT(*) from syori_rireki1 a あいまい検索";

            if (findstr.equals("")) {
                sql = sql.replace("あいまい検索", "");
                sqlc = sqlc.replace("あいまい検索", "");
            } else {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs)
                    keys.add("%" + i + "%");
                paramMap.put("keys", keys);

                String like = "WHERE b.s1_name LIKE ANY(ARRAY[ :keys ]) OR a.log LIKE ANY(ARRAY[ :keys ]) OR a.execute_ip LIKE ANY(ARRAY[ :keys ]) OR a.execute_port LIKE ANY(ARRAY[ :keys ]) ";
                sql = sql.replace("あいまい検索", like);
                sqlc = sqlc.replace("あいまい検索", like);
            }

            // 件数取得用のAPIを返す
            if (mode.equals("件数検索")) {
                sql_count = sqlc.replace("結果", sql_count);
                String all_count = namedJdbcTemplate.queryForObject(sql_count, paramMap, String.class);
                // レスポンス生成
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("正常に件数を取得しました");
                json.Add("all_count", String.valueOf(all_count));
                json.SetJsonResponse(res);
                return;
            }

            // BeanPropertyRowMapperでsyori1オブジェクトを作成するためのRowMapperを作成する
            RowMapper<syori_rireki1> rowMapper1 = new BeanPropertyRowMapper<>(syori_rireki1.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<syori_rireki1> comparator1 = Comparator.comparing(syori_rireki1::getS1_id).thenComparing(syori_rireki1::getS_count);

            // Setを生成する
            Set<syori_rireki1> set = new TreeSet<>(comparator1);

            // jdbcTemplateを使用してSQLを実行し、ResultSetから取得したデータをSetに格納する
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            times[2] += System.nanoTime() - start;
            start = System.nanoTime();

            // syori_rireki1の検索
            namedTemplate.query(sql, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori1オブジェクトを作成する
                syori_rireki1 data1 = new syori_rireki1();
                data1.setS1_id(rs.getInt("s1_id"));
                data1.setS_count(rs.getInt("s_count"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori_rireki1>) set).ceiling(data1);
                } else {
                    data1 = rowMapper1.mapRow(rs, rowNum);
                    set.add(data1);
                }
                return null;
            });
            times[3] += System.nanoTime() - start;
            start = System.nanoTime();

            // SetをListに変換する
            syori_rireki1_list = new ArrayList<>(set);
            // 更新日時順に降順ソート
            Collections.sort(syori_rireki1_list, Comparator.comparing(syori_rireki1::getUpdate_date).reversed());

            // 実施者の反映----------------------------------------------------------------
            Set<Integer> updateUIds = new HashSet<>();
            for (syori_rireki1 data : syori_rireki1_list) {
                updateUIds.add(data.getUpdate_u_id());
            }
            if (updateUIds.size() != 0) {
                Map<String, Object> paramMap2 = new HashMap<>();
                paramMap2.put("keys", updateUIds);
                // SQLの発行
                String sql4 = "SELECT user_id, sei,mei FROM m_user_a WHERE user_id = ANY(ARRAY[ :keys ])";
                NamedParameterJdbcTemplate namedTemplate2 = new NamedParameterJdbcTemplate(jdbcTemplate_com);
                List<Map<String, Object>> userNames = namedTemplate2.queryForList(sql4, paramMap2);
                // syori_rireki1のupdate_userにuser_nameを書き戻す
                for (syori_rireki1 data : syori_rireki1_list) {
                    for (Map<String, Object> row : userNames) {
                        Integer userId = (Integer) row.get("user_id");
                        if (data.getUpdate_u_id().equals(userId)) {
                            String userName = (String) row.get("sei") + " " + (String) row.get("mei");
                            data.setUpdate_user(userName);
                            break;
                        }
                    }
                }
            }
            times[4] += System.nanoTime() - start;
            start = System.nanoTime();

            long total = Arrays.stream(times).sum();
            boolean is_show = false;// 時間計測結果を表示したければTrueに変える
            if (is_show) {
                for (int i = 0; i < times.length; i++) {
                    if (times[i] == 0) continue;
                    System.out.printf("Time %d: %-13dns%10.2f秒\n", i, times[i], times[i] / 1e9);
                }
                System.out.printf("Total: %-13dns%10.2f秒\n", total, total / 1e9);
            }

            // データが取得できなかった場合は、null値を返す
            if (syori_rireki1_list == null || syori_rireki1_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(syori_rireki1_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 検索(ページネーション対応)
     * 処理履歴2
     */
    @PostMapping(value = { "/api/syori_rireki2_tab_find" }, produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori_rireki2_tab_find(Integer s1_id, Integer s_count, Integer syori_modify_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s_count", s_count);
            paramMap.put("syori_modify_count", syori_modify_count);
            List<syori_rireki2> syori_rireki2_list = new ArrayList<syori_rireki2>();

            String sql = "  SELECT b.*,a.* from syori_rireki2 a" +
                    "  LEFT JOIN syori2_b b" +
                    "  ON a.s1_id = b.s1_id" +
                    "  AND a.s2_id = b.s2_id" +
                    "  WHERE a.s1_id = :s1_id and a.s_count = :s_count and b.modify_count = :syori_modify_count";

            // BeanPropertyRowMapperでsyori1オブジェクトを作成するためのRowMapperを作成する
            RowMapper<syori_rireki2> rowMapper2 = new BeanPropertyRowMapper<>(syori_rireki2.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<syori_rireki2> comparator2 = Comparator.comparing(syori_rireki2::getS1_id).thenComparing(syori_rireki2::getS_count).thenComparing(syori_rireki2::getS2_id);

            // Setを生成する
            Set<syori_rireki2> set = new TreeSet<>(comparator2);

            // jdbcTemplateを使用してSQLを実行し、ResultSetから取得したデータをSetに格納する
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // syori_rireki2の検索
            namedTemplate.query(sql, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori2オブジェクトを作成する
                syori_rireki2 data2 = new syori_rireki2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS_count(rs.getInt("s_count"));
                data2.setS2_id(rs.getInt("s2_id"));

                if (set.contains(data2)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori_rireki2>) set).ceiling(data2);
                } else {
                    data2 = rowMapper2.mapRow(rs, rowNum);
                    set.add(data2);
                }
                return null;
            });

            // SetをListに変換する
            syori_rireki2_list = new ArrayList<>(set);
            // S2_id順に降順ソート
            Collections.sort(syori_rireki2_list, Comparator.comparing(syori_rireki2::getS2_id));

            // データが取得できなかった場合は、null値を返す
            if (syori_rireki2_list == null || syori_rireki2_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(syori_rireki2_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 検索(ページネーション対応)
     * 処理履歴3
     */
    @PostMapping(value = { "/api/syori_rireki3_tab_find" }, produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori_rireki3_tab_find(Integer s1_id, Integer s_count, Integer s2_id, Integer syori_modify_count, Integer m_keyword_modify_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s_count", s_count);
            paramMap.put("s2_id", s2_id);
            paramMap.put("syori_modify_count", syori_modify_count);
            paramMap.put("m_keyword_modify_count", m_keyword_modify_count);
            List<syori_rireki3> syori_rireki3_list = new ArrayList<syori_rireki3>();

            String sql = "  SELECT b.*,a.*,c.ok_result,c.ng_result from syori_rireki3 a" +
                    "  LEFT JOIN syori3_b b" +
                    "  ON a.s1_id = b.s1_id" +
                    "  AND a.s2_id = b.s2_id" +
                    "  AND a.s3_id = b.s3_id" +
                    "  LEFT JOIN m_keyword2_b c" +
                    "  ON b.keyword = c.keyword" +
                    "  WHERE a.s1_id = :s1_id and a.s_count = :s_count " +
                    "  and a.s2_id = :s2_id and b.modify_count = :syori_modify_count" +
                    "  and c.modify_count = :m_keyword_modify_count";

            // BeanPropertyRowMapperでsyori1オブジェクトを作成するためのRowMapperを作成する
            RowMapper<syori_rireki3> rowMapper3 = new BeanPropertyRowMapper<>(syori_rireki3.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<syori_rireki3> comparator3 = Comparator.comparing(syori_rireki3::getS1_id).thenComparing(syori_rireki3::getS_count).thenComparing(syori_rireki3::getS2_id).thenComparing(syori_rireki3::getS3_id);

            // Setを生成する
            Set<syori_rireki3> set = new TreeSet<>(comparator3);

            // jdbcTemplateを使用してSQLを実行し、ResultSetから取得したデータをSetに格納する
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // syori_rireki3の検索
            namedTemplate.query(sql, paramMap, (rs, rowNum) -> {
                // ResultSetからsyori3オブジェクトを作成する
                syori_rireki3 data3 = new syori_rireki3();
                data3.setS1_id(rs.getInt("s1_id"));
                data3.setS_count(rs.getInt("s_count"));
                data3.setS2_id(rs.getInt("s2_id"));
                data3.setS3_id(rs.getInt("s3_id"));

                if (set.contains(data3)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data3 = ((TreeSet<syori_rireki3>) set).ceiling(data3);
                } else {
                    data3 = rowMapper3.mapRow(rs, rowNum);
                    set.add(data3);
                }
                return null;
            });

            // SetをListに変換する
            syori_rireki3_list = new ArrayList<>(set);
            // S2_id順に降順ソート
            Collections.sort(syori_rireki3_list, Comparator.comparing(syori_rireki3::getS3_id));

            // データが取得できなかった場合は、null値を返す
            if (syori_rireki3_list == null || syori_rireki3_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(syori_rireki3_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * ログ取得(処理履歴1)
     */
    @PostMapping(value = "/api/get_syori_rireki1_log", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void get_syori_rireki1_log(Integer s1_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "select * from syori_rireki1 where s1_id = :s1_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s_count", s_count);
            RowMapper<syori_rireki1> rowMapper = new BeanPropertyRowMapper<syori_rireki1>(syori_rireki1.class);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            syori_rireki1 syori_rireki1 = namedTemplate.queryForObject(sql, paramMap, rowMapper);

            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(syori_rireki1.getLog());
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * ログ取得(処理履歴2)
     */
    @PostMapping(value = "/api/get_syori_rireki2_log", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void get_syori_rireki2_log(Integer s1_id, Integer s2_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "select * from syori_rireki2 where s1_id = :s1_id and s2_id = :s2_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s2_id", s2_id);
            paramMap.put("s_count", s_count);
            RowMapper<syori_rireki2> rowMapper = new BeanPropertyRowMapper<syori_rireki2>(syori_rireki2.class);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            syori_rireki2 syori_rireki2 = namedTemplate.queryForObject(sql, paramMap, rowMapper);

            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(syori_rireki2.getLog());
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * ログ取得(処理履歴3)
     */
    @PostMapping(value = "/api/get_syori_rireki3_log", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void get_syori_rireki3_log(Integer s1_id, Integer s2_id, Integer s3_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "select * from syori_rireki3 where s1_id = :s1_id and s2_id = :s2_id and s3_id = :s3_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s2_id", s2_id);
            paramMap.put("s3_id", s3_id);
            paramMap.put("s_count", s_count);
            RowMapper<syori_rireki3> rowMapper = new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            syori_rireki3 syori_rireki3 = namedTemplate.queryForObject(sql, paramMap, rowMapper);

            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(syori_rireki3.getLog());
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * スクリーンショットダウンロード
     */
    @PostMapping(value = "/api/get_syori_rireki3_screen_shot_file", produces = "application/json;charset=utf-8")
    @ResponseBody
    public ResponseEntity<Resource> get_syori_rireki3_screen_shot_file(Integer s1_id, Integer s2_id, Integer s3_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {
            // DBを検索しスクリーンショットのパスを取得
            String sql = "select * from syori_rireki3 where s1_id = :s1_id and s2_id = :s2_id and s3_id = :s3_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s2_id", s2_id);
            paramMap.put("s3_id", s3_id);
            paramMap.put("s_count", s_count);

            RowMapper<syori_rireki3> rowMapper = new BeanPropertyRowMapper<syori_rireki3>(syori_rireki3.class);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            syori_rireki3 syori_rireki3 = namedTemplate.queryForObject(sql, paramMap, rowMapper);

            // 画像読込
            String filePath = syori_rireki3.getScreen_shot_filepath();
            if (filePath.isEmpty()) return null;
            Path imagePath = Paths.get(filePath);
            byte[] imageBytes = Files.readAllBytes(imagePath);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + imagePath.getFileName().toString());
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(imageBytes.length)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
            return null;
        }
    }

    /**
     * 処理1中止指令
     */
    @PostMapping(value = "/api/stop_syori_rireki1", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void stop_syori_rireki1(Integer s1_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "update syori_rireki1 set is_suspension = true ,result_type = 3 where s1_id = :s1_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s_count", s_count);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            int rowCount = namedTemplate.update(sql, paramMap);
            if (rowCount != 1) {
                // JSON形式でレスポンスを返す
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.INTERNAL_SERVER_ERROR.value());
                json.setData("中止に失敗しました:" + rowCount + "件");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData("更新しました:" + rowCount + "件");
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 処理2中止指令
     */
    @PostMapping(value = "/api/stop_syori_rireki2", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void stop_syori_rireki2(Integer s1_id, Integer s2_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "update syori_rireki2 set is_suspension = true ,result_type = 3 where s1_id = :s1_id and s2_id = :s2_id and s_count = :s_count";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s2_id", s2_id);
            paramMap.put("s_count", s_count);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            int rowCount = namedTemplate.update(sql, paramMap);

            if (rowCount != 1) {
                // JSON形式でレスポンスを返す
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.INTERNAL_SERVER_ERROR.value());
                json.setData("中止に失敗しました:" + rowCount + "件");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData("更新しました:" + rowCount + "件");
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 処理3中止指令
     */
    @PostMapping(value = "/api/stop_syori_rireki3", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void stop_syori_rireki3(Integer s1_id, Integer s2_id, Integer s3_id, Integer s_count, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            String sql = "update syori_rireki3 set is_suspension = true ,result_type = 3 where s1_id = :s1_id and s2_id = :s2_id and s3_id = :s3_id and s_count = :s_count";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("s1_id", s1_id);
            paramMap.put("s2_id", s2_id);
            paramMap.put("s3_id", s3_id);
            paramMap.put("s_count", s_count);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            int rowCount = namedTemplate.update(sql, paramMap);

            if (rowCount != 1) {
                // JSON形式でレスポンスを返す
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.INTERNAL_SERVER_ERROR.value());
                json.setData("中止に失敗しました:" + rowCount + "件");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData("更新しました:" + rowCount + "件");
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    // ログ保存のテスト用
    @PostMapping(value = "/api/export_s_excel", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void export_s_excel(HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        Kai9Utils.makeLog("info", "This is a log message.", this.getClass());
    }

    // テスト用の受け口:テストした事に応じて作り変えを
//  	@PostMapping(value = "/api/hoge", produces = "application/json;charset=utf-8")
//  	@ResponseBody	
//	public void hoge(HttpServletResponse res,HttpServletRequest request) throws CloneNotSupportedException, IOException {
//  			System.out.println("hoge");
//  	}

}
