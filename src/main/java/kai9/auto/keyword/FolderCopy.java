package kai9.auto.keyword;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class FolderCopy {

    /**
     * フォルダコピー
     * 有れば上書き
     * 第1引数で指定したフォルダを、第2引数のフォルダへコピーする
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

            Path srcPath = Paths.get(s3p.s3.getValue1().trim());
            Path dstPath = Paths.get(s3p.s3.getValue2().trim());

            // フォルダの存在確認
            if (!Files.exists(srcPath)) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数で指定されたフォルダが存在しません。" + srcPath.toString());
                return;
            }
            if (!Files.isDirectory(srcPath)) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数で指定されたパスが不正です(ファイルが指定されています)" + srcPath.toString());
                return;
            }

            try {
                copyFolder(srcPath, dstPath);
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ディレクトリのコピーが完了しました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
            } catch (IOException e) {
                s3p.sr3s.updateError(s3p.sr3, "ディレクトリコピーに失敗しました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
            }

            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

    // フォルダを再帰的にコピー
    private static void copyFolder(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
