package org.dockbox.hartshorn.persistence.hibernate;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.di.annotations.inject.Binds;
import org.dockbox.hartshorn.di.properties.Attribute;
import org.dockbox.hartshorn.persistence.SqlService;
import org.dockbox.hartshorn.persistence.context.EntityContext;
import org.dockbox.hartshorn.persistence.properties.ConnectionAttribute;
import org.dockbox.hartshorn.persistence.properties.PersistenceConnection;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

@Binds(SqlService.class)
public class HibernateSqlService implements SqlService {

    private final Configuration configuration = new Configuration();
    private SessionFactory factory;

    @Override
    public boolean canEnable() {
        return this.factory == null;
    }

    @Override
    public void apply(Attribute<?> property) throws ApplicationException {
        if (property instanceof ConnectionAttribute connectionAttribute) {
            final PersistenceConnection connection = connectionAttribute.value();
            if (HartshornUtils.notEmpty(connection.username()) || HartshornUtils.notEmpty(connection.password())) {
                this.configuration.setProperty("hibernate.connection.username", connection.username());
                this.configuration.setProperty("hibernate.connection.password", connection.password());
            }
            this.configuration.setProperty("hibernate.connection.url", connection.url());

            final String driver = connection.remote().driver();
            final String dialect = this.dialect(connection);

            this.configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            this.configuration.setProperty("hibernate.connection.driver_class", driver);
            this.configuration.setProperty("hibernate.dialect", dialect);
        }
    }

    protected String dialect(PersistenceConnection connection) throws ApplicationException {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (connection.remote()) {
            case DERBY -> "org.hibernate.dialect.DerbyTenSevenDialect";
            default -> throw new ApplicationException("Unexpected value: " + connection.remote());
        };
    }

    @Override
    public void enable() throws ApplicationException {
        final Exceptional<EntityContext> context = Hartshorn.context().first(EntityContext.class);
        if (context.absent()) throw new IllegalStateException("Entities were not prepared, did the persistent type service start?");

        final Collection<Class<?>> entities = context.get().entities();
        for (Class<?> entity : entities) {
            this.configuration.addAnnotatedClass(entity);
        }

        final Properties properties = this.configuration.getProperties();

        // Early validation to ensure our configuration is valid
        if (properties.isEmpty()) throw new ApplicationException("Expected connection to be applied, ensure you provided a ConnectionProperty before enabling.");
        if (!properties.containsKey("hibernate.connection.driver_class")) throw new ApplicationException("No driver class provided! Ensure you provided a valid Remote instance");
        if (!properties.containsKey("hibernate.dialect")) throw new ApplicationException("No dialect class provided! Ensure you provided a valid Remote instance");

        try {
            this.factory = this.configuration.buildSessionFactory();
        }
        catch (Throwable e) {
            throw new ApplicationException(e);
        }
    }

    private void session(Consumer<Session> consumer) {
        final Session session = this.factory.openSession();
        session.beginTransaction();
        consumer.accept(session);
        session.getTransaction().commit();
        session.close();
    }

    private <T> T session(Function<Session, T> function) {
        final Session session = this.factory.openSession();
        session.beginTransaction();
        final T result = function.apply(session);
        session.getTransaction().commit();
        session.close();
        return result;
    }

    @Override
    public void save(Object object) {
        this.session(session -> {
            session.save(object);
        });
    }

    @Override
    public void update(Object object) {
        this.session(session -> {
            session.update(object);
        });
    }

    @Override
    public void updateOrSave(Object object) {
        this.session(session -> {
            session.saveOrUpdate(object);
        });
    }

    @Override
    public void delete(Object object) {
        this.session(session -> {
            session.delete(object);
        });
    }

    @Override
    public <T> Set<T> findAll(Class<T> type) {
        return this.session(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<T> criteria = builder.createQuery(type);
            criteria.from(type);
            final List<T> data = session.createQuery(criteria).getResultList();
            return HartshornUtils.asUnmodifiableSet(data);
        });
    }

    @Override
    public <T> Exceptional<T> findById(Class<T> type, Object id) {
        return this.session(session -> {
            return Exceptional.of(() -> session.find(type, id));
        });
    }
}
