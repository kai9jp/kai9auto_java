package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class applicationYmlEdit {

    /**
     * application.yml更新
     * 第1引数：application.ymlファイルのパス
     * 第2引数：：DB接続情報(primary) 「DBアドレス,ポート,データベース名,DBのID,PW,スキーマ名,ApplicationName」形式で指定
     * 例)localhost,5432,kai9tmpl_5,kai9tmpadmin,pw,kai9tmpl,kai9tmpl-tmpl_5
     * 第3引数：：DB接続情報(common) 記載内容は第２引数と同じ
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue3().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数は省略できません。");
                return;
            }

            // 文字列をカンマで分割
            String[] primary_parts = s3p.s3.getValue2().split(",");
            // 配列の長さが7かどうかを確認
            if (primary_parts.length != 7) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数には、カンマ区切りで7要素セットされている必要があります。" + crlf + "セットされた配列要素数=" + primary_parts.length + crlf + "セットされた文字列=" + s3p.s3.getValue2());
                return;
            }
            // データベースのアドレス
            String primary_dbAddress = primary_parts[0];
            // データベースのポート番号
            String primary_dbPort = primary_parts[1];
            // データベース名
            String primary_dbName = primary_parts[2];
            // データベースのユーザーID
            String primary_dbUserId = primary_parts[3];
            // データベースのパスワード
            String primary_dbPassword = primary_parts[4];
            // スキーマ名
            String primary_schemaName = primary_parts[5];
            // アプリケーション名
            String primary_applicationName = primary_parts[6];

            // 文字列をカンマで分割
            String[] common_parts = s3p.s3.getValue3().split(",");
            // 配列の長さが7かどうかを確認
            if (common_parts.length != 7) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数には、カンマ区切りで7要素セットされている必要があります。" + crlf + "セットされた配列要素数=" + common_parts.length + crlf + "セットされた文字列=" + s3p.s3.getValue3());
                return;
            }
            // データベースのアドレス
            String common_dbAddress = common_parts[0];
            // データベースのポート番号
            String common_dbPort = common_parts[1];
            // データベース名
            String common_dbName = common_parts[2];
            // データベースのユーザーID
            String common_dbUserId = common_parts[3];
            // データベースのパスワード
            String common_dbPassword = common_parts[4];
            // スキーマ名
            String common_schemaName = common_parts[5];
            // アプリケーション名
            String common_applicationName = common_parts[6];

            // JASYPT_ENCRYPTOR_PASSWORDの設定（環境変数から取得）
            String encryptorPassword = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
            if (encryptorPassword == null || encryptorPassword.isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "JASYPT_ENCRYPTOR_PASSWORDが設定されていません。");
                return;
            }

            // エンコーダの設定
            PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
            encryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256"); // SHA-512を使用するアルゴリズム
            encryptor.setIvGenerator(new RandomIvGenerator()); // IV生成器の設定
            encryptor.setPoolSize(4); // プールサイズの設定
            encryptor.setPassword(encryptorPassword); // 暗号化パスワードを設定

            // primaryユーザーIDとパスワードの暗号化
            String encryptedPrimaryUserId = encryptor.encrypt(primary_dbUserId);
            String encryptedPrimaryPassword = encryptor.encrypt(primary_dbPassword);
            // commonユーザーIDとパスワードの暗号化
            String encryptedCommonUserId = encryptor.encrypt(common_dbUserId);
            String encryptedCommonPassword = encryptor.encrypt(common_dbPassword);

            // application.yml更新
            // ファイルを読み込む
            File file = new File(s3p.s3.getValue1());
            if (!file.exists()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数で指定されたファイルが存在しません。" + crlf + "ファイル名=" + s3p.s3.getValue1());
                return;
            }
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            // primaryの各プロパティを置換
            content = replaceYamlProperty(content, "primary", "url", "jdbc:postgresql://" + primary_dbAddress + ":" + primary_dbPort + "/" + primary_dbName + "?ApplicationName=" + primary_applicationName);
            content = replaceYamlProperty(content, "primary", "username", "ENC(" + encryptedPrimaryUserId + ")");
            content = replaceYamlProperty(content, "primary", "password", "ENC(" + encryptedPrimaryPassword + ")");
            content = replaceYamlProperty(content, "primary", "schema", primary_schemaName);

            // commonの各プロパティを置換
            content = replaceYamlProperty(content, "common", "url", "jdbc:postgresql://" + common_dbAddress + ":" + common_dbPort + "/" + common_dbName + "?ApplicationName=" + common_applicationName);
            content = replaceYamlProperty(content, "common", "username", "ENC(" + encryptedCommonUserId + ")");
            content = replaceYamlProperty(content, "common", "password", "ENC(" + encryptedCommonPassword + ")");
            content = replaceYamlProperty(content, "common", "schema", common_schemaName);

            // 変更後の内容をファイルに書き込む
            Files.write(Paths.get(s3p.s3.getValue1()), content.getBytes(StandardCharsets.UTF_8));

            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "application.yml更新に成功しました" + crlf + "[ファイル名]=" + s3p.s3.getValue1());
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

    private String replaceYamlProperty(String content, String section, String property, String newValue) {
        // 改行コードを検出
        String newline = content.contains("\r\n") ? "\r\n" : "\n";
        // 改行コードで分割
        String[] lines = content.split("\\r?\\n");
        StringBuilder newContent = new StringBuilder();
        boolean inSection = false;

        for (String line : lines) {
            if (line.trim().equals(section + ":")) {
                inSection = true;
            } else if (inSection && (!line.startsWith("  ") || line.trim().isEmpty())) {
                inSection = false;
            }

            if (inSection && line.trim().startsWith(property + ":")) {
                int indentLevel = line.indexOf(property + ":"); // インデントレベルを検出
                line = line.substring(0, indentLevel) + property + ": " + newValue; // インデントを保持して置換
            }

            newContent.append(line).append(newline); // 元の改行コードを使用
        }

        return newContent.toString();
    }
}
