package com.steven.hicks.photoService.config

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@Configuration
class DatasourceConfig {

    @Bean
    fun thing(): DataSource {

        val dbUser= System.getenv("dbuser")
        val dbPassword = System.getenv("dbpassword")

        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.postgresql.Driver")
        dataSourceBuilder.url("jdbc:postgresql://shicks255.com:5432/Photos")
        dataSourceBuilder.username(dbUser)
        dataSourceBuilder.password(dbPassword)
        return dataSourceBuilder.build()
    }
}
