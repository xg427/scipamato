package ch.difty.scipamato.core.web.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HikariDatasourceIntegrationTestSettingsTest {

    @Autowired
    @Qualifier("hikariBatchDataSource")
    private DataSource batchDataSource;

    @Autowired
    @Qualifier("hikariPublicDataSource")
    private DataSource publicDataSource;

    @Autowired
    @Qualifier("hikariCoreDataSource")
    private DataSource coreDataSource;

    @Test
    public void validateSettingsBatchDatasourceInProduction() {
        assertThat(batchDataSource).isInstanceOf(HikariDataSource.class);
        HikariDataSource ds = (HikariDataSource) batchDataSource;

        assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/scipamato");
        assertThat(ds.getUsername()).isEqualTo("scipamadmin");
        assertThat(ds.getPassword()).isEqualTo("scipamadmin");
        assertThat(ds.getConnectionTimeout()).isEqualTo(10000);
        assertThat(ds.getIdleTimeout()).isEqualTo(10000);
        assertThat(ds.getMaxLifetime()).isEqualTo(10000);
        assertThat(ds.getMinimumIdle()).isEqualTo(-1);
        assertThat(ds.getMaximumPoolSize()).isEqualTo(2);
        assertThat(ds.getPoolName()).isEqualTo("SciPaMaTo-Batch-HikariCP");
        assertThat(ds.isReadOnly()).isFalse();
    }

    @Test
    public void validateTargetDataSourceInProduction() {
        assertThat(publicDataSource).isInstanceOf(HikariDataSource.class);
        HikariDataSource ds = (HikariDataSource) publicDataSource;

        assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/scipamato_public");
        assertThat(ds.getUsername()).isEqualTo("scipamadminpub");
        assertThat(ds.getPassword()).isEqualTo("scipamadminpub");
        assertThat(ds.getConnectionTimeout()).isEqualTo(10000);
        assertThat(ds.getIdleTimeout()).isEqualTo(10000);
        assertThat(ds.getMaxLifetime()).isEqualTo(10000);
        assertThat(ds.getMinimumIdle()).isEqualTo(-1);
        assertThat(ds.getMaximumPoolSize()).isEqualTo(3);
        assertThat(ds.getPoolName()).isEqualTo("SciPaMaTo-Target-HikariCP");
        assertThat(ds.isReadOnly()).isFalse();
    }

    @Test
    public void validateSettingsSourceDatasourceInProduction() {
        assertThat(coreDataSource).isInstanceOf(HikariDataSource.class);
        HikariDataSource ds = (HikariDataSource) coreDataSource;

        assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/scipamato");
        assertThat(ds.getUsername()).isEqualTo("scipamato");
        assertThat(ds.getPassword()).isEqualTo("scipamato");
        assertThat(ds.getConnectionTimeout()).isEqualTo(1000);
        assertThat(ds.getIdleTimeout()).isEqualTo(10000);
        assertThat(ds.getMaxLifetime()).isEqualTo(1800000);
        assertThat(ds.getMinimumIdle()).isEqualTo(2);
        assertThat(ds.getMaximumPoolSize()).isEqualTo(2);
        assertThat(ds.getPoolName()).isEqualTo("SciPaMaTo-Source-HikariCP");
        assertThat(ds.isReadOnly()).isFalse();
    }

}
