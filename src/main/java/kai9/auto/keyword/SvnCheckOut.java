package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class SvnCheckOut {

    /**
     * SVNチェックアウト
     * 
     * 第1引数のパスに、第2引数のURLをチェックアウトする
     * 
     * 第1引数:ローカルパス
     * 第2引数:SVNパス(URL)
     * 第3引数:SVNユーザ名:パスワード(暗号化したもの)
     * 
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator();// 改行コード
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
            if (!s3p.s3.getValue3().contains(":")) {
                s3p.sr3s.updateError(s3p.sr3, "第3引数に「:」が含まれていません「id:password」形式にして下さい");
                return;
            }

            try {
                // チェックアウトするローカルのパス
                String checkoutPath = s3p.s3.getValue1().trim();
                // SVNリポジトリのURL
                String url = s3p.s3.getValue2().trim();
                // SVNのID
                String username = s3p.s3.getValue3().split(":")[0];
                // SVNのPW
                String password = s3p.s3.getValue3().split(":")[1];

                // SVNプロトコルの初期化（HTTP/HTTPSの場合）
                DAVRepositoryFactory.setup();

                SVNURL repositoryUrl = SVNURL.parseURIEncoded(url);
                ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password.toCharArray());
                SVNClientManager clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), authManager);

                SVNUpdateClient updateClient = clientManager.getUpdateClient();
                updateClient.setIgnoreExternals(false);

                // チェックアウト実行
                // SVNDepth.INFINITY = 指定したパスとその下のすべてのファイルとディレクトリをチェックアウト
                // 最後の引数は、allowUnversionedObstructions ＝ 未バージョン管理のファイルが存在していてもエラーを発生させずに処理を続けるかどうか
                long workingRevision = updateClient.doCheckout(repositoryUrl, new File(checkoutPath), SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);

                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "チェックアウトしました" + crlf + "リビジョン:" + workingRevision);
            } catch (Exception e) {
                s3p.sr3s.updateError(s3p.sr3, "予期せぬエラーが発生しました" + crlf + Kai9Utils.GetException(e));
            }
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
