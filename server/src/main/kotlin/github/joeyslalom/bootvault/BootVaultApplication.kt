package github.joeyslalom.bootvault

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BootVaultApplication

fun main(args: Array<String>) {
    runApplication<BootVaultApplication>(*args)
}
