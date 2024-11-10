package kai9.auto.keyword;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class FolderExist {

    /**
     * フォルダ存在確認
     * 第1引数：フォルダ名
     * フォルダ名にワイルドカードを指定可能
     * 
     * [指定フォルダ]
     * C:\Users\Username*\Test*
     * [ヒット例]
     * C:\Users\UsernameA\Test1
     * C:\Users\UsernameB\Test_folder
     * C:\Users\Username\Testing
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード

        try {
            // 第1引数が空かどうかをチェック
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }

            // 第1引数のフォルダ名を取得
            String folderName = s3p.s3.getValue1().trim();

            List<Path> matchingFolders;

            // ワイルドカードが含まれているかをチェック
            if (folderName.contains("*")) {
                // ワイルドカードがある場合、階層ごとに辿る
                matchingFolders = findMatchingFoldersWithWildcard(folderName);
            } else {
                // ワイルドカードがない場合、フォルダの存在を確認
                matchingFolders = findMatchingFoldersWithoutWildcard(folderName);
            }

            // 一致するフォルダがない場合の処理
            if (matchingFolders.isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "指定されたフォルダが存在しません。: " + folderName);
            } else {
                // 一致するフォルダがある場合の処理
                String foundFolders = matchingFolders.stream()
                        .map(path -> path.toString() + crlf)
                        .collect(Collectors.joining());
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "フォルダが見つかりました。" + crlf + foundFolders);
            }
        } catch (Exception e) {
            // エラーが発生した場合の処理
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
        }
    }

    /**
     * ワイルドカードがない場合のフォルダ存在確認
     * 
     * @param folderName 検索するフォルダ名
     * @return 一致するフォルダのリスト（単一の結果）
     */
    private List<Path> findMatchingFoldersWithoutWildcard(String folderName) {
        List<Path> matchingFolders = new ArrayList<>();
        Path path = Paths.get(folderName);
        // フォルダの存在を確認
        if (Files.exists(path) && Files.isDirectory(path)) {
            matchingFolders.add(path);
        }
        return matchingFolders;
    }

    /**
     * ワイルドカードがある場合のフォルダ存在確認
     * 
     * @param folderName 検索するフォルダ名
     * @return 一致するフォルダのリスト
     * @throws IOException 入出力エラーが発生した場合
     */
    private List<Path> findMatchingFoldersWithWildcard(String folderName) throws IOException {
        List<Path> matchingFolders = new ArrayList<>();
        String[] parts = folderName.split("[\\\\/]");
        List<Path> currentPaths = new ArrayList<>();

        // 最初の部分がワイルドカードかどうかをチェック
        if (!parts[0].contains("*")) {
            // 最初の部分がワイルドカードを含まない場合、指定されたディレクトリを初期状態として設定
            currentPaths.add(Paths.get(parts[0] + "\\"));
        } else {
            // 最初の部分がワイルドカードを含む場合、全ルートディレクトリを初期状態として設定
            for (Path rootPath : FileSystems.getDefault().getRootDirectories()) {
                currentPaths.add(rootPath);
            }
        }

        // 各階層を辿って検索
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            List<Path> nextPaths = new ArrayList<>();
            if (part.contains("*")) {
                // ワイルドカードが出現した階層だけ検索
                for (Path path : currentPaths) {
                    try (Stream<Path> stream = Files.walk(path, 1)) {
                        nextPaths.addAll(stream.filter(Files::isDirectory).collect(Collectors.toList()));
                    } catch (UncheckedIOException e) {
                        if (e.getCause() instanceof java.nio.file.AccessDeniedException) {
                            // アクセスが拒否されたディレクトリをスキップ
                        } else {
                            throw e;
                        }
                    }
                }
            } else {
                // ワイルドカードがない場合は指定されたディレクトリを追加
                for (Path path : currentPaths) {
                    Path nextPath = path.resolve(part);
                    if (Files.exists(nextPath) && Files.isDirectory(nextPath)) {
                        nextPaths.add(nextPath);
                    }
                }
            }
            currentPaths = nextPaths;
        }

        // 一致するフォルダを追加
        matchingFolders.addAll(currentPaths);

        return matchingFolders;
    }
}