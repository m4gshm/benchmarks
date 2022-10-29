package m4gshm.benchmark.rest.quarkus.storage.db;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.hibernate.orm.deployment.HibernateOrmEnabled;
import io.quarkus.hibernate.orm.deployment.JpaModelBuildItem;

import java.util.Map;
import java.util.Set;

@BuildSteps(onlyIfNot = HibernateOrmEnabled.class)
public class DisabledHibernateOrmProcessor {

    @BuildStep
    public void defineNoJpaEntities(BuildProducer<JpaModelBuildItem> domainObjectsProducer) {
        domainObjectsProducer.produce(new JpaModelBuildItem(Set.of(), Set.of(), Set.of(), Set.of(), Map.of()));
    }

}
