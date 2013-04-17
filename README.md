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

    // Autowire by type, all three of these are equivalent
	val userService = Wire[UserService]
	val userService = Wire.auto[UserService]
    val userSerivce = Wire.required[UserService]

	// Wire all instances of this type to a Seq[Provider]
	val providers = Wire.all[Provider]

    // Wire by name
    val dataSource = Wire[DataSource]("centralDataRepo")

    // Wire by SpEL expression
    val presidentsBirthCity = Wire[String]("#{Officers['president'].PlaceOfBirth.City}")

    // Placeholder property
    val replyAddress = Wire[String]("${email.reply.to}")
    val replyAddress = Wire.property("${email.reply.to}")
    val replyAddress = Wire.required[String]("${email.reply.to}")

    // Placeholder property with a default value (note the type needing to be nullable, hence using java.lang.Boolean instead of scala Boolean)
    // Even if there is no context available, this will never return null
    val featureEnabled = Wire[java.lang.Boolean]("${feature.enabled:false}").booleanValue()

    // Optional wiring by type or property
    val replyAddress = Wire.option[String]("${email.reply.to}") // returns an Option[String]
    val userService = Wire.option[UserService] // returns an Option[UserService]

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

- `Wire.property` shouldn't require `${}` around the string
- `Wire.value` shouldn't require `#{}` around the string

Compatibility with native Scala getters/setters
------------

Not included in this project - Spring 3.2 and this project sort out these issues: https://github.com/SpringSource/spring-scala