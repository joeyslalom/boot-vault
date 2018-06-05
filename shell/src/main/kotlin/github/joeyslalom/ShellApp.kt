package github.joeyslalom

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ShellApp

fun main(args: Array<String>) {
    SpringApplication.run(ShellApp::class.java, *args)
}


