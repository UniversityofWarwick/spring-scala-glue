This project is for Spring projects written in Scala. It hopes to define some useful tools to help with doing things in a way that takes advantage of Scala, or wraps things that only really work well in Java projects. It also replaces some AOP-dependant functionality such as @Configurable autowiring.

h3. `SpringConfigured`

This is a replacement for the `@Configurable` annotation that requires AOP in order to work successfully on non-Spring-managed beans. When your class extends this trait, it should get wired appropriately when constructed. Just create an instance of `SpringConfigurer` in your application context, and add the usual annotation readers (such as `context:annotation-config` to enable `@Autowired`) alongside it.

See `SpringConfiguredTest` for usage.

