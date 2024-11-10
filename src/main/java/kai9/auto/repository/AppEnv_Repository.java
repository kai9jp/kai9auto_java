package kai9.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kai9.auto.model.AppEnv;

/**
 * 環境マスタ :リポジトリ
 */
public interface AppEnv_Repository extends JpaRepository<AppEnv, Integer> {
}
