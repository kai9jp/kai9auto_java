package kai9.auto.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
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
import kai9.auto.dto.m_keyword1_Request;
import kai9.auto.dto.m_keyword2_Request;
import kai9.auto.model.m_keyword1;
import kai9.auto.model.m_keyword2;
import kai9.auto.service.m_keyword1_Service;
import kai9.auto.service.m_keyword2_Service;

/**
 * 処理設定_親 :コントローラ
 */
@RestController
public class m_keyword_Controller {

    @Autowired
    private m_keyword1_Service m_keyword1_service;

    @Autowired
    private m_keyword2_Service m_keyword2_service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    /**
     * CUD操作
     */
    @PostMapping(value = { "/api/m_keyword_create", "/api/m_keyword_update" }, produces = "application/json;charset=utf-8")
    public void m_keyword_create(
            @RequestPart(value = "excel", required = false)
            MultipartFile file_excel,
            @RequestPart("m_keyword1") @Valid
            m_keyword1_Request m_keyword1,
            HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        try {
            if (file_excel != null) m_keyword1.setExcel(file_excel.getBytes());
            String URL = request.getRequestURI();
            String sql = "";
            m_keyword1 m_keyword1_result = null;
            if (URL.toLowerCase().contains("m_keyword_create")) {
                // 新規:親
                m_keyword1_result = m_keyword1_service.create(m_keyword1);
                // 新規:子
                for (m_keyword2_Request m_keyword2 : m_keyword1.getM_keyword2s()) {
                    m_keyword2.setModify_count(m_keyword1_result.getModify_count());
                    m_keyword2_service.create(m_keyword2);
                }
            } else {
                // 排他制御
                // 更新回数が異なる場合エラー
                sql = "select modify_count from m_keyword1_a";
                String modify_count = jdbcTemplate.queryForObject(sql, String.class);
                if (!modify_count.equals(String.valueOf(m_keyword1.getModify_count()))) {
                    // JSON形式でレスポンスを返す
                    JsonResponse json = new JsonResponse();
                    json.setReturn_code(HttpStatus.CONFLICT.value());
                    json.setMsg("【処理設定_親】で排他エラー発生。ページリロード後に再登録して下さい。");
                    json.SetJsonResponse(res);
                    return;
                }

                if (URL.toLowerCase().contains("m_keyword_update")) {
                    // 更新:親
                    m_keyword1_result = m_keyword1_service.update(m_keyword1);// ←内部で親子両方をdelete
                    // 更新:子(delete & insert)
                    for (m_keyword2_Request m_keyword2 : m_keyword1.getM_keyword2s()) {
                        m_keyword2.setModify_count(m_keyword1_result.getModify_count());
                        m_keyword2_service.create(m_keyword2);
                    }
                }
            }

            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setMsg("正常に登録しました");
            json.Add("modify_count", String.valueOf(m_keyword1_result.getModify_count()));
            json.SetJsonResponse(res);
            return;

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 子の件数を返す
     */
    @PostMapping(value = "/api/m_keyword2_count", produces = "application/json;charset=utf-8")
    public void m_keyword2_count(String findstr, HttpServletResponse res) throws CloneNotSupportedException, IOException {
        try {
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
                        + " keyword like any(array[ :keys ])"
                        + " or"
                        + " func_name like any(array[ :keys ])"
                        + " or"
                        + " ok_result like any(array[ :keys ])"
                        + " or"
                        + " ng_result like any(array[ :keys ])"
                        + " or"
                        + " param1 like any(array[ :keys ])"
                        + " or"
                        + " param2 like any(array[ :keys ])"
                        + " or"
                        + " param3 like any(array[ :keys ])"
                        + " or"
                        + " variable1 like any(array[ :keys ])"
                        + " or"
                        + " bikou like any(array[ :keys ])"
                        + ")";
                MapSqlParameterSource Param = new MapSqlParameterSource();
                Param.addValue("keys", keys);

                String sql = "select count(*) from m_keyword2_a where 0 = 0 " + where;
                all_count = namedJdbcTemplate.queryForObject(sql, Param, String.class);
            } else {
                String sql = "select count(*) from m_keyword2_a";
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
     * 全量検索
     */
    @PostMapping(value = "/api/m_keyword1_find", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void m_keyword1_find(HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {
            String sql = "SELECT " +
                    "a.modify_count, " +
                    "NULL, " +
                    "a.excel_filename, " +
                    "a.update_u_id, " +
                    "a.update_date, " +
                    "b.no, " +
                    "b.keyword, " +
                    "b.func_name, " +
                    "b.ok_result, " +
                    "b.ng_result, " +
                    "b.param1, " +
                    "b.param2, " +
                    "b.param3, " +
                    "b.variable1, " +
                    "b.bikou " +
                    "FROM " +
                    "m_keyword1_a a " +
                    "LEFT JOIN m_keyword2_a b ON a.modify_count = b.modify_count";

            // BeanPropertyRowMapperで各クラスのオブジェクトを作成するためのRowMapperを作成する
            RowMapper<m_keyword1> rowMapper1 = new BeanPropertyRowMapper<>(m_keyword1.class);
            RowMapper<m_keyword2> rowMapper2 = new BeanPropertyRowMapper<>(m_keyword2.class);

            // オブジェクト同士を比較するためのComparatorを作成する
            Comparator<m_keyword1> comparator1 = Comparator.comparing(m_keyword1::getModify_count);
            Comparator<m_keyword2> comparator2 = Comparator.comparing(m_keyword2::getModify_count).thenComparing(m_keyword2::getKeyword);

            // Setを生成する
            Set<m_keyword1> set = new TreeSet<>(comparator1);

            namedJdbcTemplate.query(sql, (rs, rowNum) -> {
                m_keyword1 data1 = new m_keyword1();
                data1.setModify_count(rs.getInt("modify_count"));

                if (set.contains(data1)) {
                    // Setに含まれている場合には、Setから要素を取り出し、挿げ替える
                    data1 = ((TreeSet<m_keyword1>) set).ceiling(data1);
                } else {
                    data1 = rowMapper1.mapRow(rs, rowNum);
                    set.add(data1);
                    data1.m_keyword2s = new TreeSet<>(comparator2); // Comparatorをセットする
                }

                // 子(syori2)を親(syori1)に紐付ける
                m_keyword2 data2 = new m_keyword2();
                data2.setModify_count(rs.getInt("modify_count"));
                data2.setKeyword(rs.getString("keyword"));

                if (data1.m_keyword2s.contains(data2)) {
                    // 既に含まれている場合には、Setから要素を取り出し、挿げ替える
                    data2 = ((TreeSet<m_keyword2>) data1.m_keyword2s).ceiling(data2);
                } else {
                    data2 = rowMapper2.mapRow(rs, rowNum);
                    data1.m_keyword2s.add(data2);
                }
                return null;
            });
            // SetをListに変換する
            List<m_keyword1> m_keyword1_list = new ArrayList<m_keyword1>();
            m_keyword1_list = new ArrayList<>(set);

            // データが取得できなかった場合は、null値を返す
            if (m_keyword1_list == null || m_keyword1_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(m_keyword1_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 検索(ページネーション対応)
     */
    @PostMapping(value = "/api/m_keyword2_find", produces = "application/json;charset=utf-8")
    @ResponseBody
    public void m_keyword2_find(Integer limit, Integer offset, String findstr, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("limit", limit);
            paramMap.put("offset", offset);

            List<m_keyword2> m_keyword2_list = new ArrayList<m_keyword2>();

            String sql = "";
            if (findstr.equals("")) {
                sql = "select * from m_keyword2_a order by no asc limit :limit offset :offset";
            } else {
                // あいまい検索準備
                findstr = findstr.replace("　", " ");
                String[] strs = findstr.split(" ");
                Set<String> keys = new HashSet<>();
                for (String i : strs) {
                    keys.add("%" + i + "%");
                }

                sql = "select * from m_keyword2_a where 0 = 0 and (\n"
                        + "  keyword LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR func_name LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR ok_result LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR ng_result LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR param1 LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR param2 LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR param3 LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR variable1 LIKE ANY(ARRAY[ :keys ])\n"
                        + "  OR bikou LIKE ANY(ARRAY[ :keys ])\n"
                        + ")\n"
                        + " order by no limit :limit offset :offset ";
                paramMap.put("keys", keys);
            }

            RowMapper<m_keyword2> rowMapper = new BeanPropertyRowMapper<m_keyword2>(m_keyword2.class);
            m_keyword2_list = namedJdbcTemplate.query(sql, paramMap, rowMapper);

            // データが取得できなかった場合は、null値を返す
            if (m_keyword2_list == null || m_keyword2_list.size() == 0) {
                JsonResponse json = new JsonResponse();
                json.setReturn_code(HttpStatus.OK.value());
                json.setMsg("対象データが存在しません");
                json.SetJsonResponse(res);
                return;
            }
            // JSON形式でレスポンスを返す
            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setData(Kai9Utils.getJsonData(m_keyword2_list));
            json.SetJsonResponse(res);
        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * ファイルダウンロード(excel)
     */
    @PostMapping(value = "/api/m_keyword1_excel_download", produces = "application/json;charset=utf-8")
    public ResponseEntity<Resource> m_keyword1_excel_download(int modify_count) throws CloneNotSupportedException, IOException {

        String sql = "select * from m_keyword1_b where modify_count = ?";
        RowMapper<m_keyword1> rowMapper = new BeanPropertyRowMapper<m_keyword1>(m_keyword1.class);
        m_keyword1 m_keyword1 = jdbcTemplate.queryForObject(sql, rowMapper, modify_count);

        ByteArrayResource resource = new ByteArrayResource(m_keyword1.getExcel());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + m_keyword1.getExcel_filename());
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(m_keyword1.getExcel().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 履歴検索(親)
     */
    @PostMapping(value = "/api/m_keyword1_history_find", produces = "application/json;charset=utf-8")
    public void m_keyword1_history_find(HttpServletResponse res) throws CloneNotSupportedException, IOException {
        // BLOBは個別に取り出すのでnullに挿げ替える
        String sql = "select modify_count,null,excel_filename,update_u_id,update_date from m_keyword1_b order by modify_count desc";
        RowMapper<m_keyword1> rowMapper = new BeanPropertyRowMapper<m_keyword1>(m_keyword1.class);
        List<m_keyword1> m_keyword1_list = namedJdbcTemplate.query(sql, rowMapper);

        // データが取得できなかった場合は、null値を返す
        if (m_keyword1_list == null || m_keyword1_list.size() == 0) {
            return;
        }
        // JSON形式でレスポンスを返す
        JsonResponse json = new JsonResponse();
        json.setReturn_code(HttpStatus.OK.value());
        json.setData(Kai9Utils.getJsonData((Object) m_keyword1_list));
        json.SetJsonResponse(res);
        return;
    }

    /**
     * 存在確認
     */
    @PostMapping(value = "/api/m_keyword2_a_find_all_keywords", produces = "application/json;charset=utf-8")
    public void m_keyword2_a_find_all_keywords(HttpServletResponse res) throws CloneNotSupportedException, IOException {

        // SQLクエリを実行して、keyword列の全ての値を取得
        String sql = "SELECT keyword FROM m_keyword2_a";
        List<String> keywords = jdbcTemplate.queryForList(sql, String.class);

        // データが取得できなかった場合は、null値を返す
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        // JSON形式でレスポンスを返す
        JsonResponse json = new JsonResponse();
        json.setReturn_code(HttpStatus.OK.value());
        json.setData(Kai9Utils.getJsonData(keywords));
        json.SetJsonResponse(res);
        return;
    }

}
