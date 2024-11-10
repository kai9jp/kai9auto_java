package kai9.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kai9.auto.model.db_version;

/**
 * DBバージョン :リポジトリ
 */
public interface db_version_Repository extends JpaRepository<db_version, Integer> {
}
