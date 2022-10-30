package net.coroke.terminal

import mu.KotlinLogging
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

private val logger = KotlinLogging.logger {}

@SpringBootApplication
@EnableJpaAuditing
class TerminalApplication

fun main(args: Array<String>) {
    runApplication<TerminalApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}
