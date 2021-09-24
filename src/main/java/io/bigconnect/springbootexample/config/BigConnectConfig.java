package io.bigconnect.springbootexample.config;

import com.mware.core.cache.CacheService;
import com.mware.core.cache.InMemoryCacheService;
import com.mware.core.config.HashMapConfigurationLoader;
import com.mware.core.lifecycle.LifeSupportService;
import com.mware.core.model.graph.AccumuloGraphAuthorizationRepository;
import com.mware.core.model.graph.GraphRepository;
import com.mware.core.model.lock.LockRepository;
import com.mware.core.model.lock.SingleJvmLockRepository;
import com.mware.core.model.role.AuthorizationRepository;
import com.mware.core.model.role.GeAuthorizationRepository;
import com.mware.core.model.schema.GeSchemaRepository;
import com.mware.core.model.schema.SchemaRepository;
import com.mware.core.model.termMention.TermMentionRepository;
import com.mware.core.model.user.*;
import com.mware.core.model.workQueue.InMemoryWebQueueRepository;
import com.mware.core.model.workQueue.InMemoryWorkQueueRepository;
import com.mware.core.model.workQueue.WebQueueRepository;
import com.mware.core.model.workQueue.WorkQueueRepository;
import com.mware.core.model.workspace.GeWorkspaceRepository;
import com.mware.core.model.workspace.WorkspaceRepository;
import com.mware.core.orm.SimpleOrmSession;
import com.mware.core.orm.accumulo.AccumuloSimpleOrmSession;
import com.mware.core.security.DirectVisibilityTranslator;
import com.mware.core.time.TimeRepository;
import com.mware.ge.Graph;
import com.mware.ge.GraphFactory;
import com.mware.ge.collection.Iterables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class BigConnectConfig {
    @Bean
    public com.mware.core.config.Configuration getConfiguration(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:bc.properties");
        Properties props = new Properties();
        props.load(resource.getInputStream());
        return new HashMapConfigurationLoader(props).createConfiguration();
    }

    @Bean
    public Graph getGraph(com.mware.core.config.Configuration bcConfig) {
        return new GraphFactory().createGraph(bcConfig.getSubset("graph"));
    }

    @Bean
    public SchemaRepository getSchemaRepository(
            com.mware.core.config.Configuration bcConfig,
            GraphRepository graphRepository,
            GraphAuthorizationRepository graphAuthorizationRepository,
            CacheService cacheService,
            Graph graph
    ) throws Exception {
        return new GeSchemaRepository(
                graph, graphRepository, new DirectVisibilityTranslator(), bcConfig,
                graphAuthorizationRepository, cacheService
        );
    }

    @Bean
    public GraphRepository getGraphRepository(
            Graph graph,
            com.mware.core.config.Configuration bcConfig,
            TermMentionRepository termMentionRepository,
            WorkQueueRepository workQueueRepository
    ) {
        return new GraphRepository(
                graph,
                new DirectVisibilityTranslator(),
                termMentionRepository,
                workQueueRepository,
                new InMemoryWebQueueRepository(),
                bcConfig
        );
    }

    @Bean
    public TermMentionRepository getTermMentionRepository(Graph graph, GraphAuthorizationRepository graphAuthorizationRepository) {
        return new TermMentionRepository(graph, graphAuthorizationRepository);
    }

    @Bean
    public GraphAuthorizationRepository getGraphAuthorizationRepository(Graph graph, LockRepository lockRepository) {
        AccumuloGraphAuthorizationRepository repository = new AccumuloGraphAuthorizationRepository();
        repository.setLockRepository(lockRepository);
        repository.setGraph(graph);
        return repository;
    }

    @Bean
    public LockRepository getLockRepository(LifeSupportService lifeSupportService) {
        return new SingleJvmLockRepository(lifeSupportService);
    }

    @Bean
    public LifeSupportService getLifeSupportService() {
        return new LifeSupportService();
    }

    @Bean
    public WorkQueueRepository getWorkQueueRepository(Graph graph, com.mware.core.config.Configuration bcConfig) {
        return new InMemoryWorkQueueRepository(graph, bcConfig);
    }

    @Bean
    public AuthorizationRepository getAuthorizationRepository(
            Graph graph,
            GraphAuthorizationRepository graphAuthorizationRepository,
            com.mware.core.config.Configuration bcConfig,
            LockRepository lockRepository
    ) {
        return new GeAuthorizationRepository(
                graph, graphAuthorizationRepository, bcConfig, new NopUserNotificationRepository(),
                new InMemoryWebQueueRepository(), lockRepository
        );
    }

    @Bean
    public UserRepository getUserRepository(
            Graph graph,
            com.mware.core.config.Configuration bcConfig,
            SimpleOrmSession simpleOrmSession,
            GraphAuthorizationRepository graphAuthorizationRepository,
            SchemaRepository schemaRepository,
            WorkQueueRepository workQueueRepository,
            LockRepository lockRepository,
            AuthorizationRepository authorizationRepository
    ) {
        WebQueueRepository webQueueRepository = new InMemoryWebQueueRepository();
        UserPropertyPrivilegeRepository userPropertyPrivilegeRepository = new UserPropertyPrivilegeRepository(
                schemaRepository, bcConfig, new NopUserNotificationRepository(),
                webQueueRepository, authorizationRepository
        ) {
            @Override
            protected Iterable<PrivilegesProvider> getPrivilegesProviders(com.mware.core.config.Configuration configuration) {
                return Iterables.asIterable(new BcPrivilegeProvider());
            }
        };

        return new GeUserRepository(
                bcConfig, simpleOrmSession, graphAuthorizationRepository, graph, schemaRepository,
                new InMemoryUserSessionCounterRepository(new TimeRepository()),
                workQueueRepository, webQueueRepository, lockRepository, authorizationRepository,
                userPropertyPrivilegeRepository
        );
    }

    @Bean
    public WorkspaceRepository getWorkspaceRepository(
            Graph graph,
            com.mware.core.config.Configuration bcConfig,
            UserRepository userRepository,
            GraphAuthorizationRepository graphAuthorizationRepository,
            LockRepository lockRepository,
            TermMentionRepository termMentionRepository,
            SchemaRepository schemaRepository,
            WorkQueueRepository workQueueRepository,
            AuthorizationRepository authorizationRepository
    ) {
        return new GeWorkspaceRepository(
                graph, bcConfig, userRepository, graphAuthorizationRepository, lockRepository,
                new DirectVisibilityTranslator(), termMentionRepository, schemaRepository,
                workQueueRepository, new InMemoryWebQueueRepository(), authorizationRepository
        );
    }

    @Bean
    public SimpleOrmSession getSimpleOrmSession(com.mware.core.config.Configuration bcConfig) {
        return new AccumuloSimpleOrmSession(bcConfig.toMap());
    }

    @Bean
    public CacheService getCacheService() {
        return new InMemoryCacheService();
    }
}
