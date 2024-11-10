package kai9.auto.keyword;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.libs.PoiUtil;
import kai9.auto.common.Syori3Param;

@Component
public class TableExportExcelKw {

    /**
     * テーブル出力(エクセル)
     * 第1引数:テーブル名
     * 第2引数:出力ファイル名
     */
    public void exec(Syori3Param s3p, Boolean IsNeedData) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                if (s3p.sr3 != null) {
                    s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                    return;
                } else {
                    return;
                }
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                if (s3p.sr3 != null) {
                    s3p.sr3s.updateError(s3p.sr3, "第二一引数は省略できません。");
                } else {
                    return;
                }
            }

            String FileName = s3p.s3.getValue2();

            exportTableInfoToExcel(s3p.s3.getValue1(), FileName, s3p, IsNeedData, s3p.getSchema());

            if (s3p.sr3 != null) {
                String crlf = System.lineSeparator();
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "出力しました" + crlf + "ファイル名=" + FileName);
                return;
            } else {
                return;
            }
        } catch (Exception e) {
            if (s3p.sr3 != null) {
                s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
                return;
            } else {
                return;
            }
        }
    }

    /**
     * 
     * テーブル情報をExcelファイルに出力する
     * 
     * @param tableName 出力するテーブル名
     * @param FileName 出力先のファイル名
     * @param s3p DB接続情報などを保持するパラメータクラス
     * @param IsNeedData テーブルのデータも出力するかどうか
     * @param schema_param 出力するテーブルが属するスキーマ名
     * @throws SQLException
     * @throws IOException
     */
    public void exportTableInfoToExcel(String tableName, String FileName, Syori3Param s3p, Boolean IsNeedData, String schema_param) throws SQLException, IOException {
        // テーブルスキーマが空の場合、デフォルト値を設定する
        String schema = schema_param.isEmpty() ? "public" : schema_param;

        // カラム情報を取得するためのSQLクエリ
        String sql = "SELECT a.column_name, a.column_default, a.data_type, a.character_maximum_length, " +
                "a.numeric_precision, a.numeric_scale, a.ordinal_position, d.description " +
                "FROM information_schema.columns a " +
                "JOIN pg_class c ON a.table_name = c.relname " +
                "JOIN pg_namespace n ON c.relnamespace = n.oid " +
                "JOIN pg_attribute attr ON attr.attname = a.column_name AND attr.attrelid = c.oid " +
                "LEFT JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = attr.attnum " +
                "WHERE a.table_name = ? AND a.table_schema = ? " +
                "ORDER BY a.ordinal_position";

        // カラム情報を取得し、リストに格納する
        List<String[]> columnInfoList = s3p.db_jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            String[] columnInfo = new String[7];
            columnInfo[0] = resultSet.getString("column_name");
            columnInfo[1] = resultSet.getString("description");
            columnInfo[2] = resultSet.getString("data_type");
            columnInfo[3] = resultSet.getString("character_maximum_length");
            columnInfo[4] = resultSet.getString("numeric_precision");
            columnInfo[5] = resultSet.getString("numeric_scale");
            return columnInfo;
        }, tableName, schema);

        // Excelファイルを作成する
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(tableName);

        // カラム情報の行を設定する
        for (int i = 0; i < columnInfoList.size(); i++) {
            String[] columnInfo = columnInfoList.get(i);
            // カラム名 (和名)
            createCellWithValue(sheet, 1, i + 2, columnInfo[1]);
            // カラム名 (英名)
            createCellWithValue(sheet, 2, i + 2, columnInfo[0]);
            // データ型
            createCellWithValue(sheet, 3, i + 2, columnInfo[2]);
            // 桁数 (数値型の場合は非表示)
            if ("numeric".equals(columnInfo[2]) || "decimal".equals(columnInfo[2]) ||
                    "smallint".equals(columnInfo[2]) || "integer".equals(columnInfo[2]) ||
                    "bigint".equals(columnInfo[2]) || "real".equals(columnInfo[2]) ||
                    "double precision".equals(columnInfo[2]) || "numeric".equals(columnInfo[2])) {
                createCellWithValue(sheet, 4, i + 2, "");
            } else {
                createCellWithValue(sheet, 4, i + 2, columnInfo[3]);
            }
        }

        // カラム情報のヘッダーを設定する
        createCellWithValue(sheet, 1, 1, "カラム名(和)");
        createCellWithValue(sheet, 2, 1, "カラム名(英)");
        createCellWithValue(sheet, 3, 1, "型");
        createCellWithValue(sheet, 4, 1, "桁数");
        createCellWithValue(sheet, 5, 1, "No");
        createCellWithValue(sheet, 0, 1, "#C1#");
        createCellWithValue(sheet, 0, 2, "#C2#");
        createCellWithValue(sheet, 1, 0, "#R1#");
        createCellWithValue(sheet, 2, 0, "#R2#");
        createCellWithValue(sheet, 3, 0, "#R3#");
        createCellWithValue(sheet, 4, 0, "#R4#");
        createCellWithValue(sheet, 5, 0, "#R5#");
        createCellWithValue(sheet, 6, 0, "#R6#");

        // テーブルデータを取得し、Excelに出力する
        if (IsNeedData) {
            String queryDataSql = "SELECT * FROM " + schema + "." + tableName;
            List<Map<String, Object>> tableData = s3p.db_jdbcTemplate.queryForList(queryDataSql);
            int currentRow = 6;

            // テーブルデータの各行に対して、各カラムの値を出力する
            for (Map<String, Object> rowData : tableData) {
                for (int i = 0; i < columnInfoList.size(); i++) {
                    String columnName = columnInfoList.get(i)[0];
                    Object value = rowData.get(columnName);
                    if (value != null) {
                        createCellWithValue(sheet, currentRow, i + 2, value.toString());
                        createCellWithValue(sheet, currentRow, 1, String.valueOf(currentRow - 5));
                    }
                }
                currentRow++;
            }
        }

        // 格子状の罫線を引く
        PoiUtil.setGridLines(sheet, 1, 1, sheet.getLastRowNum(), PoiUtil.getLastColumnIndex(sheet));

        // 背景色とフォント色を設定する
        CellRangeAddress range = new CellRangeAddress(1, 5, 1, PoiUtil.getLastColumnIndex(sheet)); // ※startRow, endRow, startCol, endCol
        PoiUtil.setCellBackgroundAndFontColor(sheet, range, IndexedColors.GREY_50_PERCENT, IndexedColors.WHITE);
        range = new CellRangeAddress(1, 6, 0, 0); // ※startRow, endRow, startCol, endCol
        PoiUtil.setCellBackgroundAndFontColor(sheet, range, IndexedColors.GREY_50_PERCENT, IndexedColors.WHITE);
        range = new CellRangeAddress(0, 0, 1, 2); // ※startRow, endRow, startCol, endCol
        PoiUtil.setCellBackgroundAndFontColor(sheet, range, IndexedColors.GREY_50_PERCENT, IndexedColors.WHITE);
        range = new CellRangeAddress(5, 5, 2, PoiUtil.getLastColumnIndex(sheet)); // ※startRow, endRow, startCol, endCol
        PoiUtil.setCellBackgroundAndFontColor(sheet, range, IndexedColors.GREY_25_PERCENT, IndexedColors.WHITE);

        // シート内の全フォントを統一する
        PoiUtil.setFont(sheet, "メイリオ", 11);

        // 固定するセルの行番号と列番号を指定します（C7で固定）
        int rowNum = 6;
        int colNum = 2;
        // ウィンドウを固定
        sheet.createFreezePane(colNum, rowNum, colNum, rowNum);

        // システム列の幅を狭くする
        sheet.getRow(0).setHeightInPoints(10);// ピクセル
        sheet.setColumnWidth(0, 256);// 256で1文字分

        // Excelファイルをディスクに書き込む
        try (FileOutputStream fileOut = new FileOutputStream(FileName)) {
            workbook.write(fileOut);
        }

        // workbookを閉じる
        workbook.close();
    }

    // メソッド: セルを作成し、値を設定する
    private void createCellWithValue(XSSFSheet sheet, int rowNumber, int columnNumber, String value) {
        // 指定された行番号で行を取得
        XSSFRow row = sheet.getRow(rowNumber);
        // 行がnullの場合、新しい行を作成
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }
        // 指定された列番号でセルを取得
        XSSFCell cell = row.getCell(columnNumber);
        // セルがnullの場合、新しいセルを作成
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }
        // セルに値を設定
        cell.setCellValue(value);
    }

}
