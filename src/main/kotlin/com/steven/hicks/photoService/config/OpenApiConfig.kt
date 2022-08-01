package com.steven.hicks.photoService.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress

@Configuration
class OpenApiConfig {

    @Bean
    fun api(): OpenAPI {
        val host = InetAddress.getLocalHost().hostAddress
        println(host)
        return OpenAPI()
            .addServersItem(Server().url("/"))
            .info(
                Info()
                    .title("photos api")
                    .version("1.0.0")
                    .description("Test")
                    .termsOfService("https://swagger.io/terms/") // todo
                    .license(
                        License().name("Apache 2.0") // todo
                            .url("https://springdoc.org")
                    )
            )
    }
}
