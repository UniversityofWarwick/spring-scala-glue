This project is for Spring projects written in Scala. It hopes to define some useful tools to help with doing things in a way that takes advantage of Scala, or wraps things that only really work well in Java projects. It also replaces some AOP-dependant functionality such as @Configurable autowiring.

`SpringConfigured`
-----------------

This is a replacement for the `@Configurable` annotation that normally requires AOP in order to work successfully on non-Spring-managed beans. `SpringConfigured` is just a regular trait. When your class extends this trait, it should get wired appropriately when constructed. Just create an instance of `SpringConfigurer` in your application context, and add the usual annotation readers (such as `context:annotation-config` to enable `@Autowired`) alongside it.

See `SpringConfiguredTest` for usage.

New things we could add
------------

- Replacement for `@Autowired` and `@Resource` annotations on vars - it would be good if instead of

        @Autowired var userLookupService: UserLookupService = _
        @Resource("dataSource") var dataSource: DataSource = _

  we could have this (the val could optionally be `lazy` in case of dependency cycles)

      val userLookupService = autowired[UserLookupService]
      val dataSource = wired[DataSource]("cmsDataSource")

- Investigate compatibility with native Scala getters/setters and collections. This is likely to be difficult to do in a library since Spring's BeanWrapperImpl is hardwired to Java style bean properties, and similarly most of Spring assumes all collections to implement interfaces in the Java Collection API.