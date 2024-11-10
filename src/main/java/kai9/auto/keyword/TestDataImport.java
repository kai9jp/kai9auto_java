package kai9.auto.keyword;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.libs.PoiUtil;
import kai9.auto.common.Syori3Param;

@Component
public class TestDataImport {

    /**
     * テストデータ投入
     * 
     * 第1引数：テーブル名
     * 第2引数：エクセルのパス
     * 第3引数：シート名。省略時、第1引数のテーブル名をシート名として扱う
     * ※自動でBテーブルのデータも削除される
     * 
     * @throws SQLException
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }

            String TableName = s3p.s3.getValue1();
            String FilePath = s3p.s3.getValue2();
            String sheetName = s3p.s3.getValue3().isEmpty() ? TableName : s3p.s3.getValue3();

            Path path = Paths.get(FilePath);
            if (!Files.exists(path)) {
                s3p.sr3s.updateError(s3p.sr3, "ファイルが存在しません。ファイル名" + FilePath);
                return;
            }

            Integer recordsSize = bulkInsertFromExcel(FilePath, TableName, sheetName, s3p);

            String crlf = System.lineSeparator();
            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "取込に成功しました" + crlf + "テーブル名=" + TableName + crlf + "取込行数=" + recordsSize);
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

    public Map<String, String> getColumnTypes(String excelFilePath, String SheetName) throws IOException {
        Map<String, String> columnTypes = new LinkedHashMap<>(); // 列名と型情報を保持するMap

        // Excelファイルを読み込む
        Path path = Paths.get(excelFilePath);
        InputStream inputStream = Files.newInputStream(path);
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 対象シートを取得する
            Sheet sheet = workbook.getSheet(SheetName);

            // 列名が記載された行番号を取得する
            Integer Row2 = PoiUtil.findRow(sheet, "#R2#");
            if (Row2 == -1) {
                workbook.close();
                throw new RuntimeException("制御文字「#R2#」がエクセルに発見できませんでした:シート名[" + SheetName + "]'");
            }
            // 型情報が記載された行番号を取得する
            Integer Row3 = PoiUtil.findRow(sheet, "#R3#");
            if (Row3 == -1) {
                workbook.close();
                throw new RuntimeException("制御文字「#R3#」がエクセルに発見できませんでした:シート名[" + SheetName + "]'");
            }
            // 開始列番号を取得する
            Integer Col2 = PoiUtil.findCol(sheet, "#C2#");
            if (Col2 == -1) {
                workbook.close();
                throw new RuntimeException("制御文字「#C2#」がエクセルに発見できませんでした:シート名[" + SheetName + "]'");
            }

            // 2行目から列名を取得する
            Row headerRow = sheet.getRow(Row2);
            for (int i = Col2; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    break;
                }
                String columnName = cell.getStringCellValue().trim();

                // 3行目から型情報を取得する
                Row typeRow = sheet.getRow(Row3);
                if (typeRow == null) {
                    throw new IllegalArgumentException("Type row is missing");
                }
                Cell typeCell = typeRow.getCell(i);
                if (typeCell == null || typeCell.getCellType() == CellType.BLANK) {
                    throw new IllegalArgumentException("型の指定が不正です。カラム名=: " + columnName);
                }
                String columnType = typeCell.getStringCellValue().trim().toLowerCase();

                // DBの型に対応するspringの型に変換する
                switch (columnType) {
                case "boolean":
                    columnType = "Boolean";
                    break;
                case "smallint":
                    columnType = "short";
                    break;
                case "integer":
                    columnType = "Integer";
                    break;
                case "bigint":
                    columnType = "long";
                    break;
                case "real":
                    columnType = "float";
                    break;
                case "double":
                    columnType = "long";
                    break;
                case "numeric":
                    columnType = "java.math.BigDecimal";
                    break;
                case "text":
                case "varchar":
                case "character":
                case "character varying":
                    columnType = "String";
                    break;
                case "bytea":
                    columnType = "byte[]";
                    break;
                case "timestamp":
                case "date":
                case "time":
                case "timestamp without time zone":
                    columnType = "Date";
                    break;
                case "smallserial":
                    columnType = "short";
                    break;
                case "serial":
                    columnType = "int";
                    break;
                case "bigserial":
                    columnType = "long";
                    break;
                default:
                    throw new IllegalArgumentException("取込テーブルのカラムがサポート対象外です。カラム名=: " + columnName + "、型" + columnType);
                }

                // 列名と型情報をMapに格納する
                columnTypes.put(columnName, columnType);
            }

            // Excelファイルを閉じる
            workbook.close();
        }

        return columnTypes;
    }

    public List<Map<String, Object>> getRecords(String excelFilePath, String SheetName, Map<String, String> columnTypes) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>(); // レコード情報を保持するList

        // Excelファイルを読み込む
        Path path = Paths.get(excelFilePath);
        InputStream inputStream = Files.newInputStream(path);
        Workbook workbook = new XSSFWorkbook(inputStream);

        // 対象シートを取得する
        Sheet sheet = workbook.getSheet(SheetName);

        // データの開始列番号を取得する
        Integer Col2 = PoiUtil.findCol(sheet, "#C2#");// エラーチェックは割愛(getColumnTypes側で実施)
        // 列名が記載された行番号を取得する
        Integer Row2 = PoiUtil.findRow(sheet, "#R2#");// エラーチェックは割愛(getColumnTypes側で実施)
        // 開始行番号を取得する
        Integer Row6 = PoiUtil.findRow(sheet, "#R6#");
        if (Row6 == -1) {
            workbook.close();
            throw new RuntimeException("制御文字「#R6#」がエクセルに発見できませんでした:シート名[" + SheetName + "]'");
        }

        // 7行目以降からレコード情報を取得する
        for (int i = Row6; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            Map<String, Object> record = new LinkedHashMap<>();
            for (int j = Col2; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                String columnName = sheet.getRow(Row2).getCell(j).getStringCellValue().trim();
                String columnType = columnTypes.get(columnName);
                Object value = null;
                switch (cell.getCellType()) {
                case BLANK:
                    value = null;
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getDateCellValue();
                    } else {
                        if (columnType.equals("short")) {
                            value = (short) cell.getNumericCellValue();
                        } else if (columnType.equals("Integer")) {
                            value = (int) cell.getNumericCellValue();
                        } else if (columnType.equals("long")) {
                            value = (long) cell.getNumericCellValue();
                        } else if (columnType.equals("float")) {
                            value = (float) cell.getNumericCellValue();
                        } else if (columnType.equals("java.math.BigDecimal")) {
                            value = new BigDecimal(cell.getNumericCellValue());
                        }
                    }
                    break;
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            value = cell.getDateCellValue();
                        } else {
                            if (columnType.equals("short")) {
                                value = (short) cell.getNumericCellValue();
                            } else if (columnType.equals("Integer")) {
                                value = (int) cell.getNumericCellValue();
                            } else if (columnType.equals("long")) {
                                value = (long) cell.getNumericCellValue();
                            } else if (columnType.equals("float")) {
                                value = (float) cell.getNumericCellValue();
                            } else if (columnType.equals("java.math.BigDecimal")) {
                                value = new BigDecimal(cell.getNumericCellValue());
                            }
                        }
                        break;
                    case STRING:
                        value = cell.getStringCellValue();
                        break;
                    default:
                        value = null;
                    }
                    break;
                default:
                    value = null;
                }
                record.put(columnName, value);
            }
            records.add(record);
        }

        // Excelファイルを閉じる
        workbook.close();

        return records;
    }

    public Integer bulkInsertFromExcel(String excelFilePath, String tableName, String SheetName, Syori3Param s3p) throws IOException {
        // 列名と型情報を取得する
        Map<String, String> columnTypes = getColumnTypes(excelFilePath, SheetName);

        // レコード情報を取得する
        List<Map<String, Object>> records = getRecords(excelFilePath, SheetName, columnTypes);

        // delete発行
        String sql = "DELETE FROM " + tableName;
        s3p.db_jdbcTemplate.execute(sql);

        // delete発行(Bテーブルが有れば)
        String tableName_b = tableName.replaceAll("_[aA]$", "_b"); // 末尾を"_a"または"_A"から"_b"に変更する
        if (!tableName_b.equals(tableName)) {
            sql = "DELETE FROM " + tableName_b;
            s3p.db_jdbcTemplate.execute(sql);
        }

        // INSERT文の生成
        sql = "INSERT INTO " + tableName + " (";
        sql += String.join(", ", columnTypes.keySet());
        sql += ") VALUES (";
        sql += String.join(", ", Collections.nCopies(columnTypes.size(), "?"));
        sql += ")";

        // バッチ更新(バルクインサートで高速化)
        // バッチ更新(バルクインサートで高速化)
        s3p.db_jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> record = records.get(i);
                int index = 1;
                for (String columnName : columnTypes.keySet()) {
                    String columnType = columnTypes.get(columnName);
                    Object value = record.get(columnName);
                    try {
                        switch (columnType) {
                        // カラムの型がBooleanの場合
                        case "Boolean":
                            // 値がnullの場合は、PreparedStatementのsetNull()メソッドを使用して、null値を設定する
                            if (value == null) {
                                ps.setNull(index, Types.BOOLEAN);
                            }
                            // 値がBoolean型の場合は、PreparedStatementのsetBoolean()メソッドを使用して、Boolean型に変換する
                            else if (value instanceof Boolean) {
                                ps.setBoolean(index, (Boolean) value);
                            }
                            // 値が文字列型の場合は、文字列をBoolean型に変換する
                            else if (value instanceof String) {
                                String strValue = (String) value;
                                // 文字列が"true"または"false"の場合は、Boolean.valueOf()メソッドを使用して、Boolean型に変換する
                                if ("true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)) {
                                    ps.setBoolean(index, Boolean.valueOf(strValue));
                                }
                                // それ以外の場合は、エラーをスローする
                                else {
                                    throw new SQLException("エラー: カラム '" + columnName + "' の値 '" + value + "' を Boolean にキャストできません。インデックス " + index);
                                }
                            }
                            // それ以外の場合は、エラーをスローする
                            else {
                                throw new SQLException("エラー: カラム '" + columnName + "' の値 '" + value + "' を Boolean にキャストできません。インデックス " + index);
                            }
                            break;
                        case "short":
                            ps.setShort(index, (Short) value);
                            break;
                        case "Integer":
                            ps.setInt(index, Integer.valueOf(value.toString()));
                            break;
                        case "long":
                            ps.setLong(index, Long.valueOf(value.toString()));
                            break;
                        case "float":
                            ps.setFloat(index, (Float) value);
                            break;
                        case "java.math.BigDecimal":
                            ps.setBigDecimal(index, (BigDecimal) value);
                            break;
                        case "String":
                            ps.setString(index, (String) value);
                            break;
                        case "byte[]":
                            ps.setBytes(index, (byte[]) value);
                            break;
                        // カラムの型がDateの場合
                        case "Date":
                            // 値がnullの場合は、PreparedStatementのsetNull()メソッドを使用して、null値を設定する
                            if (value == null) {
                                ps.setNull(index, Types.TIMESTAMP);
                            }
                            // 値がDate型の場合は、PreparedStatementのsetTimestamp()メソッドを使用して、Date型に変換する
                            else if (value instanceof Date) {
                                ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
                            }
                            // 値が文字列型の場合は、文字列をDate型に変換する
                            else if (value instanceof String) {
                                String strValue = (String) value;
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                try {
                                    // SimpleDateFormatクラスを使用して、文字列をDate型に変換する
                                    Date dateValue = dateFormat.parse(strValue);
                                    ps.setTimestamp(index, new Timestamp(dateValue.getTime()));
                                }
                                // 変換に失敗した場合は、エラーをスローする
                                catch (ParseException e) {
                                    throw new SQLException("エラー: カラム '" + columnName + "' の値 '" + value + "' を Date にキャストできません。インデックス " + index);
                                }
                            }
                            // それ以外の場合は、エラーをスローする
                            else {
                                throw new SQLException("エラー: カラム '" + columnName + "' の値 '" + value + "' を Date にキャストできません。インデックス " + index);
                            }
                            break;
                        default:
                            throw new SQLException("取込テーブルのカラムがサポート対象外です。カラム名=: " + columnName + "、型" + columnType);
                        }
                    } catch (ClassCastException e) {
                        throw new SQLException("エラー: カラム '" + columnName + "' の値 '" + value + "' を " + columnType + " にキャストできません。インデックス " + index);
                    }
                    index++;
                }
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });

        // シリアル型が採用されている場合、そのシリアル値を現在登録済のレコードに合わせて更新する
        // SQLクエリで、指定されたテーブル全ての列名を取得
        sql = "SELECT attname FROM pg_catalog.pg_attribute WHERE attrelid = ?::regclass AND attnum > 0 AND attisdropped = false";
        List<String> columnNames = s3p.db_jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("attname"), tableName);
        // 変数を初期化
        String sequenceName = null;
        Long generatedId = (long) 0;
        // 各列に対して実行
        for (String columnName : columnNames) {
            // 指定されたテーブルと列名に対応するシーケンス名を取得
            String sequenceNameSql = String.format("SELECT pg_get_serial_sequence('%s', '%s')", tableName, columnName);
            sequenceName = s3p.db_jdbcTemplate.queryForObject(sequenceNameSql, String.class);
            // シーケンス名が見つかった場合、ループを抜ける
            if (sequenceName != null) {
                // 対象テーブルの最大値を取得
                String maxIdSql = "SELECT MAX(" + columnName + ") FROM " + tableName;
                // ofNullableを用い、nullの場合は0で返す
                generatedId = Optional.ofNullable(s3p.db_jdbcTemplate.queryForObject(maxIdSql, Long.class)).orElse(0L);
                break;
            }
        }
        // シリアルキーの次の値を更新する
        if (sequenceName != null) {
            String setValSql = "SELECT setval('" + sequenceName + "', " + (generatedId + 1) + ", true)";
            s3p.db_jdbcTemplate.execute(setValSql);
        }

        return records.size();
    }

}
