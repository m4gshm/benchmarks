plugins {
    `java-library`
    id("liquibase-conventions")
}
apply(plugin = "io.spring.dependency-management")

dependencies {
    api("org.liquibase:liquibase-core")
    api("org.postgresql:postgresql")

    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("info.picocli:picocli")
    liquibaseRuntime("org.postgresql:postgresql")
}

val dbSchema by project.extra { "public" }
val dbUsername by project.extra { "postgres" }
val dbPassword by project.extra { "postgres" }
val dbAddress by project.extra { "localhost:5000" }
val dbUrl by project.extra { "jdbc:postgresql://$dbAddress/jvm_orders" }

jooq {
    configuration {
        logging = org.jooq.meta.jaxb.Logging.DEBUG
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl
            user = dbUsername
            password = dbPassword
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database {
                inputSchema = "public"
                name = "org.jooq.meta.postgres.PostgresDatabase"
                includes = ".*"
                excludes = ""
            }
            target {
                packageName = "io.github.m4gshm.orders.data.access.jooq"
            }
        }
    }
}

fun requiredProperty(propertyName: String, defaultValue: String? = null) = project.findProperty(propertyName)
    ?: defaultValue ?: throw GradleException("undefined $propertyName")

tasks.register<LiquibaseTask>("liquibaseUpdate") {
    command = "update"
}

tasks.named("jooqCodegen") {
    dependsOn("liquibaseUpdate")
}

//tasks.withType<JavaCompile> {
//    if (!project.hasProperty("no-codegen")) {
//        dependsOn(tasks.named("jooqCodegen"))
//    }
//}
