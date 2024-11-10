package kai9.auto.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kai9.libs.JsonResponse;
import kai9.libs.Kai9Utils;
import kai9.auto.dto.syori1_Request;
import kai9.auto.dto.syori2_Request;
import kai9.auto.dto.syori3_Request;
import kai9.auto.model.syori1;
import kai9.auto.model.syori2;
import kai9.auto.model.syori3;
import kai9.auto.service.syori1_Service;
import kai9.auto.service.syori2_Service;
import kai9.auto.service.syori3_Service;

/**
 * 処理設定_親 :コントローラ
 */
@RestController
public class syori1_Controller {

    @Autowired
    private syori1_Service syori1_service;

    @Autowired
    private syori2_Service syori2_service;

    @Autowired
    private syori3_Service syori3_service;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    /**
     * CUD操作
     */
    @PostMapping(value = { "/api/syori1_create", "/api/syori1_update", "/api/syori1_delete" }, produces = "application/json;charset=utf-8")
    public void create(
            @RequestPart(value = "s_excel", required = false)
            MultipartFile file_s_excel,
            @RequestPart("syori1") @Valid
            syori1_Request syori1,
            HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        try {
            if (file_s_excel != null) syori1.setS_excel(file_s_excel.getBytes());
            String URL = request.getRequestURI();
            String sql = "";
            syori1 syori1_result = null;
            if (URL.toLowerCase().contains("syori1_create")) {
                // 処理番号に指定がある場合、既に存在しないかを確認
                if (syori1.getS1_id() != 0 && syori1_service.findById(syori1.getS1_id()) != null) {
                    // JSON形式でレスポンスを返す
                    JsonResponse json = new JsonResponse();
                    json.setReturn_code(HttpStatus.CONFLICT.value());
                    json.setMsg("処理設定_親【S1_id=" + syori1.getS1_id() + "】　は既に登録済です。「新規」ではなく「変更」で登録して下さい。");
                    json.SetJsonResponse(res);
                    return;
                }
                // 新規:syori1
                syori1_result = syori1_service.create(syori1);
                // 新規:syori2
                for (syori2_Request syori2 : syori1.getSyori2s()) {
                    syori2.setS1_id(syori1_result.getS1_id());
                    syori2.setModify_count(syori1_result.getModify_count());
                    syori2_service.create(syori2);
                    // 新規:syori3
                    for (syori3_Request syori3 : syori2.getSyori3s()) {
                        syori3.setS1_id(syori1_result.getS1_id());
                        syori3.setModify_count(syori1_result.getModify_count());
                        syori3_service.create(syori3);
                    }
                }

            } else {
                // 排他制御
                // 更新回数が異なる場合エラー
                sql = "select modify_count from syori1_a where s1_id = ?";
                String modify_count = jdbcTemplate.queryForObject(sql, String.class, syori1.getS1_id());
                if (!modify_count.equals(String.valueOf(syori1.getModify_count()))) {
                    // JSON形式でレスポンスを返す
                    JsonResponse json = new JsonResponse();
                    json.setReturn_code(HttpStatus.CONFLICT.value());
                    json.setMsg("処理設定_親【S1_id=" + syori1.getS1_id() + "】　で排他エラー発生。ページリロード後に再登録して下さい。");
                    json.SetJsonResponse(res);
                    return;
                }

                if (URL.toLowerCase().contains("syori1_update")) {
                    // 更新
                    syori1_result = syori1_service.update(syori1);// ←内部で子孫の全てをdelete
                    // 更新:syori2(delete & insert)
                    for (syori2_Request syori2 : syori1.getSyori2s()) {
                        syori2.setModify_count(syori1_result.getModify_count());
                        syori2_service.create(syori2);
                        // 更新:syori3(delete & insert)
                        for (syori3_Request syori3 : syori2.getSyori3s()) {
                            syori3.setModify_count(syori1_result.getModify_count());
                            syori3_service.create(syori3);
                        }
                    }
                }

                if (URL.toLowerCase().contains("syori1_delete")) {
                    // 削除(フラグON/OFF反転)
                    syori1_result = syori1_service.delete(syori1);
                    // 削除:syori2
                    for (syori2_Request syori2 : syori1.getSyori2s()) {
                        syori2.setS1_id(syori1_result.getS1_id());
                        syori2_service.delete(syori2);
                        // 削除:syori3
                        for (syori3_Request syori3 : syori2.getSyori3s()) {
                            syori3.setS1_id(syori1_result.getS1_id());
                            syori3_service.delete(syori3);
                        }
                    }
                }
            }

            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setMsg("正常に登録しました");
            json.Add("s1_id", String.valueOf(syori1_result.getS1_id()));
            json.Add("modify_count", String.valueOf(syori1_result.getModify_count()));
            json.SetJsonResponse(res);
            return;

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 件数を返す
     */
    @PostMapping(value = "/api/syori1_count", produces = "application/json;charset=utf-8")
    public void syori1_count(String findstr, boolean isDelDataShow, HttpServletResponse res) throws CloneNotSupportedException, IOException {
        try {
            String Delflg = "";
            if (!isDelDataShow) Delflg = "and delflg = false ";

            String where = "";
            String all_count = "0";
            if (findstr != "") {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs) {
                    keys.add("%" + i + "%");
                }
                where = " and("
                        + " s1_name like any(array[ :keys ])"
                        + " or"
                        + " run_host like any(array[ :keys ])"
                        + " or"
                        + " run_timing like any(array[ :keys ])"
                        + " or"
                        + " execute_ip like any(array[ :keys ])"
                        + " or"
                        + " execute_port like any(array[ :keys ])"
                        + " or"
                        + " api_url like any(array[ :keys ])"
                        + " or"
                        + " bikou like any(array[ :keys ])"
                        + " or"
                        + " s_excel_filename like any(array[ :keys ])"
                        + ")";
                MapSqlParameterSource Param = new MapSqlParameterSource();
                Param.addValue("keys", keys);

                String sql = "select count(*) from syori1_a where 0 = 0 " + Delflg + where;
                all_count = namedJdbcTemplate.queryForObject(sql, Param, String.class);
            } else {
                String sql = "select count(*) from syori1_a where 0 = 0 " + Delflg;
                all_count = jdbcTemplate.queryForObject(sql, String.class);
            }

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
     * 検索(ページネーション対応)
     */
    @PostMapping(value = { "/api/syori1_find" }, produces = "application/json;charset=utf-8")
    @ResponseBody
    public void syori1_find(Integer limit, Integer offset, String findstr, boolean isDelDataShow, HttpServletResponse res, HttpServletRequest request, Optional<Integer> s1_id) throws CloneNotSupportedException, IOException {
        try {
            String delflg = "";
            String sql = "";
            String sql1 = "a.s1_id,a.modify_count,s1_name,run_host,run_timing,execute_ip,execute_port,execute_date,api_url,bikou,null as s_excel,s_excel_filename,col_s1_name,col_s1_id,col_run_host,col_run_timing,col_run_parameter,col_bikou,col_run_order,col_sheetname,col_is_do,col_is_normal,col_r_start_time,col_r_end_time,col_result,a.col_ng_stop,col_scenario,col_s_outline,update_u_id,update_date,delflg";
            String sql2 = "b.*";
            String sql3 = "c.*";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("limit", limit);
            paramMap.put("offset", offset);

            List<syori1> syori1_list = new ArrayList<syori1>();
            if (findstr.equals("")) {
                if (!isDelDataShow) {
                    delflg = " and a.delflg = false";
                }
                if (s1_id.isPresent()) {
                    // s1_idが指定された場合、単一処理の検索モードとして扱う
                    delflg = " and a.s1_id = :s1_id";
                    paramMap.put("s1_id", s1_id.get());
                }
                // BLOBは個別に取り出すのでnullに挿げ替える
                sql = "select 結果 from syori1_a a"
                        + " left join syori2_a b on a.s1_id = b.s1_id"
                        + " left join syori3_a c on a.s1_id = c.s1_id and c.s2_id = b.s2_id"
                        + " where a.s1_id IN (SELECT s1_id FROM syori1_a LIMIT :limit OFFSET :offset)" + delflg + " ORDER BY a.s1_id";

            } else {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs) {
                    keys.add("%" + i + "%");
                }

                if (!isDelDataShow) delflg = " and delflg = false ";

                // BLOBは個別に取り出すのでnullに挿げ替える
                sql = "SELECT 結果\n"
                        + "FROM syori1_a a\n"
                        + "LEFT JOIN syori2_a b ON a.s1_id = b.s1_id\n"
                        + "LEFT JOIN syori3_a c ON a.s1_id = c.s1_id AND c.s2_id = b.s2_id\n"
                        + "WHERE a.s1_id IN (\n"
                        + "  SELECT s1_id\n"
                        + "  FROM syori1_a\n"
                        + "  ORDER BY s1_id ASC\n"
                        + "  LIMIT :limit OFFSET :offset\n"
                        + ")\n"
                        + "" + delflg + " AND (\n"
                        + "  a.s1_name LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.run_host LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.run_timing LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.execute_ip LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.execute_port LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.api_url LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.bikou LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR a.s_excel_filename LIKE ANY(ARRAY[ :keys ])\n"
                        + ")\n"
                        + "ORDER BY a.s1_id";

                paramMap.put("keys", keys);
            }
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

            // 処理時間計測
            long[] times = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            // jdbcTemplateを使用してSQLを実行し、ResultSetから取得したデータをSetに格納する
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            // syori1検索
            sql1 = sql.replace("結果", sql1);
            namedTemplate.query(sql1, paramMap, (rs, rowNum) -> {
                long start0 = System.nanoTime();
                // ResultSetからsyori1オブジェクトを作成する
                long start = System.nanoTime();
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                times[7] += System.nanoTime() - start;

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    data1 = rowMapper1.mapRow(rs, rowNum);
                    set.add(data1);
                    data1.syori2s = new TreeSet<>(comparator2); // Comparatorをセットする
                }

                times[0] += System.nanoTime() - start0;
                return null;
            });
            // syori2検索
            sql2 = sql.replace("結果", sql2);
            namedTemplate.query(sql2, paramMap, (rs, rowNum) -> {
                long start0 = System.nanoTime();
                // ResultSetからsyori1オブジェクトを作成する
                long start = System.nanoTime();
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                times[7] += System.nanoTime() - start;

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                start = System.nanoTime();
                syori2 data2 = new syori2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));
                times[8] += System.nanoTime() - start;

                if (data1.syori2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori2>) data1.syori2s).ceiling(data2);
                } else {
                    data2 = rowMapper2.mapRow(rs, rowNum);
                    data1.syori2s.add(data2);

                    start = System.nanoTime();
                    data2.syori3s = new TreeSet<>(comparator3); // Comparatorをセットする
                    times[5] += System.nanoTime() - start;
                }

                times[0] += System.nanoTime() - start0;
                return null;
            });
            // syori3検索
            sql3 = sql.replace("結果", sql3);
            namedTemplate.query(sql3, paramMap, (rs, rowNum) -> {
                long start0 = System.nanoTime();
                // ResultSetからsyori1オブジェクトを作成する
                long start = System.nanoTime();
                syori1 data1 = new syori1();
                data1.setS1_id(rs.getInt("s1_id"));

                times[7] += System.nanoTime() - start;

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<syori1>) set).ceiling(data1);
                } else {
                    return null;
                }

                // 子(syori2)を親(syori1)に紐付ける
                start = System.nanoTime();
                syori2 data2 = new syori2();
                data2.setS1_id(rs.getInt("s1_id"));
                data2.setS2_id(rs.getInt("s2_id"));
                times[8] += System.nanoTime() - start;

                if (data1.syori2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<syori2>) data1.syori2s).ceiling(data2);
                } else {
                    return null;
                }

                // 孫(syori3)を子(syori2)に紐付ける
                start = System.nanoTime();
                syori3 data3 = new syori3();
                data3.setS1_id(rs.getInt("s1_id"));
                data3.setS2_id(rs.getInt("s2_id"));
                data3.setS3_id(rs.getInt("s3_id"));
                times[9] += System.nanoTime() - start;

                if (data2.syori3s.contains(data3)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    start = System.nanoTime();
                    data3 = ((TreeSet<syori3>) data2.syori3s).ceiling(data3);
                    times[3] += System.nanoTime() - start;
                } else {
                    data3 = rowMapper3.mapRow(rs, rowNum);
                    start = System.nanoTime();
                    data2.syori3s.add(data3);
                    times[4] += System.nanoTime() - start;
                }
                times[0] += System.nanoTime() - start0;
                return null;
            });
            long total = Arrays.stream(times).sum();
//	  			Arrays.stream(times).forEach(t -> System.out.println(t + "ns"));
//	  			System.out.println("Total: " + total + "ns");	  			

            // SetをListに変換する
            syori1_list = new ArrayList<>(set);

            // データが取得できなかった場合は、null値を返す
            if (syori1_list == null || syori1_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(syori1_list));
            json.SetJsonResponse(res);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }

    }

    /**
     * ファイルダウンロード(s_excel)
     */
    @PostMapping(value = "/api/syori1_s_excel_download", produces = "application/json;charset=utf-8")
    public ResponseEntity<Resource> syori1_s_excel_download(int s1_id, int modify_count) throws CloneNotSupportedException, IOException {

        String sql = "select * from syori1_b where s1_id = ? and modify_count = ?";
        RowMapper<syori1> rowMapper = new BeanPropertyRowMapper<syori1>(syori1.class);
        syori1 syori1 = jdbcTemplate.queryForObject(sql, rowMapper, s1_id, modify_count);

        ByteArrayResource resource = new ByteArrayResource(syori1.getS_excel());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + syori1.getS_excel_filename());
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(syori1.getS_excel().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 履歴検索
     */
    @PostMapping(value = "/api/syori1_history_find", produces = "application/json;charset=utf-8")
    public void syori1_history_find(int s1_id, HttpServletResponse res) throws CloneNotSupportedException, IOException {
        // BLOBは個別に取り出すのでnullに挿げ替える
        String sql = "select s1_id,modify_count,s1_name,run_host,run_timing,execute_ip,execute_port,execute_date,api_url,bikou,null,s_excel_filename,col_s1_name,col_s1_id,col_run_host,col_run_timing,col_run_parameter,col_bikou,col_run_order,col_sheetname,col_is_do,col_is_normal,col_r_start_time,col_r_end_time,col_result,col_ng_stop,col_scenario,col_s_outline,update_u_id,update_date,delflg from syori1_b where s1_id = :s1_id order by modify_count desc";
        RowMapper<syori1> rowMapper = new BeanPropertyRowMapper<syori1>(syori1.class);
        MapSqlParameterSource Param = new MapSqlParameterSource();
        Param.addValue("s1_id", s1_id);
        List<syori1> syori1_list = namedJdbcTemplate.query(sql, Param, rowMapper);

        // データが取得できなかった場合は、null値を返す
        if (syori1_list == null || syori1_list.size() == 0) {
            return;
        }

        // JSON形式でレスポンスを返す
        JsonResponse json = new JsonResponse();
        json.setReturn_code(HttpStatus.OK.value());
        json.setData(Kai9Utils.getJsonData((Object) syori1_list));
        json.SetJsonResponse(res);
        return;
    }

}
