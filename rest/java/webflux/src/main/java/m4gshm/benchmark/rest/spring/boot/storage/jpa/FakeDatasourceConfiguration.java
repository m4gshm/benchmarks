package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration
@ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "false")
public class FakeDatasourceConfiguration {


//    @Bean
    EntityManagerFactory entityManagerFactory() {
        return new EntityManagerFactory() {
            @Override
            public EntityManager createEntityManager() {
                return null;
            }

            @Override
            public EntityManager createEntityManager(Map map) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public Map<String, Object> getProperties() {
                return null;
            }

            @Override
            public Cache getCache() {
                return null;
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                return null;
            }

            @Override
            public void addNamedQuery(String name, Query query) {

            }

            @Override
            public <T> T unwrap(Class<T> cls) {
                return null;
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

            }
        };
    }

//    @Bean
    DataSource fakeDataSource() {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return null;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return null;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
    }
}
