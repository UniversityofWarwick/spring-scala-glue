This project is for Spring projects written in Scala. It hopes to define some useful tools to help with doing things in a way that takes advantage of Scala, or wraps things that only really work well in Java projects. It also replaces some AOP-dependant functionality such as @Configurable autowiring.

There's already a more official-looking Spring Scala project and we won't try to duplicate anything they've already done there: https://github.com/SpringSource/spring-scala

`SpringConfigurer`
-----

This is a bean that needs defining in your application context. It will hold a reference to the context for the other classes to use.

    <!-- Support for Wire and SpringConfigured -->
    <bean class="uk.ac.warwick.spring.SpringConfigurer" />

`Wire`
------

This is a factory object that replaces things like `@Autowired`, `@Value` and `@Resource`. 
Importantly it works on non-Spring-managed objects without needing the help of AOP or any
other magic - they are simply methods that resolve beans.

In the below examples you could use `var` instead of `val` if you prefer, and even a `lazy val`
(which can be useful to avoid circular dependency issues that would normally affect constructor autowiring)

    // Autowire by type
		val userService = Wire[UserService]
		val userSercice = Wire.auto[UserService]

		// Wire all instances of this type to a Seq[Provider]
		val providers = Wire.all[Provider]

    // Wire by name
    val dataSource = Wire[DataSource]("centralDataRepo")

    // Wire by SpEL expression
    val presidentsBirthCity = Wire[String]("#{Officers['president'].PlaceOfBirth.City}")

    // Placeholder property
    val replyAddress = Wire[String]("${email.reply.to}")
    val replyAddress = Wire.property("${email.reply.to}")

By default, if the ApplicationContext is missing then the wiring methods will quietly return null. This
is similar to what the wiring annotations will do - in their case they just don't set a value, but usually
the initial value of these properties is null anyway.

`SpringConfigured`
-----------------

This is a replacement for the `@Configurable` annotation that normally requires AOP in order to work successfully on non-Spring-managed beans. `SpringConfigured` is just a regular trait. When your class extends this trait, it should get wired appropriately when constructed. Just create an instance of `SpringConfigurer` in your application context, and add the usual annotation readers (such as `context:annotation-config` to enable `@Autowired`) alongside it.

See `SpringConfiguredTest` for usage. Use of `Wire` is preferred, as it gives you more control
over how things are wired.

New things we could add
------------

- `Wire` methods could add support for not-required dependencies by returning an Option of the bean.
- `Wire.property` shouldn't require `${}` around the string
- `Wire.value` shouldn't require `#{}` around the string
- Investigate compatibility with native Scala getters/setters and collections. This is likely to be difficult to do in a library since Spring's BeanWrapperImpl is hardwired to Java style bean properties, and similarly most of Spring assumes all collections to implement interfaces in the Java Collection API.
  - Spring 3.2 and this project may sort out these issues: https://github.com/SpringSource/spring-scala
